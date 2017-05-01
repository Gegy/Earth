package net.gegy1000.earth.server.world.gen.raster.object;

import com.vividsolutions.jts.geom.MultiPolygon;
import de.topobyte.osm4j.core.model.iface.OsmWay;
import de.topobyte.osm4j.core.model.util.OsmModelUtil;
import net.gegy1000.earth.server.util.osm.MapObject;
import net.gegy1000.earth.server.util.osm.MapRelation;
import net.gegy1000.earth.server.util.osm.tag.TagType;
import net.gegy1000.earth.server.util.osm.tag.Tags;
import net.gegy1000.earth.server.world.gen.EarthGenerator;
import net.gegy1000.earth.server.world.gen.raster.GenData;
import net.gegy1000.earth.server.world.gen.raster.adapter.BuildingLevelAdapter;
import net.gegy1000.earth.server.world.gen.raster.adapter.GenAdapter;
import net.gegy1000.earth.server.world.gen.raster.adapter.HeightExpandAdapter;
import net.gegy1000.earth.server.world.gen.raster.adapter.TerrainLevelAdapter;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.MathHelper;

import java.awt.BasicStroke;
import java.awt.geom.Area;
import java.util.List;
import java.util.Set;

public class BuildingRasterization implements ObjectRasterization {
    @Override
    public boolean applies(MapObject object) {
        return object.getTags().is("building", false) || object.getTags().is("building:part", false);
    }

    @Override
    public void rasterize(EarthGenerator generator, MapObject object, List<GenData> data) {
        MultiPolygon area = object.toArea();
        if (area != null) {
            Tags tags = object.getTags();
            int levels = tags.top("levels").get(TagType.INTEGER, 1);
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

            GRAPHICS.setBlock(Blocks.PLANKS.getDefaultState());
            GRAPHICS.setStroke(new BasicStroke(1));

            Area shape = GRAPHICS.toArea(generator, area);

            GenAdapter floorAdapter = new BuildingLevelAdapter(generator, levelHeights);
            data.add(GRAPHICS.fill(shape).adapt(floorAdapter));

            GenAdapter wallAdapter = new HeightExpandAdapter(new TerrainLevelAdapter((x, z) -> generator.provideHeight(x, z) + minHeight + 1), height);
            data.add(GRAPHICS.draw(shape).adapt(wallAdapter));

            GRAPHICS.setBlock(Blocks.LOG.getDefaultState());
            data.add(GRAPHICS.drawVertices(shape).adapt(wallAdapter));
        }
    }
}
