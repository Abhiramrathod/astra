package io.astra.api.event;

import java.util.*;

public class AstraEvent {
    private final AstraEventType type;
    private final Map<String, Object> attributes;
    private final long timestamp;

    public AstraEvent(AstraEventType type, Map<String, Object> attributes) {
        this.type = type;
        this.attributes = Collections.unmodifiableMap(new LinkedHashMap<>(attributes));
        this.timestamp = System.currentTimeMillis();
    }

    public AstraEventType type() { return type; }
    public Map<String, Object> attributes() { return attributes; }
    public long timestamp() { return timestamp; }

    public static AstraEvent of(AstraEventType type, Object... keyValuePairs) {
        Map<String, Object> attrs = new LinkedHashMap<>();
        for (int i = 0; i < keyValuePairs.length; i += 2) {
            attrs.put(String.valueOf(keyValuePairs[i]), keyValuePairs[i + 1]);
        }
        return new AstraEvent(type, attrs);
    }
}
