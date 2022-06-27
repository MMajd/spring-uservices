package mmajd.microservices.core.recommendation;

import lombok.var;
import mmajd.microservices.core.recommendation.entity.RecommendationEntity;
import mmajd.microservices.core.recommendation.entity.RecommendationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.mongo.embedded.EmbeddedMongoAutoConfiguration;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.OptimisticLockingFailureException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataMongoTest(excludeAutoConfiguration = EmbeddedMongoAutoConfiguration.class)
public class PersistenceTest extends MongodbTestBase {

    @Autowired
    private RecommendationRepository repository;

    private RecommendationEntity savedEntity;

    @BeforeEach
    void setup() {
        repository.deleteAll();

        RecommendationEntity entity = RecommendationEntity.builder()
                .productId(1)
                .recommendationId(2)
                .author("a")
                .content("c")
                .rating(3)
                .build();

        savedEntity = repository.save(entity);
        assertEqualsRecommendation(entity, savedEntity);
    }

    @Test
    void create() {
        RecommendationEntity newEntity = RecommendationEntity.builder()
                .productId(1)
                .recommendationId(3)
                .author("a")
                .rating(3)
                .content("c")
                .build();

        repository.save(newEntity);

        var found = repository.findById(newEntity.getId()).get();

        assertNotNull(found);

        assertEquals(0, found.getVersion().intValue());
        assertEquals("a", found.getAuthor());
    }

    @Test
    void update() {
        savedEntity.setAuthor("a2");
        repository.save(savedEntity);

        RecommendationEntity found = repository.findById(savedEntity.getId()).get();

        assertNotNull(found);

        assertEquals("a2", found.getAuthor());
    }

    @Test
    void delete() {
        repository.delete(savedEntity);
        assertFalse(repository.existsById(savedEntity.getId()));
    }

    @Test
    void getByProductId() {
        List<RecommendationEntity> entities = repository.findByProductId(savedEntity.getProductId());
        assertThat(entities).hasSize(1);

        assertEqualsRecommendation(savedEntity, entities.get(0));
    }

    @Test
    void duplicateError() {
        assertThrows(DuplicateKeyException.class, () -> {
            RecommendationEntity e1 = RecommendationEntity.builder()
                    .productId(1) // throws the error;
                    .recommendationId(2)
                    .author("a")
                    .rating(3)
                    .content("c")
                    .build();

            repository.save(e1);
        });
    }


    @Test
    void optimisticLockingError() {
        RecommendationEntity e1 = repository.findById(savedEntity.getId()).get();
        RecommendationEntity e2 = repository.findById(savedEntity.getId()).get();

        assertNotNull(e1);
        assertNotNull(e2);

        e1.setAuthor("a1");
        repository.save(e1);

        assertThrows(OptimisticLockingFailureException.class, () -> {
           e2.setAuthor("a2");
           repository.save(e2);
        });

        RecommendationEntity updated = repository.findById(savedEntity.getId()).get();
        assertNotNull(updated);

        assertEquals(1, updated.getVersion().intValue());
        assertEquals("a1", updated.getAuthor());
    }

    private void assertEqualsRecommendation(RecommendationEntity expected, RecommendationEntity actual) {
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getVersion(), actual.getVersion());
        assertEquals(expected.getProductId(), actual.getProductId());
        assertEquals(expected.getRecommendationId(), actual.getRecommendationId());
        assertEquals(expected.getRating(), actual.getRating());
        assertEquals(expected.getContent(), actual.getContent());
        assertEquals(expected.getAuthor(), actual.getAuthor());
    }

}
