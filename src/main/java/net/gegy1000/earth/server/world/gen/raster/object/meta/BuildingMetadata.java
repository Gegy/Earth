package net.gegy1000.earth.server.world.gen.raster.object.meta;

import de.topobyte.osm4j.core.model.iface.OsmWay;
import de.topobyte.osm4j.core.model.util.OsmModelUtil;
import net.gegy1000.earth.server.util.osm.MapObject;
import net.gegy1000.earth.server.util.osm.MapRelation;
import net.gegy1000.earth.server.util.osm.OSMConstants;
import net.gegy1000.earth.server.util.osm.tag.TagType;
import net.gegy1000.earth.server.util.osm.tag.Tags;
import net.minecraft.util.math.MathHelper;

import java.util.Set;

public class BuildingMetadata implements OSMConstants {
    private final int levels;
    private final int minLevel;
    private final int height;
    private final int minHeight;
    private final int[] levelHeights;

    public BuildingMetadata(int levels, int minLevel, int height, int minHeight, int[] levelHeights) {
        this.levels = levels;
        this.minLevel = minLevel;
        this.height = height;
        this.minHeight = minHeight;
        this.levelHeights = levelHeights;
    }

    public static BuildingMetadata parse(Tags tags, MapObject object) {
        int levels = Math.max(0, tags.top("levels").get(TagType.INTEGER, 1));
        int minLevel = tags.top("min_level").get(TagType.INTEGER, 0);
        double defaultHeight = levels * LEVEL_HEIGHT;
        int height = MathHelper.ceil(MathHelper.clamp(tags.top("height").get(TagType.DOUBLE, defaultHeight), LEVEL_HEIGHT, 255));
        int minHeight = MathHelper.ceil(tags.top("min_height").get(TagType.DOUBLE, (double) minLevel * LEVEL_HEIGHT));
        int[] levelHeights = new int[levels + 1];
        double levelHeight = (double) height / levels;
        double currentHeight = minHeight;
        for (int i = 0; i < levels; i++) {
            levelHeights[i] = (int) currentHeight;
            currentHeight += levelHeight;
        }
        if (object instanceof MapRelation) {
            MapRelation relation = (MapRelation) object;
            Set<OsmWay> ways = relation.getWays();
            for (OsmWay way : ways) {
                Tags relationTags = Tags.from(OsmModelUtil.getTagsAsMap(way));
                if (relationTags.is("min_level")) {
                    int minRelationLevel = relationTags.top("min_level").get(TagType.INTEGER, 0);
                    int minRelationHeight = MathHelper.ceil(relationTags.top("min_height").get(TagType.DOUBLE, (double) minRelationLevel * LEVEL_HEIGHT));
                    levelHeights[minRelationLevel] = minRelationHeight;
                }
            }
        }
        levelHeights[levelHeights.length - 1] = minHeight + height;
        return new BuildingMetadata(levels, minLevel, height, minHeight, levelHeights);
    }

    public int getLevels() {
        return this.levels;
    }

    public int getMinLevel() {
        return this.minLevel;
    }

    public int getHeight() {
        return this.height;
    }

    public int getMinHeight() {
        return this.minHeight;
    }

    public int[] getLevelHeights() {
        return this.levelHeights;
    }
}
