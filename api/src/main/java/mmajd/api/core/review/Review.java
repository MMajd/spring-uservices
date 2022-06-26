package mmajd.api.core.review;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Review {
  private final int productId;
  private final int reviewId;
  private final String author;
  private final String subject;
  private final String content;
  private final String serviceAddress;
}
