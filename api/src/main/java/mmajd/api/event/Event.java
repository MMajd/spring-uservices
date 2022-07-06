package mmajd.api.event;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.ZonedDateTimeSerializer;
import lombok.Getter;
import lombok.ToString;

import java.time.ZonedDateTime;

@Getter
@ToString
public class Event <K, D> {

    public enum Type {
        CREATE,
        DELETE
    }


    private final Type eventType;
    private final K key;
    private final D data;
    private final ZonedDateTime createdAt;

    public Event(Type create) {
        eventType = null;
        key = null;
        data = null;
        createdAt = null;
    }

    public Event(Type eventType, K key, D data) {
        this.eventType = eventType;
        this.key = key;
        this.data = data;
        this.createdAt = ZonedDateTime.now();
    }

    @JsonSerialize(using = ZonedDateTimeSerializer.class)
    public ZonedDateTime getCreatedAt() {
        return createdAt;
    }
}