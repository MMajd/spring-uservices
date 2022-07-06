package mmajd.api.core.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(setterPrefix = "")
public class Product {
  private int productId;
  private String name;
  private int weight;
  private String serviceAddress;
}
