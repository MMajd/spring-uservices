package mmajd.microservices.core.recommendation.services;

import mmajd.api.core.recommendation.Recommendation;
import mmajd.api.core.recommendation.RecommendationService;
import mmajd.api.exceptions.InvalidInputException;
import mmajd.microservices.core.recommendation.entity.RecommendationEntity;
import mmajd.microservices.core.recommendation.entity.RecommendationRepository;
import mmajd.util.http.ServiceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
public class RecommendationServiceImpl implements RecommendationService {

  private static final Logger LOG = LoggerFactory.getLogger(RecommendationServiceImpl.class);

  private final ServiceUtil serviceUtil;

  private final RecommendationRepository repository;

  private final RecommendationMapper mapper;

  @Autowired
  public RecommendationServiceImpl(ServiceUtil serviceUtil, RecommendationRepository repository, RecommendationMapper mapper) {
    this.serviceUtil = serviceUtil;
    this.repository = repository;
    this.mapper = mapper;
  }

  @Override
  public List<Recommendation> getRecommendations(int productId) {

    if (productId < 1) {
      throw new InvalidInputException("Invalid productId: " + productId);
    }

    List<RecommendationEntity> entityList = repository.findByProductId(productId);
    List<Recommendation> apiList = mapper.entityListToApiList(entityList);

    apiList.forEach(e -> e.setServiceAddress(serviceUtil.getServiceAddress()));

    LOG.debug("getRecommendations: response size: {}", apiList.size());

    return apiList;
  }

  @Override
  public Recommendation createRecommendation(Recommendation body) {
    try {
      RecommendationEntity entity = mapper.apiToEntity(body);
      RecommendationEntity newEntity = repository.save(entity);

      LOG.debug("createRecommendation: created a recommendation entity: {}/{}", body.getProductId(), body.getRecommendationId());

      return mapper.entityToApi(newEntity);

    } catch(DuplicateKeyException ex) {
      throw new InvalidInputException(
              String
                      .format("Duplicate key, Product ID: %d, Recommendation ID: %d",
                              body.getProductId(), body.getRecommendationId()));

    }
  }

  @Override
  public void deleteRecommendations(int productId) {
    LOG.debug("deleteRecommendations: tries to delete recommendations for the product with productId: {}", productId);
    repository.deleteAll(repository.findByProductId(productId));
  }
}
