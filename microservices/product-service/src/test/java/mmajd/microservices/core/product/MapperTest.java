package mmajd.microservices.core.product;

import mmajd.api.core.product.Product;
import mmajd.microservices.core.product.presistence.ProductEntity;
import mmajd.microservices.core.product.services.ProductMapper;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.junit.jupiter.api.Assertions.*;


public class MapperTest {
    private final ProductMapper mapper = Mappers.getMapper(ProductMapper.class);

    @Test
    void mapperTests() {
        assertNotNull(mapper);
        Product api = Product.builder()
                .productId(1)
                .name("p")
                .weight(1)
                .serviceAddress("xy")
                .build();

        ProductEntity entity = mapper.apiToEntity(api);

        assertEquals(api.getProductId(), entity.getProductId());
        assertEquals(api.getName(), entity.getName());
        assertEquals(api.getWeight(), entity.getWeight());

        Product api2 = mapper.entityToApi(entity);

        assertEquals(api.getProductId(), api2.getProductId());
        assertEquals(api.getName(), api2.getName());
        assertEquals(api.getWeight(), api2.getWeight());
        assertNull(api2.getServiceAddress());
    }
}
