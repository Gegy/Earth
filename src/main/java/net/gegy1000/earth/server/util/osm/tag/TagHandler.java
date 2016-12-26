package net.gegy1000.earth.server.util.osm.tag;

import java.util.Map;

public class TagHandler {
    public static boolean is(Map<String, String> tags, String key, boolean base) {
        for (Map.Entry<String, String> tagEntry : tags.entrySet()) {
            String baseTag = base ? getBaseTag(tagEntry.getKey()) : tagEntry.getKey();
            if (baseTag.equalsIgnoreCase(key)) {
                return true;
            }
        }
        return false;
    }

    public static String getBaseTag(String tag) {
        return tag.split(":")[0];
    }

    public static String getTopTag(String tag) {
        String[] split = tag.split(":");
        return split[split.length - 1];
    }

    public static <T> T parse(String value, TagType<T> type) {
        try {
            return type.parse(value);
        } catch (Exception e) {
            return null;
        }
    }

    public static <T> T getTop(TagType<T> type, Map<String, String> tags, T defaultValue, String... keys) {
        for (String key : keys) {
            for (Map.Entry<String, String> tagEntry : tags.entrySet()) {
                String topKey = TagHandler.getTopTag(tagEntry.getKey());
                if (topKey.equalsIgnoreCase(key)) {
                    T parsed = TagHandler.parse(tagEntry.getValue(), type);
                    if (parsed != null) {
                        return parsed;
                    }
                }
            }
        }
        return defaultValue;
    }

    public static <T> T getBase(TagType<T> type, Map<String, String> tags, T defaultValue, String... keys) {
        for (String key : keys) {
            for (Map.Entry<String, String> tagEntry : tags.entrySet()) {
                String baseKey = TagHandler.getBaseTag(tagEntry.getKey());
                if (baseKey.equalsIgnoreCase(key)) {
                    T parsed = TagHandler.parse(tagEntry.getValue(), type);
                    if (parsed != null) {
                        return parsed;
                    }
                }
            }
        }
        return defaultValue;
    }

    public static <T> T getFull(TagType<T> type, Map<String, String> tags, T defaultValue, String... keys) {
        for (String key : keys) {
            String value = tags.get(key);
            if (value != null) {
                T parsed = TagHandler.parse(value, type);
                if (parsed != null) {
                    return parsed;
                }
            }
        }
        return defaultValue;
    }
}
