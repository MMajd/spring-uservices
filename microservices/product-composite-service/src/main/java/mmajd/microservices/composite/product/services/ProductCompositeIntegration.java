package mmajd.microservices.composite.product.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import mmajd.api.core.product.Product;
import mmajd.api.core.product.ProductService;
import mmajd.api.core.recommendation.Recommendation;
import mmajd.api.core.recommendation.RecommendationService;
import mmajd.api.core.review.Review;
import mmajd.api.core.review.ReviewService;
import mmajd.api.event.Event;
import mmajd.api.exceptions.InvalidInputException;
import mmajd.api.exceptions.NotFoundException;
import mmajd.util.http.HttpErrorInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static java.util.logging.Level.FINE;
import static mmajd.api.event.Event.Type.CREATE;
import static mmajd.api.event.Event.Type.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static reactor.core.publisher.Flux.empty;

@Component
public class ProductCompositeIntegration implements ProductService, RecommendationService, ReviewService {

  private static final Logger LOG = LoggerFactory.getLogger(ProductCompositeIntegration.class);

  private final WebClient webClient;
  private final ObjectMapper mapper;

  private final String productServiceUrl;
  private final String recommendationServiceUrl;
  private final String reviewServiceUrl;

  private final Scheduler publishEventScheduler;

  private final StreamBridge streamBridge;

  @Autowired
  public ProductCompositeIntegration(
          @Qualifier("publicEventScheduler") Scheduler eventScheduler,
          StreamBridge streamBridge,
          WebClient.Builder webClient,
          ObjectMapper mapper,
          @Value("${app.product-service.host}") String productServiceHost,
          @Value("${app.product-service.port}") int productServicePort,
          @Value("${app.recommendation-service.host}") String recommendationServiceHost,
          @Value("${app.recommendation-service.port}") int recommendationServicePort,
          @Value("${app.review-service.host}") String reviewServiceHost,
          @Value("${app.review-service.port}") int reviewServicePort) {

    this.publishEventScheduler = eventScheduler;
    this.streamBridge = streamBridge;
    this.webClient = webClient.build();
    this.mapper = mapper;

    productServiceUrl = "http://" + productServiceHost + ":" + productServicePort; 
    recommendationServiceUrl = "http://" + recommendationServiceHost + ":" + recommendationServicePort;
    reviewServiceUrl = "http://" + reviewServiceHost + ":" + reviewServicePort;
  }

  public Mono<Product> getProduct(int productId) {
    String url = productServiceUrl + "/product/" + productId;
    LOG.debug("Will call getProduct API on URL: {}", url);

    return webClient.get()
            .uri(url)
            .retrieve()
            .bodyToMono(Product.class)
            .log(LOG.getName(), FINE)
            .onErrorMap(WebClientResponseException.class, this::handleException);
  }


  @Override
  public Mono<Product> createProduct(Product body) {
    return Mono.fromCallable(() -> {
      sendMessage("products-out-0", new Event<Integer, Product>(CREATE, body.getProductId(), body));
      return body;
    }).log(LOG.getName(), FINE).subscribeOn(publishEventScheduler);
  }


  @Override
  public Mono<Void> deleteProduct(int productId) {
    String url = productServiceUrl + "/product/" + productId;
    LOG.debug("Will delete product with id: {}", productId);

    return Mono
            .fromRunnable(() ->
                    sendMessage("products-out-0", new Event<>(DELETE, productId, null) ))
            .subscribeOn(publishEventScheduler)
            .then();
  }

  public Flux<Recommendation> getRecommendations(int productId) {
      String url = recommendationServiceUrl + "/recommendation?productId=" + productId;
      LOG.debug("Will call getRecommendations API on URL: {}", url);

      return webClient
              .get()
              .uri(url)
              .retrieve()
              .bodyToFlux(Recommendation.class)
              .log(LOG.getName(), FINE)
              .onErrorResume(e -> empty());
  }

