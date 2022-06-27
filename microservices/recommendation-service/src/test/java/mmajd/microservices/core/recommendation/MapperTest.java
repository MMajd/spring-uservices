package mmajd.microservices.core.recommendation;

import mmajd.api.core.recommendation.Recommendation;
import mmajd.microservices.core.recommendation.entity.RecommendationEntity;
import mmajd.microservices.core.recommendation.services.RecommendationMapper;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class MapperTest {
    private RecommendationMapper mapper = Mappers.getMapper(RecommendationMapper.class);

    @Test
    void mapperTest() {
        assertNotNull(mapper);

        Recommendation api = Recommendation.builder()
                .productId(1)
                .recommendationId(2)
                .author("a")
                .rate(4)
                .content("C")
                .serviceAddress("adr")
                .build();

        RecommendationEntity entity = mapper.apiToEntity(api);

        assertEquals(api.getProductId(), entity.getProductId());
        assertEquals(api.getRecommendationId(), entity.getRecommendationId());
        assertEquals(api.getAuthor(), entity.getAuthor());
        assertEquals(api.getContent(), entity.getContent());
        assertEquals(api.getRate(), entity.getRating());

        Recommendation doubleMappedApi = mapper.entityToApi(entity);

        assertNotNull(doubleMappedApi);

        assertEquals(entity.getProductId(), doubleMappedApi.getProductId());
        assertEquals(entity.getRecommendationId(), doubleMappedApi.getRecommendationId());
        assertEquals(entity.getAuthor(), doubleMappedApi.getAuthor());
        assertEquals(entity.getContent(), doubleMappedApi.getContent());
        assertEquals(entity.getRating(), doubleMappedApi.getRate());
    }
}
