package mmajd.microservices.core.review.services;

import mmajd.api.core.review.Review;
import mmajd.microservices.core.review.persistence.ReviewEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ReviewMapper {

    @Mappings({
            @Mapping(target = "version", ignore = true),
            @Mapping(target = "id", ignore = true)
    })
    ReviewEntity apiToEntity(Review api);


    @Mappings({
            @Mapping(target = "serviceAddress", ignore = true),
    })
    Review entityToApi(ReviewEntity entity);

    @Mappings({})
    List<ReviewEntity> apiListToEntitiesList(List<Review> apis);

    @Mappings({})
    List<Review> entitiesListToApiList(List<ReviewEntity> apis);
}