  @Override
  public Mono<Recommendation> createRecommendation(Recommendation body) {
      LOG.debug("Will post new recommendation on through message broker");

      return Mono.fromCallable(() -> {
        sendMessage("recommendations-out-0",
                new Event<Integer, Recommendation>(CREATE, body.getProductId(), body));
        return body;
      }).subscribeOn(publishEventScheduler);
  }

  @Override
  public Mono<Void> deleteRecommendations(int productId) {
      LOG.debug("Will delete recommendation with product id: {}, through message broker", productId);

      return Mono.fromRunnable(() -> sendMessage("recommendations-out-0", new Event<>(DELETE, productId, null))
      ).subscribeOn(publishEventScheduler).then();
  }

  public Flux<Review> getReviews(int productId) {
    String url = reviewServiceUrl + "/review?productId=" + productId;
    LOG.debug("Will call getReviews API on URL: {}", url);

    return webClient.get().uri(url).retrieve()
            .bodyToFlux(Review.class)
            .log(LOG.getName(), FINE)
            .onErrorResume(e -> empty());
  }

  @Override
  public Mono<Review> createReview(Review body) {
    return Mono.fromCallable(() -> {
      sendMessage("reviews-out-0", new Event<Integer, Review>(CREATE, body.getProductId(), body));
      return body;
    }).subscribeOn(publishEventScheduler);
  }

  @Override
  public Mono<Void> deleteReviews(int productId) {
    LOG.debug("Will delete review with product id: {}, through message broker", productId);

    return Mono
            .fromRunnable(() ->
                    sendMessage("reviews-out-0", new Event<>(DELETE, productId, null)))
            .subscribeOn(publishEventScheduler).then();
  }

  private Throwable handleException(WebClientResponseException ex) {
    if (!(ex instanceof WebClientResponseException)) {
      LOG.warn("Got a unexpected error: {}, will rethrow it", ex.toString());
      return ex;
    }

    WebClientResponseException webclientEx = (WebClientResponseException)ex;

    switch (webclientEx.getStatusCode()) {
      case NOT_FOUND:
        return new NotFoundException(getErrorMessage(webclientEx));
      case UNPROCESSABLE_ENTITY:
        return new InvalidInputException(getErrorMessage(webclientEx));
      default:
        LOG.warn("Got an unexpected HTTP error: {}, will rethrow it", webclientEx.getStatusCode());
        LOG.warn("Error body: {}", webclientEx.getResponseBodyAsString());
        return ex; }
  }

  private <K, V> void sendMessage(String topic, Event<K, V> event) {
    LOG.debug("Sending a {} message to {}", event.getEventType(), topic);

    Message<Event<K, V>> message = MessageBuilder.withPayload(event)
            .setHeader("partitionKey", event.getKey())
            .build();


    LOG.debug("message is : {}, on topic: {}", message.getPayload().toString(), topic);

    streamBridge.send(topic, message);
  }

  private String getErrorMessage(WebClientResponseException ex) {
    try {
      return mapper.readValue(ex.getResponseBodyAsString(), HttpErrorInfo.class).getMessage();
    } catch (IOException _IOex) {
      return ex.getMessage();
    }
  }

  private Mono<Health> getHealth(String url) {
    url += "/actuator/health";
    LOG.debug("Will call the Health API on URL: {}", url);
    return webClient.get().uri(url).retrieve().bodyToMono(String.class)
            .map(s -> new Health.Builder().up().build())
            .onErrorResume(ex -> Mono.just(new Health.Builder().down(ex).build()))
            .log(LOG.getName(), FINE);
  }

  public Mono<Health> getProductHealth() {
    return getHealth(productServiceUrl);
  }


  public Mono<Health> getRecommendationHealth() {
    return getHealth(recommendationServiceUrl);
  }

  public Mono<Health> getReviewHealth() {
    return getHealth(reviewServiceUrl);
  }
}
