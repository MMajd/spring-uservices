package mmajd.api.core.recommendation;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Recommendation {
  private final int productId;
  private final int recommendationId;
  private final String author;
  private final int rate;
  private final String content;
  private final String serviceAddress;
}
