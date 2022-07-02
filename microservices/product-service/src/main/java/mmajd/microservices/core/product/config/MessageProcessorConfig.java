package mmajd.microservices.core.product.config;

import mmajd.api.core.product.Product;
import mmajd.api.core.product.ProductService;
import mmajd.api.event.Event;
import mmajd.api.exceptions.EventProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

@Configuration
public class MessageProcessorConfig {
    private static final Logger LOG = LoggerFactory.getLogger(MessageProcessorConfig.class);

    private final ProductService productService;

    @Autowired
    public MessageProcessorConfig(ProductService productService) {
        this.productService = productService;
    }

    //TODO: switch to event store and saga pattern later
    @Bean
    public Consumer<Event<Integer, Product>> messageProcessor() {
        return event -> {
            LOG.info("Process errMsg created at {}...", event.getCreatedAt());

            switch(event.getEventType()) {
                case CREATE: {
                    Product product = event.getData();
                    LOG.info("Create product with ID: {}", product.getProductId());
                    productService.createProduct(product).block(); // not best practise
                }
                break;

                case DELETE: {
                    int productId = event.getKey();
                    LOG.info("Delete recommendations with ProductID: {}", productId);
                    productService.deleteProduct(productId).block();
                }
                break;
                default: {
                    String errMsg = String.format("Incorrect event type: %s, expected a CREATED or DELETED", event.getEventType());
                    LOG.warn(errMsg);
                    throw new EventProcessingException(errMsg);
                }
            }
        };
    }
}
