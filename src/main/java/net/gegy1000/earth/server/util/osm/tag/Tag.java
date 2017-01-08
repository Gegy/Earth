package net.gegy1000.earth.server.util.osm.tag;

public class Tag {
    private final String key;
    private final String value;

    public Tag(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return this.key;
    }

    public String getValue() {
        return this.value;
    }

    public <T> T get(TagType<T> type, T defaultValue) {
        if (this.value == null) {
            return defaultValue;
        }
        T parsed;
        try {
            parsed = type.parse(this.value);
        } catch (Exception e) {
            return defaultValue;
        }
        if (parsed == null) {
            return defaultValue;
        }
        return parsed;
    }

    @Override
    public int hashCode() {
        return this.key.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Tag && ((Tag) obj).key.equals(this.key);
    }
}
