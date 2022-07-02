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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

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
  public Flux<Recommendation> getRecommendations(int productId) {

    if (productId < 1) {
      throw new InvalidInputException("Invalid productId: " + productId);
    }

    LOG.info("Will get recommendations for product with id={}", productId);

    return repository
            .findByProductId(productId)
            .log(LOG.getName(), Level.FINE)
            .map(mapper::entityToApi)
            .map(e -> {
              e.setServiceAddress(serviceUtil.getServiceAddress());
              return e;
            });
  }

  @Override
  public Mono<Recommendation> createRecommendation(Recommendation body) {
    if (body.getProductId() < 1) {
      throw new InvalidInputException("Invalid productId: " + body.getProductId());
    }

    LOG.debug("createRecommendation: created a recommendation entity: {}/{}", body.getProductId(), body.getRecommendationId());

    RecommendationEntity entity = mapper.apiToEntity(body);

    return repository.save(entity)
            .log(LOG.getName(), Level.FINE)
            .onErrorMap(DuplicateKeyException.class, ex -> new InvalidInputException(
                    String
                            .format("Duplicate key, Product ID: %d, Recommendation ID: %d",
                                    body.getProductId(), body.getRecommendationId())))
            .map(mapper::entityToApi);
  }

  @Override
  public Mono<Void> deleteRecommendations(int productId) {
    if (productId < 1) {
      throw new InvalidInputException("Invalid productId: " + productId);
    }

    LOG.debug("deleteRecommendations: tries to delete recommendations for the product with productId: {}", productId);

    return repository
            .deleteAll(repository.findByProductId(productId));
  }
}
