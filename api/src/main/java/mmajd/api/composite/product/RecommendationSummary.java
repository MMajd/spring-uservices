package mmajd.api.composite.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(setterPrefix = "")
public class RecommendationSummary {
  private int recommendationId;
  private String author;
  private int rate;
  private String content;
}
