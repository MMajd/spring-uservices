package mmajd.microservices.core.recommendation.services;

import mmajd.api.core.recommendation.Recommendation;
import mmajd.microservices.core.recommendation.entity.RecommendationEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RecommendationMapper {
    @Mappings({
            @Mapping(target = "rate", source = "entity.rating"),
            @Mapping(target = "serviceAddress", ignore = true)
    })
    Recommendation  entityToApi(RecommendationEntity entity);

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "rating", source = "api.rate"),
            @Mapping(target = "version", ignore = true)
    })
    RecommendationEntity apiToEntity(Recommendation api);

    List<Recommendation> entityListToApiList(List<RecommendationEntity> entities);
    List<RecommendationEntity> apiListToEntityList(List<Recommendation> apis);
}
