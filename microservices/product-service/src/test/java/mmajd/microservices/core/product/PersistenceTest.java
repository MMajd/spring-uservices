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
import reactor.test.StepVerifier;

import java.util.List;
import java.util.Objects;
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
        StepVerifier
                .create(repository.deleteAll())
                .verifyComplete();

        ProductEntity entity = ProductEntity.builder()
                .productId(1)
                .name("x")
                .weight(1)
                .build();

        StepVerifier
                .create(repository.save(entity))
                .expectNextMatches(e -> {
                    savedEntity = e;
                    return areProductEqual(entity, savedEntity);
                })
                .verifyComplete();
    }


    @Test
    void create() {
        ProductEntity newEntity = ProductEntity.builder()
                .productId(2)
                .name("x2")
                .weight(2)
                .build();

        StepVerifier.create(repository
                        .save(newEntity))
                .expectNextMatches(e -> Objects.equals(newEntity.getProductId(), e.getProductId()))
                .verifyComplete();


        StepVerifier.create(repository.findById(newEntity.getId()))
                .expectNextMatches(foundEntity -> areProductEqual(newEntity, foundEntity))
                .verifyComplete();
    }

    @Test
    void update() {
        savedEntity.setName("x2");
        StepVerifier.create(repository.save(savedEntity))
                .expectNextMatches(e -> {
                    return e.getName().equals("x2");
                })
                .verifyComplete();

        StepVerifier.create(repository.findById(savedEntity.getId()))
                .expectNextMatches(e -> {
                    return e.getVersion() == 1
                            && e.getName().equals("x2");
                }).verifyComplete();
    }

    @Test
    void delete() {
        StepVerifier
                .create(repository.delete(savedEntity))
                .verifyComplete();
        StepVerifier
                .create(repository.existsById(savedEntity.getId()))
                .expectNext(false).verifyComplete();
    }

    @Test
    void getProductById() {
        StepVerifier
                .create(repository.findByProductId(savedEntity.getProductId()))
                .expectNextMatches(e -> areProductEqual(savedEntity, e))
                .verifyComplete();
    }

    @Test
    void duplicateKeysError() {
        ProductEntity entity = ProductEntity.builder()
                .productId(savedEntity.getProductId())
                .name("x")
                .weight(1)
                .build();

        StepVerifier
                .create(repository.save(entity))
                .expectError(DuplicateKeyException.class)
                .verify();
    }

    @Test
    void optimisticLockError() {
        ProductEntity e1 = repository.findById(savedEntity.getId()).block();
        ProductEntity e2 = repository.findById(savedEntity.getId()).block();

        assertNotNull(e1);
        e1.setName("x2");
        repository.save(e1).block();

        assertNotNull(e2);
        StepVerifier
                .create(repository.save(e2))
                .expectError(OptimisticLockingFailureException.class)
                .verify();

        StepVerifier
                .create(repository.findById(savedEntity.getId()))
                .expectNextMatches(e -> e.getVersion() == 1 && e.getName().equals("x2"))
                .verifyComplete();
    }


//    @Test
//    void paging() {
//        repository.deleteAll();
//
//        List<ProductEntity> newEntities = IntStream.rangeClosed(100, 110).mapToObj(
//                i -> ProductEntity.builder()
//                        .productId(i)
//                        .name("name" + i)
//                        .weight(i)
//                        .build()
//        ).collect(Collectors.toList());
//
//        System.out.println(newEntities);
//
//        repository.saveAll(newEntities);
//
//        Pageable nextPage = (Pageable) PageRequest.of(0, 4, Sort.Direction.ASC, "productId");
//        nextPage = testNextPage(nextPage, "[100, 101, 102, 103]", true);
//        nextPage = testNextPage(nextPage, "[104, 105, 106, 107]", true);
//        nextPage = testNextPage(nextPage, "[108, 109, 110]", false);
//    }
//
//    private Pageable testNextPage(Pageable nextPage, String expectedProductIds, boolean expectsNextPage) {
//        Page<ProductEntity> productEntityPage = repository.findAll(nextPage);
//        assertEquals(expectedProductIds, productEntityPage.getContent().stream().map(ProductEntity::getProductId).collect(Collectors.toList()).toString());
//        assertEquals(expectsNextPage, productEntityPage.hasNext());
//        return productEntityPage.nextPageable();
//    }
//    private void assertEqualsProduct(ProductEntity expected, ProductEntity actual) {
//        assertEquals(expected.getId(), actual.getId());
//        assertEquals(expected.getVersion(), actual.getVersion());
//        assertEquals(expected.getProductId(), actual.getProductId());
//        assertEquals(expected.getName(), actual.getName());
//        assertEquals(expected.getWeight(), actual.getWeight());
//    }

    private boolean areProductEqual(ProductEntity expected, ProductEntity actual) {
        return (
                (expected.getId().equals(actual.getId()))
                        && (Objects.equals(expected.getVersion(), actual.getVersion()))
                        && (expected.getName().equals(actual.getName()))
                        && (Objects.equals(expected.getWeight(), actual.getWeight()))
        );
    }
}
