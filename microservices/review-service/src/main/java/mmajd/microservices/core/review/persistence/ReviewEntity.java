package mmajd.microservices.core.review.persistence;

import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "reviews", indexes = {
        @Index(name = "views_unique_idx",
                unique = true, columnList = "productId,reviewId")
})
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder(setterPrefix = "")
public class ReviewEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Version
    private Long version;

    private Integer productId;
    private Integer reviewId;
    private String author;
    private String content;
    private String subject;
}
