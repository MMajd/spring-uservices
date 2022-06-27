package mmajd.api.composite.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(setterPrefix = "")
public class ReviewSummary {
  private int reviewId;
  private String author;
  private String subject;
  private String content;
}
