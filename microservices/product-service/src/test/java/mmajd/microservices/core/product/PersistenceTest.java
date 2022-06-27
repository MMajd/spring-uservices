package mmajd.microservices.core.product;

import mmajd.microservices.core.product.presistence.ProductEntity;
import mmajd.microservices.core.product.presistence.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.mongo.embedded.EmbeddedMongoAutoConfiguration;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

@DataMongoTest(excludeAutoConfiguration = EmbeddedMongoAutoConfiguration.class)
public class PersistenceTest extends MongodbTestBase {
    @Autowired
    private ProductRepository repository;

    private ProductEntity savedEntity;


    @BeforeEach
    void setup() {
        repository.deleteAll();

        ProductEntity entity = ProductEntity.builder()
                .productId(1)
                .name("x")
                .weight(1)
                .build();

        savedEntity = repository.save(entity);
        assertEqualsProduct(entity, savedEntity);
    }

    @Test
    void create() {
        ProductEntity newEntity = ProductEntity.builder()
                .productId(2)
                .name("x2")
                .weight(2)
                .build();

        repository.save(newEntity);

        ProductEntity foundEntity = repository.findById(newEntity.getId()).get();
        assertEqualsProduct(newEntity, foundEntity);

        assertEquals(2, repository.count());
    }

    @Test
    void update() {
        savedEntity.setName("x2");
        repository.save(savedEntity);

        ProductEntity foundEntity = repository.findById(savedEntity.getId()).get();
        assertEquals(1, (long) foundEntity.getVersion());
        assertEquals("x2", foundEntity.getName());
    }

    @Test
    void delete() {
        repository.delete(savedEntity);
        assertFalse(repository.existsById(savedEntity.getId()));
    }

    @Test
    void getProductById() {
        ProductEntity entity = repository.findByProductId(savedEntity.getProductId()).orElse(null);
        assertNotNull(entity);
        assertEqualsProduct(savedEntity, entity);
    }

    @Test
    void duplicateKeysError() {
        assertThrows(DuplicateKeyException.class, () -> {
            ProductEntity entity = ProductEntity.builder()
                    .productId(savedEntity.getProductId())
                    .name("x")
                    .weight(1)
                    .build();

            repository.save(entity);
        });
    }

    @Test
    void optimisticLockError() {
        ProductEntity e1 = repository.findById(savedEntity.getId()).get();
        ProductEntity e2 = repository.findById(savedEntity.getId()).get();

        e1.setName("x2");
        repository.save(e1);

        assertThrows(OptimisticLockingFailureException.class, () -> {
            e2.setName("x3");
            repository.save(e2);
        });

        ProductEntity updatedEntity = repository.findById(savedEntity.getId()).get();
        assertEquals(1, (long) updatedEntity.getVersion());
        assertEquals("x2", updatedEntity.getName());
    }


    @Test
    void paging() {
        repository.deleteAll();

        List<ProductEntity> newEntities = IntStream.rangeClosed(100, 110).mapToObj(
                i -> ProductEntity.builder()
                        .productId(i)
                        .name("name" + i)
                        .weight(i)
                        .build()
        ).collect(Collectors.toList());

        System.out.println(newEntities);

        repository.saveAll(newEntities);

        Pageable nextPage = (Pageable) PageRequest.of(0, 4, Sort.Direction.ASC, "productId");
        nextPage = testNextPage(nextPage, "[100, 101, 102, 103]", true);
        nextPage = testNextPage(nextPage, "[104, 105, 106, 107]", true);
        nextPage = testNextPage(nextPage, "[108, 109, 110]", false);
    }

    private Pageable testNextPage(Pageable nextPage, String expectedProductIds, boolean expectsNextPage) {
        Page<ProductEntity> productEntityPage = repository.findAll(nextPage);
        assertEquals(expectedProductIds, productEntityPage.getContent().stream().map(ProductEntity::getProductId).collect(Collectors.toList()).toString());
        assertEquals(expectsNextPage, productEntityPage.hasNext());
        return productEntityPage.nextPageable();
    }
    private void assertEqualsProduct(ProductEntity expected, ProductEntity actual) {
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getVersion(), actual.getVersion());
        assertEquals(expected.getProductId(), actual.getProductId());
        assertEquals(expected.getName(), actual.getName());
        assertEquals(expected.getWeight(), actual.getWeight());
    }
}
