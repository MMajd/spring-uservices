package mmajd.api.core.recommendation;

import org.springframework.web.bind.annotation.*;

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
  List<Recommendation> getRecommendations(
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
  Recommendation createRecommendation(@RequestBody Recommendation body);


  /**
   * Sample usage: "curl -X DELETE $HOST:$POST/recommendation?productId=1"
   *
   * @param productId of the product
   */
  @DeleteMapping(
          value = "/recommendation"
  )
  void deleteRecommendations(@RequestParam(value = "productId", required = true) int productId);

}
