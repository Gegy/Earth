package net.gegy1000.earth.server.util.osm.tag;

public interface TagSelector {
    boolean select(String key, String value);

    static TagSelector key(String select) {
        return (key, value) -> key.equals(select);
    }

    static TagSelector baseKey(String select) {
        return (key, value) -> key.split(":")[0].equals(select);
    }

    static TagSelector value(String select) {
        return (key, value) -> value.equals(select);
    }

    static TagSelector keyValue(String selectKey, String selectValue) {
        return (key, value) -> key.equals(selectKey) && value.equals(selectValue);
    }
}
