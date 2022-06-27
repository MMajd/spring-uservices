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
  private int weight;
  private String name;
  private String serviceAddress;
}
