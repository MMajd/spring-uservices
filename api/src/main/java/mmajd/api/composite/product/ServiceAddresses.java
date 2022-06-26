package mmajd.api.composite.product;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ServiceAddresses {
  private final String cmp;
  private final String pro;
  private final String rev;
  private final String rec;
}
