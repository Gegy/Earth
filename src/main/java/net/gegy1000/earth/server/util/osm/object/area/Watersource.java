package net.gegy1000.earth.server.util.osm.object.area;

import com.vividsolutions.jts.geom.Geometry;
import net.gegy1000.earth.server.util.osm.MapBlockAccess;
import net.gegy1000.earth.server.util.osm.object.MapObjectType;
import net.gegy1000.earth.server.util.osm.tag.Tags;
import net.gegy1000.earth.server.util.raster.Rasterize;
import net.gegy1000.earth.server.world.gen.EarthGenerator;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;

import java.util.Set;

public class Watersource extends Area {
    public Watersource(Geometry geometry, Tags tags) {
        super(geometry, tags);
    }

    @Override
    public void generate(EarthGenerator generator, MapBlockAccess storage, int pass) {
        if (pass == 0) {
            Set<BlockPos> rasterizedArea = Rasterize.area(generator, this.geometry);
            for (BlockPos pos : rasterizedArea) {
                int height = generator.getGenerationHeight(pos.getX(), pos.getZ());
                storage.set(pos.up(height), Blocks.WATER.getDefaultState());
            }
        }
    }

    @Override
    public MapObjectType getType() {
        return MapObjectType.WATER_SOURCE;
    }
}
