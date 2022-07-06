package mmajd.microservices.composite.product;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import mmajd.api.event.Event;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class IsSameEvent extends TypeSafeMatcher<String> {
    private static final Logger LOG = LoggerFactory.getLogger(IsSameEvent.class);
    private final ObjectMapper mapper = new ObjectMapper();

    private Event expectedEvent;

    public IsSameEvent(Event expectedEvent) {
        this.expectedEvent = expectedEvent;
    }

    @Override
    protected boolean matchesSafely(String eventJson) {
        if (expectedEvent == null) {
            return false;
        }

        LOG.trace("Convert the following json string to a map: {}", eventJson);

        Map<String, Object> eventMap = convertJsonStringToMap(eventJson);
        eventMap.remove("createdAt");

        Map<String, Object> expectedEventMap = getMapWithoutCreatedAt(expectedEvent);

        LOG.trace("Got the map: {}", eventMap);
        LOG.trace("Compare to the expected map: {}", expectedEventMap);

        return eventMap.equals(expectedEventMap);
    }


    @Override
    public void describeTo(Description description) {
        String expectedJson = covertObjectToJsonString(expectedEvent);
        description.appendText("expected to look like " + expectedJson);
    }

    private String covertObjectToJsonString(Object o) {
        try {
            return mapper.writeValueAsString(o);
        } catch (JsonProcessingException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static Matcher<String> sameEventExceptCreatedAt(Event expectedEvent) {
        return new IsSameEvent(expectedEvent);
    }


    private Map<String, Object> convertObjectToMap(Object object) {
        JsonNode node = mapper.convertValue(object, JsonNode.class);
        return mapper.convertValue(node, new TypeReference<Map<String, Object>>(){});
    }

    private Map<String, Object> getMapWithoutCreatedAt(Event event) {
        Map<String, Object> eventMap = convertObjectToMap(event);
        eventMap.remove("createdAt");
        return eventMap;
    }

    private Map<String, Object> convertJsonStringToMap(String eventJson) {
        try {
            return mapper.readValue(eventJson, new TypeReference<HashMap<String, Object>>(){});
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

}
