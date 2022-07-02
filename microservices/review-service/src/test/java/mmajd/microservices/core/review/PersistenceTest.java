package mmajd.microservices.core.review;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.transaction.annotation.Propagation.NOT_SUPPORTED;

import java.util.List;

import mmajd.microservices.core.review.persistence.ReviewEntity;
import mmajd.microservices.core.review.persistence.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.transaction.annotation.Transactional;

@DataJpaTest
@Transactional(propagation = NOT_SUPPORTED) // stop clearing our database
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) // stop auto configration
class PersistenceTests extends MySqlTestBase {

    @Autowired
    private ReviewRepository repository;

    private ReviewEntity savedEntity;

    @BeforeEach
    void setup() {
        repository.deleteAll();

        ReviewEntity entity = ReviewEntity.builder()
                .productId(1)
                .reviewId(2)
                .author("a")
                .content("c")
                .subject("s")
                .build();
        savedEntity = repository.save(entity);

        assertEqualsReview(entity, savedEntity);
    }


    @Test
    void create() {

        ReviewEntity newEntity = ReviewEntity.builder()
                .productId(1)
                .reviewId(3)
                .author("a")
                .content("c")
                .subject("s")
                .build();
        repository.save(newEntity);

        ReviewEntity foundEntity = repository.findById(newEntity.getId()).get();
        assertEqualsReview(newEntity, foundEntity);

        assertEquals(2, repository.count());
    }

    @Test
    void update() {
        savedEntity.setAuthor("a2");
        repository.save(savedEntity);

        ReviewEntity foundEntity = repository.findById(savedEntity.getId()).get();
        assertEquals(1, (long)foundEntity.getVersion());
        assertEquals("a2", foundEntity.getAuthor());
    }

    @Test
    void delete() {
        repository.delete(savedEntity);
        assertFalse(repository.existsById(savedEntity.getId()));
    }

    @Test
    void getByProductId() {
        List<ReviewEntity> entityList = repository.findByProductId(savedEntity.getProductId());

        assertThat(entityList, hasSize(1));
        assertEqualsReview(savedEntity, entityList.get(0));
    }

    @Test
    void duplicateError() {
        assertThrows(DataIntegrityViolationException.class, () -> {
            ReviewEntity entity  = ReviewEntity.builder()
                    .productId(1)
                    .reviewId(2)
                    .author("a")
                    .content("c")
                    .subject("s")
                    .build();

            repository.save(entity);
        });

    }

    @Test
    void optimisticLockError() {

        ReviewEntity entity1 = repository.findById(savedEntity.getId()).get();
        ReviewEntity entity2 = repository.findById(savedEntity.getId()).get();

        entity1.setAuthor("a1");
        repository.save(entity1);

        assertThrows(OptimisticLockingFailureException.class, () -> {
            entity2.setAuthor("a2");
            repository.save(entity2);
        });

        ReviewEntity updatedEntity = repository.findById(savedEntity.getId()).get();
        assertEquals(1, updatedEntity.getVersion());
        assertEquals("a1", updatedEntity.getAuthor());
    }

    private void assertEqualsReview(ReviewEntity expectedEntity, ReviewEntity actualEntity) {
        assertEquals(expectedEntity.getId(),        actualEntity.getId());
        assertEquals(expectedEntity.getVersion(),   actualEntity.getVersion());
        assertEquals(expectedEntity.getProductId(), actualEntity.getProductId());
        assertEquals(expectedEntity.getReviewId(),  actualEntity.getReviewId());
        assertEquals(expectedEntity.getAuthor(),    actualEntity.getAuthor());
        assertEquals(expectedEntity.getSubject(),   actualEntity.getSubject());
        assertEquals(expectedEntity.getContent(),   actualEntity.getContent());
    }
}