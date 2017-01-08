package net.gegy1000.earth.server.util.osm.object.area;

import com.vividsolutions.jts.geom.Geometry;
import net.gegy1000.earth.server.util.osm.MapBlockAccess;
import net.gegy1000.earth.server.util.osm.object.MapObjectType;
import net.gegy1000.earth.server.util.osm.tag.TagType;
import net.gegy1000.earth.server.util.osm.tag.Tags;
import net.gegy1000.earth.server.util.raster.Rasterize;
import net.gegy1000.earth.server.world.gen.EarthGenerator;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;

import java.util.Set;

public class SwimmingPool extends Area {
    private int depth;

    public SwimmingPool(EarthGenerator generator, Geometry geometry, Tags tags) {
        super(geometry, tags);
        this.depth = (int) (tags.top("depth").get(TagType.DOUBLE, 3.0) / generator.getScaleRatio());
    }

    @Override
    public void generate(EarthGenerator generator, MapBlockAccess storage, int pass) {
        if (pass == 0) {
            Rasterize.AreaWithOutline areaOutline = Rasterize.areaOutline(generator, this.geometry);
            Set<BlockPos> rasterizedArea = areaOutline.getArea();
            int bottom = this.depth + 1;
            for (BlockPos pos : rasterizedArea) {
                int height = generator.getGenerationHeight(pos.getX(), pos.getZ());
                storage.fillY(pos.up(height - this.depth), Blocks.WATER.getDefaultState(), bottom);
                storage.set(pos.up(height - bottom), Blocks.QUARTZ_BLOCK.getDefaultState());
            }
            Set<BlockPos> rasterizedOutline = areaOutline.getOutline();
            for (BlockPos pos : rasterizedOutline) {
                int height = generator.getGenerationHeight(pos.getX(), pos.getZ());
                storage.fillY(pos.up(height - this.depth), Blocks.QUARTZ_BLOCK.getDefaultState(), bottom);
            }
        }
    }

    @Override
    public MapObjectType getType() {
        return MapObjectType.WATER_SOURCE;
    }
}
