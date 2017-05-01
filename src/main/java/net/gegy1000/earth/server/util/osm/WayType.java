package net.gegy1000.earth.server.util.osm;

import gnu.trove.list.TLongList;
import net.gegy1000.earth.server.util.osm.tag.TagSelector;
import net.gegy1000.earth.server.util.osm.tag.Tags;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public enum WayType {
    POINT("point"),
    LINE("line"),
    AREA("area");

    private String name;

    WayType(String name) {
        this.name = name;
    }

    public static final List<TagSelector> LINE_SELECTORS = new ArrayList<>();

    static {
        LINE_SELECTORS.add(TagSelector.baseKey("highway"));
        LINE_SELECTORS.add(TagSelector.baseKey("barrier"));
        LINE_SELECTORS.add(TagSelector.keyValue("water", "stream"));
        LINE_SELECTORS.add(TagSelector.keyValue("water", "river"));
        LINE_SELECTORS.add(TagSelector.keyValue("railway", "subway"));
        LINE_SELECTORS.add(TagSelector.keyValue("railway", "rail"));
        LINE_SELECTORS.add((key, value) -> key.equals("waterway") && !value.equals("riverbank"));
    }

    public static WayType get(Tags tags, TLongList nodes) {
        for (WayType type : WayType.values()) {
            if (tags.is(type.name, true)) {
                return type;
            }
        }
        if (nodes != null) {
            if (nodes.size() == 1) {
                return POINT;
            } else if (nodes.get(nodes.size() - 1) != nodes.get(0)) {
                return LINE;
            }
        } else {
            return POINT;
        }
        for (TagSelector selector : LINE_SELECTORS) {
            for (Map.Entry<String, String> tagEntry : tags.all().entrySet()) {
                if (selector.select(tagEntry.getKey(), tagEntry.getValue())) {
                    return WayType.LINE;
                }
            }
        }
        return AREA;
    }
}
