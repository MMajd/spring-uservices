package mmajd.microservices.composite.product;

import static java.util.Collections.singletonList;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static reactor.core.publisher.Mono.just;

import mmajd.api.composite.product.ProductAggregate;
import mmajd.api.composite.product.RecommendationSummary;
import mmajd.api.composite.product.ReviewSummary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;
import mmajd.api.core.product.Product;
import mmajd.api.core.recommendation.Recommendation;
import mmajd.api.core.review.Review;
import mmajd.api.exceptions.InvalidInputException;
import mmajd.api.exceptions.NotFoundException;
import mmajd.microservices.composite.product.services.ProductCompositeIntegration;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@SpringBootTest(webEnvironment = RANDOM_PORT)
class ProductCompositeServiceApplicationTests {

  private static final int PRODUCT_ID_OK = 1;
  private static final int PRODUCT_ID_NOT_FOUND = 2;
  private static final int PRODUCT_ID_INVALID = 3;

  @Autowired
  private WebTestClient client;

  @MockBean
  private ProductCompositeIntegration compositeIntegration;

  @BeforeEach
  void setUp() {

    when(compositeIntegration.getProduct(PRODUCT_ID_OK))
            .thenReturn(Mono.just(Product
                            .builder()
                            .name("name")
                            .productId(PRODUCT_ID_OK)
                            .weight(0)
                            .serviceAddress("mock-address")
                            .build()));

    when(compositeIntegration.getRecommendations(PRODUCT_ID_OK))
            .thenReturn(Flux.fromIterable(
                    singletonList(Recommendation.builder()
                                    .productId(PRODUCT_ID_OK)
                                    .recommendationId(1)
                                    .author("author")
                                    .content("content")
                                    .rate(1)
                                    .serviceAddress("mock address")
                                    .build())));

    when(compositeIntegration.getReviews(PRODUCT_ID_OK))
            .thenReturn(Flux.fromIterable(singletonList(
                    Review.builder()
                            .subject("subject")
                            .author("author")
                            .content("content")
                            .serviceAddress("mock address")
                            .build())));

    when(compositeIntegration.getProduct(PRODUCT_ID_NOT_FOUND))
            .thenThrow(new NotFoundException("NOT FOUND: " + PRODUCT_ID_NOT_FOUND));

    when(compositeIntegration.getProduct(PRODUCT_ID_INVALID))
            .thenThrow(new InvalidInputException("INVALID: " + PRODUCT_ID_INVALID));
  }

  @Test
  void contextLoads() {}

  @Test
  void getProductById() {
    getAndVerifyProduct(PRODUCT_ID_OK, OK)
            .jsonPath("$.productId").isEqualTo(PRODUCT_ID_OK)
            .jsonPath("$.recommendations.length()").isEqualTo(1)
            .jsonPath("$.reviews.length()").isEqualTo(1);
  }

  @Test
  void getProductNotFound() {
    getAndVerifyProduct(PRODUCT_ID_NOT_FOUND, NOT_FOUND)
            .jsonPath("$.path").isEqualTo("/product-composite/" + PRODUCT_ID_NOT_FOUND)
            .jsonPath("$.message").isEqualTo("NOT FOUND: " + PRODUCT_ID_NOT_FOUND);
  }

  @Test
  void getProductInvalidInput() {
    getAndVerifyProduct(PRODUCT_ID_INVALID, UNPROCESSABLE_ENTITY)
            .jsonPath("$.path").isEqualTo("/product-composite/" + PRODUCT_ID_INVALID)
            .jsonPath("$.message").isEqualTo("INVALID: " + PRODUCT_ID_INVALID);
  }

//  @Test
//  void createCompositeProductWithoutRecommendationAndReviews() {
//    ProductAggregate compositeProduct = new ProductAggregate(1, "name", 1, null, null, null);
//    postAndVerifyProduct(compositeProduct, OK);
//  }
//
//  @Test
//  void createCompositeProductWithRecommendationAndReviews() {
//    ProductAggregate compositeProduct = new ProductAggregate(1, "name", 1,
//            singletonList(new RecommendationSummary(1, "a", 1, "c")),
//            singletonList(new ReviewSummary(1, "a", "s", "c")), null);
//
//    postAndVerifyProduct(compositeProduct, OK);
//  }
//
//  @Test
//  void deleteCompositeProduct() {
//    ProductAggregate compositeProduct = new ProductAggregate(1, "name", 1,
//            singletonList(new RecommendationSummary(1, "a", 1, "c")),
//            singletonList(new ReviewSummary(1, "a", "s", "c")), null);
//
//    postAndVerifyProduct(compositeProduct, OK);
//
//    deleteAndVerifyProduct(compositeProduct.getProductId(), OK);
//    // Testing that our api is idempotent
//    deleteAndVerifyProduct(compositeProduct.getProductId(), OK);
//  }

  private WebTestClient.BodyContentSpec getAndVerifyProduct(int productId, HttpStatus expectedStatus) {
    return client.get()
            .uri("/product-composite/" + productId)
            .accept(APPLICATION_JSON)
            .exchange()
            .expectStatus().isEqualTo(expectedStatus)
            .expectHeader().contentType(APPLICATION_JSON)
            .expectBody();
  }

  private void postAndVerifyProduct(ProductAggregate productAggregate, HttpStatus expectedStatus) {
    client.post()
            .uri("/product-composite")
            .body(just(productAggregate), ProductAggregate.class)
            .exchange()
            .expectStatus().isEqualTo(expectedStatus);
  }

  private void deleteAndVerifyProduct(int productId, HttpStatus expectedStatus) {
    client.delete()
            .uri("/product-composite/" + productId)
            .exchange()
            .expectStatus().isEqualTo(expectedStatus);
  }
}
