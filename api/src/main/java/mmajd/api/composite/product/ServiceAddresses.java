package mmajd.api.composite.product;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceAddresses {
  private String cmp;
  private String pro;
  private String rev;
  private String rec;
}
