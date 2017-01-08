package net.gegy1000.earth.server.util.osm.tag;

import java.util.HashMap;
import java.util.Map;

public class TagGroup {
    private final String group;
    private final Map<String, Tag> tags = new HashMap<>();
    private final Map<String, TagGroup> groups = new HashMap<>();

    public TagGroup(String group) {
        this.group = group;
    }

    public String getGroup() {
        return this.group;
    }

    public Map<String, Tag> getTags() {
        return this.tags;
    }

    public Map<String, TagGroup> getGroups() {
        return this.groups;
    }

    public Tag tag(String key) {
        return this.tags.get(key);
    }

    public TagGroup group(String key) {
        return this.groups.getOrDefault(key, new TagGroup(key));
    }

    @Override
    public int hashCode() {
        return this.group.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof TagGroup && ((TagGroup) obj).group.equals(this.group);
    }
}
