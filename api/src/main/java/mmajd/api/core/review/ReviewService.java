package mmajd.api.core.review;

import java.util.List;

import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ReviewService {

  /**
   * Sample usage: "curl $HOST:$PORT/review?productId=1".
   *
   * @param productId of the product
   * @return the reviews of the product
   */
  @GetMapping(
          value = "/review",
          produces = "application/json")
  Flux<Review> getReviews(@RequestParam(value = "productId", required = true) int productId);


  /*
   * Sample usage, see below.
   *
   * curl -X POST $HOST:$PORT/review \
   *   -H "Content-Type: application/json" --data \
   *   '{"productId":123,"reviewId":456,"author":"x","subject":"x, y, x","content":"x, y, z"}'
   *
   * @param body json of the new review
   * @return newly created review
   */
  @PostMapping(
          value    = "/review",
          consumes = "application/json",
          produces = "application/json")
  Mono<Review> createReview(@RequestBody Review body);

  /**
   18a34,41

   /**
   * Sample usage: "curl -X DELETE $HOST:$PORT/review?productId=1".
   *
   * @param productId of the product
   */
  @DeleteMapping(value = "/review")
  Mono<Void> deleteReviews(@RequestParam(value = "productId", required = true)  int productId);
}
