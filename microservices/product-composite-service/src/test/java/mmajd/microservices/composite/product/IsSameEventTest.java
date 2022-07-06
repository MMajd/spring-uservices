package mmajd.microservices.composite.product;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import mmajd.api.core.product.Product;
import mmajd.api.event.Event;
import org.junit.jupiter.api.Test;

import static mmajd.api.event.Event.Type.CREATE;
import static mmajd.api.event.Event.Type.DELETE;
import static mmajd.microservices.composite.product.IsSameEvent.sameEventExceptCreatedAt;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

public class IsSameEventTest {

    ObjectMapper mapper = new ObjectMapper();

    // Event #1 and #2 are the same event, but occurs as different times
    // Event #3 and #4 are different events

    @Test
    void testEventObjectCompare() throws JsonProcessingException {
        Event<Integer, Product> event1 = new Event<>(CREATE, 1, Product.builder()
                .productId(1)
                .name("name")
                .weight(1)
                .build()
        );

        Event<Integer, Product> event2 = new Event<>(CREATE, 1, Product.builder()
                .productId(1)
                .name("name")
                .weight(1)
                .build()
        );

        Event<Integer, Product> event3 = new Event<>(DELETE, 1, null);

        Event<Integer, Product> event4 = new Event<>(CREATE, 1, Product.builder()
                .productId(2)
                .name("name")
                .weight(1)
                .build()
        );

        String event1Json = mapper.writeValueAsString(event1);

        assertThat(event1Json, is(sameEventExceptCreatedAt(event2)));
        assertThat(event1Json, not(sameEventExceptCreatedAt(event3)));
        assertThat(event1Json, not(sameEventExceptCreatedAt(event4)));
    }
}
