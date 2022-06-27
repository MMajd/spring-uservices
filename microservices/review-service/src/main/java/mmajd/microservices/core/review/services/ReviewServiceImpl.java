package mmajd.microservices.core.review.services;

import java.util.ArrayList;
import java.util.List;

import mmajd.microservices.core.review.persistence.ReviewEntity;
import mmajd.microservices.core.review.persistence.ReviewRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.RestController;
import mmajd.api.core.review.Review;
import mmajd.api.core.review.ReviewService;
import mmajd.api.exceptions.InvalidInputException;
import mmajd.util.http.ServiceUtil;

@RestController
public class ReviewServiceImpl implements ReviewService {

  private static final Logger LOG = LoggerFactory.getLogger(ReviewServiceImpl.class);

  private final ServiceUtil serviceUtil;

  private final ReviewRepository repository;
  private final ReviewMapper mapper;

  @Autowired
  public ReviewServiceImpl(ServiceUtil serviceUtil, ReviewRepository repository, ReviewMapper mapper) {
    this.serviceUtil = serviceUtil;
    this.repository = repository;
    this.mapper = mapper;
  }


  @Override
  public Review createReview(Review body) {
    try {
      ReviewEntity entity = mapper.apiToEntity(body);
      ReviewEntity newEntity = repository.save(entity);

      LOG.debug("createReview: created a review entity: {}/{}", body.getProductId(), body.getReviewId());
      return mapper.entityToApi(newEntity);
    } catch (DataIntegrityViolationException ex) {
      throw new InvalidInputException(
              String.format("Duplicate key, Product Id: %d, Review Id: %d",
                      body.getProductId(), body.getReviewId())
      );
    }
  }

  @Override
  public List<Review> getReviews(int productId) {

    if (productId < 1) {
      throw new InvalidInputException("Invalid productId: " + productId);
    }

    List<ReviewEntity> entitiesList = repository.findByProductId(productId);
    List<Review> list = mapper  .entitiesListToApiList(entitiesList);

    list.forEach(e -> e.setServiceAddress(serviceUtil.getServiceAddress()));

    LOG.debug("getReviews: response size: {}", list.size());

    return list;
  }

  @Override
  public void deleteReviews(int productId) {
    LOG.debug("deleteReviews: tries to delete reviews for the product with productId: {}", productId);
    repository.deleteAll(repository.findByProductId(productId));
  }
}
