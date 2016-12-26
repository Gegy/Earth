package net.gegy1000.earth.server.util.osm;

import gnu.trove.list.TLongList;
import net.gegy1000.earth.server.util.osm.tag.TagSelector;

import java.util.HashMap;
import java.util.Map;

public enum WayType {
    LINE,
    AREA,
    POINT;

    public static final Map<TagSelector, WayType> TAG_TYPES = new HashMap<>();

    static {
        TAG_TYPES.put(TagSelector.baseKey("building"), AREA);
        TAG_TYPES.put(TagSelector.baseKey("area"), AREA);
        TAG_TYPES.put(TagSelector.keyValue("water", "lake"), AREA);
        TAG_TYPES.put(TagSelector.keyValue("water", "pond"), AREA);
        TAG_TYPES.put(TagSelector.keyValue("amenity", "school"), AREA);
        TAG_TYPES.put(TagSelector.keyValue("railway", "platform"), AREA);
        TAG_TYPES.put(TagSelector.keyValue("railway", "station"), AREA);
        TAG_TYPES.put(TagSelector.key("leisure"), AREA);
        TAG_TYPES.put(TagSelector.key("landuse"), AREA);

        TAG_TYPES.put(TagSelector.baseKey("line"), LINE);
        TAG_TYPES.put(TagSelector.baseKey("highway"), LINE);
        TAG_TYPES.put(TagSelector.baseKey("barrier"), LINE);
        TAG_TYPES.put(TagSelector.keyValue("water", "stream"), LINE);
        TAG_TYPES.put(TagSelector.keyValue("water", "river"), LINE);
        TAG_TYPES.put(TagSelector.keyValue("railway", "subway"), LINE);
        TAG_TYPES.put(TagSelector.keyValue("railway", "rail"), LINE);
        TAG_TYPES.put(TagSelector.key("waterway"), LINE);

        TAG_TYPES.put(TagSelector.baseKey("point"), POINT);
    }

    public static WayType get(Map<String, String> tags, TLongList nodes) {
        if (nodes != null) {
            if (nodes.size() == 1) {
                return POINT;
            } else if (nodes.get(nodes.size() - 1) != nodes.get(0)) {
                return LINE;
            }
        }
        for (Map.Entry<TagSelector, WayType> entry : TAG_TYPES.entrySet()) {
            TagSelector selector = entry.getKey();
            for (Map.Entry<String, String> tagEntry : tags.entrySet()) {
                if (selector.select(tagEntry.getKey(), tagEntry.getValue())) {
                    return entry.getValue();
                }
            }
        }
        return LINE;
    }
}
