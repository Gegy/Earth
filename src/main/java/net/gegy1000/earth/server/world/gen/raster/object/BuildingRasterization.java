package net.gegy1000.earth.server.world.gen.raster.object;

import com.vividsolutions.jts.geom.MultiPolygon;
import net.gegy1000.earth.server.util.osm.MapObject;
import net.gegy1000.earth.server.util.osm.tag.Tags;
import net.gegy1000.earth.server.world.gen.EarthGenerator;
import net.gegy1000.earth.server.world.gen.raster.GenData;
import net.gegy1000.earth.server.world.gen.raster.adapter.BuildingAdapter;
import net.gegy1000.earth.server.world.gen.raster.object.meta.BuildingMetadata;

import java.awt.geom.Area;
import java.util.List;

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
            BuildingMetadata metadata = BuildingMetadata.parse(tags, object);

            GRAPHICS.resetStroke();

            Area shape = GRAPHICS.toArea(generator, area);

            BuildingAdapter adapter = new BuildingAdapter((x, z) -> generator.provideHeight(x, z) + metadata.getMinHeight(), metadata.getLevelHeights());
            GRAPHICS.draw(shape, s -> {
                GRAPHICS.setState(WALL);
                GRAPHICS.draw(shape);
            }).adapt(adapter).addTo(data);
            GRAPHICS.draw(shape, s -> {
                GRAPHICS.setState(SURFACE);
                GRAPHICS.fill(shape);
            }).adapt(adapter).addTo(data);
            GRAPHICS.draw(shape, s -> {
                GRAPHICS.setState(WALL_LINING);
                GRAPHICS.draw(shape);
                GRAPHICS.setState(PILLAR);
                GRAPHICS.drawVertices(shape);
            }).adapt(adapter).addTo(data);
        }
    }
}
