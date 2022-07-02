package mmajd.microservices.core.review.services;

import mmajd.api.core.review.Review;
import mmajd.api.core.review.ReviewService;
import mmajd.api.exceptions.InvalidInputException;
import mmajd.microservices.core.review.persistence.ReviewEntity;
import mmajd.microservices.core.review.persistence.ReviewRepository;
import mmajd.util.http.ServiceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import java.util.List;
import java.util.logging.Level;

import static java.util.logging.Level.FINE;

@RestController
public class ReviewServiceImpl implements ReviewService {

  private static final Logger LOG = LoggerFactory.getLogger(ReviewServiceImpl.class);

  private final ServiceUtil serviceUtil;

  private final ReviewRepository repository;
  private final ReviewMapper mapper;

  private final Scheduler jdbcScheduler;

  @Autowired
  public ReviewServiceImpl(
          @Qualifier("jdbcScheduler") Scheduler jdbcScheduler,
          ServiceUtil serviceUtil, ReviewRepository repository,
          ReviewMapper mapper) {

    this.serviceUtil = serviceUtil;
    this.repository = repository;
    this.mapper = mapper;
    this.jdbcScheduler = jdbcScheduler;
  }


  @Override
  public Mono<Review> createReview(Review body) {
    if (body.getProductId() < 1) {
      throw new InvalidInputException(String.format("Invalid productId: %d", body.getProductId()));
    }

    return Mono
            .fromCallable(() -> createReviewCallback(body))
            .subscribeOn(jdbcScheduler);
  }

  @Override
  public Flux<Review> getReviews(int productId) {

    if (productId < 1) {
      throw new InvalidInputException("Invalid productId: " + productId);
    }

    return Mono
            .fromCallable(() -> getReviewsCallback(productId))
            .flatMapMany(Flux::fromIterable)
            .log(LOG.getName(), FINE)
            .subscribeOn(jdbcScheduler);
  }

  @Override
  public Mono<Void> deleteReviews(int productId) {
    if (productId < 1) throw new InvalidInputException("Invalid productId: " + productId);

    return Mono
            .fromRunnable(() -> deleteReviewsCallback(productId))
            .subscribeOn(jdbcScheduler)
            .then();
  }

  private void deleteReviewsCallback(int productId) {
    LOG.debug("deleteReviews: tries to delete reviews for the product with productId: {}", productId);
    repository.deleteAll(repository.findByProductId(productId));
  }

  private List<Review> getReviewsCallback(int productId) {
    List<ReviewEntity> entitiesList = repository.findByProductId(productId);
    List<Review> list = mapper  .entitiesListToApiList(entitiesList);

    list.forEach(e -> e.setServiceAddress(serviceUtil.getServiceAddress()));

    LOG.debug("getReviews: response size: {}", list.size());

    return list;
  }


  private Review createReviewCallback(Review body) {
    try {
      ReviewEntity entity = mapper.apiToEntity(body);
      ReviewEntity newEntity = repository.save(entity);

      LOG.debug("createReview: created a review entity: {}/{}", body.getProductId(), body.getReviewId());
      return mapper.entityToApi(newEntity);
    }
    catch (DataIntegrityViolationException ex) {
      throw new InvalidInputException(
              String.format("Duplicate key, Product Id: %d, Review Id: %d",
                      body.getProductId(), body.getReviewId()));
    }
  }
}
