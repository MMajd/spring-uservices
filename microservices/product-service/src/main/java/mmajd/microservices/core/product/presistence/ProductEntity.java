package mmajd.microservices.core.product.presistence;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection= "products")
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
@Builder
public class ProductEntity {
    @Id
    private String id;
    @Version
    private Integer version;
    @Indexed(unique = true)
    private Integer productId;
    private Integer weight;
    private String name;
}
