package mmajd.api.core.product;

import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

public interface ProductService {

  /**
   * Sample usage: "curl $HOST:$PORT/product/1".
   *
   * @param productId Id of the product
   * @return the product, if found, else null
   */
  @GetMapping(
    value = "/product/{productId}",
    produces = "application/json")
  Mono<Product> getProduct(@PathVariable int productId);

  /**
   * Sample usage, see below.
   *
   * curl -X POST $HOST:$POST/product \
   *      -H "Content-Type: application/json" \
   *      --data '{productId: 123, "name":"product123", "weight":123}'
   *
   * @param body json of the new product
   * @return new create product
   */

  @PostMapping(
          value = "/product",
          consumes = "application/json",
          produces = "application/json"
  )
  Mono<Product> createProduct(@RequestBody Product body);

  /**
   * Sample usage "curl -X DELETE $HOST:$PORT/product/1"
   *
   * @param productId of the product to be deleted
   */
  @DeleteMapping(
          value = "/product/{productId}"
  )
  Mono<Void> deleteProduct(@PathVariable int productId);
}
