package net.gegy1000.earth.server.util.osm.tag;

import java.util.HashMap;
import java.util.Map;

public class Tags {
    private final Map<String, String> allTags;
    private final Map<String, Tag> tags = new HashMap<>();
    private final Map<String, TagGroup> tagGroups = new HashMap<>();

    private Tags(Map<String, String> tags) {
        this.allTags = tags;
        for (Map.Entry<String, String> entry : tags.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (key.contains(":")) {
                String[] keyGroups = key.split(":");
                TagGroup lastGroup = this.tagGroups.computeIfAbsent(keyGroups[0], group -> new TagGroup(keyGroups[0]));
                for (int i = 1; i < keyGroups.length - 1; i++) {
                    String keyGroup = keyGroups[i];
                    TagGroup group = this.tagGroups.computeIfAbsent(keyGroup, groupKey -> new TagGroup(keyGroup));
                    lastGroup.getGroups().putIfAbsent(keyGroup, group);
                    lastGroup = group;
                }
                String groupedKey = keyGroups[keyGroups.length - 1];
                lastGroup.getTags().put(groupedKey, new Tag(groupedKey, value));
            } else {
                this.tags.put(key, new Tag(key, value));
            }
        }
    }

    public static Tags from(Map<String, String> tags) {
        return new Tags(tags);
    }

    public Tag top(String key) {
        for (Map.Entry<String, String> entry : this.allTags.entrySet()) {
            String[] split = entry.getKey().split(":");
            if (split[split.length - 1].equals(key)) {
                return new Tag(entry.getKey(), entry.getValue());
            }
        }
        return new Tag(key, null);
    }

    public Tag full(String key) {
        String value = this.allTags.get(key);
        if (value != null) {
            return new Tag(key, value);
        }
        return new Tag(key, null);
    }

    public Tag tag(String key) {
        return this.tags.getOrDefault(key, new Tag(key, null));
    }

    public TagGroup group(String key) {
        return this.tagGroups.getOrDefault(key, new TagGroup(key));
    }

    public String get(String key) {
        return this.tag(key).getValue();
    }

    public Map<String, String> all() {
        return this.allTags;
    }

    public boolean is(String key, boolean base) {
        return (base ? this.tag(key) : this.full(key)).getValue() != null;
    }

    public boolean is(String key) {
        return this.tags.containsKey(key) && this.tags.get(key).getValue() != null;
    }

    public boolean is(String key, String value) {
        String tagValue = this.tag(key).getValue();
        return tagValue != null && tagValue.equals(value);
    }
}
