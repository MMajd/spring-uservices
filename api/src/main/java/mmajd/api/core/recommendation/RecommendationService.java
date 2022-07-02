package mmajd.api.core.recommendation;

import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface RecommendationService {

  /**
   * Sample usage: "curl $HOST:$PORT/recommendation?productId=1".
   *
   * @param productId of the product
   * @return the recommendations of the product
   */
  @GetMapping(
    value = "/recommendation",
    produces = "application/json")
  Flux<Recommendation> getRecommendations(
    @RequestParam(value = "productId", required = true) int productId);

  /**
   * Sample usage, see below.
   *
   * curl -X POST $HOST:$POST/recommendation \
   *      -H "Content-Type: application/json" \
   *      --data '{"productId": 123, "recommendationId": 456, "author": "x", "rate": 5, "content":"x, y, z"}'
   *
   * @param body json of the new recommendation
   * @return newly created recommendation
   */
  @PostMapping(
          value = "/recommendation",
          consumes = "application/json",
          produces = "application/json"
  )
  Mono<Recommendation> createRecommendation(@RequestBody Recommendation body);


  /**
   * Sample usage: "curl -X DELETE $HOST:$POST/recommendation?productId=1"
   *
   * @param productId of the product
   */
  @DeleteMapping(
          value = "/recommendation"
  )
  Mono<Void> deleteRecommendations(@RequestParam(value = "productId", required = true) int productId);

}
