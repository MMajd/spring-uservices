package mmajd.microservices.composite.product.services;

import mmajd.api.composite.product.*;
import mmajd.api.core.product.Product;
import mmajd.api.core.recommendation.Recommendation;
import mmajd.api.core.review.Review;
import mmajd.api.exceptions.NotFoundException;
import mmajd.util.http.ServiceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
public class ProductCompositeServiceImpl implements ProductCompositeService {

  private static final Logger LOG = LoggerFactory.getLogger(ProductCompositeServiceImpl.class);

  private final ServiceUtil serviceUtil;
  private final ProductCompositeIntegration integration;

  @Autowired
  public ProductCompositeServiceImpl(
          ServiceUtil serviceUtil, ProductCompositeIntegration integration) {

    this.serviceUtil = serviceUtil;
    this.integration = integration;
  }

  @Override
  public void createProduct(ProductAggregate body) {

    try {

      LOG.debug("createCompositeProduct: creates a new composite entity for productId: {}", body.getProductId());

      Product product = Product.builder()
              .productId(body.getProductId())
              .name(body.getName())
              .weight(body.getWeight())
              .serviceAddress(null)
              .build();

      integration.createProduct(product);

      if (body.getRecommendations() != null) {
        body.getRecommendations().forEach(r -> {
          Recommendation recommendation = Recommendation.builder()
                  .productId(body.getProductId())
                  .recommendationId(r.getRecommendationId())
                  .author(r.getAuthor())
                  .rate(r.getRate())
                  .content(r.getContent())
                  .build();
          integration.createRecommendation(recommendation);
        });
      }

      if (body.getReviews() != null) {
        body.getReviews().forEach(r -> {
          Review review = Review.builder()
                  .productId(body.getProductId())
                  .reviewId(r.getReviewId())
                  .author(r.getAuthor())
                  .content(r.getContent())
                  .subject(r.getSubject())
                  .build();
          integration.createReview(review);
        });
      }

      LOG.debug("createCompositeProduct: composite entities created for productId: {}", body.getProductId());

    } catch (RuntimeException re) {
      LOG.warn("createCompositeProduct failed", re);
      throw re;
    }
  }


  @Override
  public ProductAggregate getProduct(int productId) {

    LOG.debug("getCompositeProduct: lookup a product aggregate for productId: {}", productId);

    Product product = integration.getProduct(productId);
    if (product == null) {
      throw new NotFoundException("No product found for productId: " + productId);
    }

    List<Recommendation> recommendations = integration.getRecommendations(productId);

    List<Review> reviews = integration.getReviews(productId);

    LOG.debug("getCompositeProduct: aggregate entity found for productId: {}", productId);

    return createProductAggregate(product, recommendations, reviews, serviceUtil.getServiceAddress());
  }

  @Override
  public void deleteProduct(int productId) {

    LOG.debug("deleteCompositeProduct: Deletes a product aggregate for productId: {}", productId);

    integration.deleteProduct(productId);

    integration.deleteRecommendations(productId);

    integration.deleteReviews(productId);

    LOG.debug("deleteCompositeProduct: aggregate entities deleted for productId: {}", productId);
  }

  private ProductAggregate createProductAggregate(
          Product product,
          List<Recommendation> recommendations,
          List<Review> reviews,
          String serviceAddress) {

    System.out.println(product);

    String name = product.getName();
    int weight = product.getWeight();
    int productId = product.getProductId();

    List<RecommendationSummary> recommendationSummaries = (recommendations == null) ? null :
            recommendations.stream()
                    .map(r -> RecommendationSummary.builder()
                            .recommendationId(r.getRecommendationId())
                            .rate(r.getRate())
                            .author(r.getAuthor())
                            .content(r.getContent())
                            .build())
                    .collect(Collectors.toList());

    System.out.printf(reviews.toString());

    List<ReviewSummary> reviewSummaries = (reviews == null) ? null :
            reviews.stream()
                    .map(r -> ReviewSummary.builder()
                            .reviewId(r.getReviewId())
                            .author(r.getAuthor())
                            .subject(r.getSubject())
                            .content(r.getContent())
                            .build())
                    .collect(Collectors.toList());

    String productAddress = product.getServiceAddress();
    String reviewAddress = (reviews != null && reviews.size() > 0) ? reviews.get(0).getServiceAddress() : "";
    String recommendationAddress = (recommendations != null && recommendations.size() > 0) ? recommendations.get(0).getServiceAddress() : "";
    ServiceAddresses serviceAddresses = new ServiceAddresses(serviceAddress, productAddress, reviewAddress, recommendationAddress);

    return new ProductAggregate(productId, name, weight, recommendationSummaries, reviewSummaries, serviceAddresses);
  }
}
