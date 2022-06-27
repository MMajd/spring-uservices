package mmajd.microservices.core.recommendation.entity;


import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "recommendations")
@CompoundIndex(name = "prod-rec-id", unique = true,
        def = "{'productId': 1, 'recommendationId': 1}") //projection
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
@Builder(setterPrefix = "")
public class RecommendationEntity {
    @Id
    private String id;
    @Version
    private Integer version;

    private Integer productId;
    private Integer recommendationId;
    private String author;
    private Integer rating;
    private String content;
}
