package net.gegy1000.earth.server.world.gen.raster.object;

import net.gegy1000.earth.server.util.osm.MapObject;
import net.gegy1000.earth.server.world.gen.EarthGenerator;
import net.gegy1000.earth.server.world.gen.raster.BlockGraphics;
import net.gegy1000.earth.server.world.gen.raster.GenData;
import net.minecraft.world.World;

import java.util.List;

public interface ObjectRasterization {
    BlockGraphics GRAPHICS = new BlockGraphics();
    HighwayRasterization HIGHWAY = new HighwayRasterization();
    BuildingRasterization BUILDING = new BuildingRasterization();
    ObjectRasterization[] RASTERIZERS = { HIGHWAY, BUILDING };

    boolean applies(MapObject object);

    void rasterize(World world, EarthGenerator generator, MapObject object, List<GenData> data);

    static ObjectRasterization get(MapObject object) {
        for (ObjectRasterization rasterizer : RASTERIZERS) {
            if (rasterizer.applies(object)) {
                return rasterizer;
            }
        }
        return null;
    }
}
