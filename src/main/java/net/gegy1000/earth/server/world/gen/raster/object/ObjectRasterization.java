package net.gegy1000.earth.server.world.gen.raster.object;

import net.gegy1000.earth.server.util.osm.MapObject;
import net.gegy1000.earth.server.util.osm.OSMConstants;
import net.gegy1000.earth.server.world.gen.EarthGenerator;
import net.gegy1000.earth.server.world.gen.raster.BlockGraphics;
import net.gegy1000.earth.server.world.gen.raster.ConstantRasterIds;
import net.gegy1000.earth.server.world.gen.raster.GenData;

import java.util.List;

public interface ObjectRasterization extends OSMConstants, ConstantRasterIds {
    BlockGraphics GRAPHICS = new BlockGraphics();
    HighwayRasterization HIGHWAY = new HighwayRasterization();
    BuildingRasterization BUILDING = new BuildingRasterization();
    ObjectRasterization[] RASTERIZERS = { HIGHWAY, BUILDING };

    boolean applies(MapObject object);

    void rasterize(EarthGenerator generator, MapObject object, List<GenData> data);

    static ObjectRasterization get(MapObject object) {
        for (ObjectRasterization rasterizer : RASTERIZERS) {
            if (rasterizer.applies(object)) {
                return rasterizer;
            }
        }
        return null;
    }
}
