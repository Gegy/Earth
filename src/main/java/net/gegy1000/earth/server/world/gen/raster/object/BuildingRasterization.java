package net.gegy1000.earth.server.world.gen.raster.object;

import com.vividsolutions.jts.geom.MultiPolygon;
import net.gegy1000.earth.server.util.osm.MapObject;
import net.gegy1000.earth.server.util.osm.tag.Tags;
import net.gegy1000.earth.server.world.gen.EarthGenerator;
import net.gegy1000.earth.server.world.gen.raster.ConstantRasterIds;
import net.gegy1000.earth.server.world.gen.raster.GenData;
import net.gegy1000.earth.server.world.gen.raster.adapter.BuildingAdapter;
import net.gegy1000.earth.server.world.gen.raster.object.meta.BuildingMetadata;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import java.awt.BasicStroke;
import java.awt.geom.Area;
import java.util.List;

public class BuildingRasterization implements ObjectRasterization {
    @Override
    public boolean applies(MapObject object) {
        return object.getTags().is("building", false) || object.getTags().is("building:part", false);
    }

    @Override
    public void rasterize(World world, EarthGenerator generator, MapObject object, List<GenData> data) {
        MultiPolygon polygon = object.toArea();
        if (polygon != null) {
            Tags tags = object.getTags();

            Area area = GRAPHICS.toArea(generator, polygon);

            BuildingAdapter adapter = new BuildingAdapter(generator, BuildingMetadata.parse(tags, object));

            GRAPHICS.resetStroke();
            GRAPHICS.draw(area, shape -> {
                GRAPHICS.setState(ConstantRasterIds.WALL);
                GRAPHICS.draw(shape);
            }).adapt(adapter).addTo(data);

            GRAPHICS.draw(area, shape -> {
                GRAPHICS.setState(ConstantRasterIds.SURFACE);
                GRAPHICS.fill(shape);
            }).adapt(adapter).addTo(data);

            GRAPHICS.draw(area, shape -> {
                GRAPHICS.setState(ConstantRasterIds.WINDOW);
                GRAPHICS.outline(area, length -> {
                    int windowLength = MathHelper.clamp(length / 4, 1, 4);
                    int windowSpacing = MathHelper.clamp(length - windowLength, 1, 5);
                    GRAPHICS.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[] { windowLength, windowSpacing }, length / 2 + 1));
                });
            }).adapt(adapter).addTo(data);

            GRAPHICS.resetStroke();

            GRAPHICS.draw(area, shape -> {
                GRAPHICS.setState(ConstantRasterIds.WALL_LINING);
                GRAPHICS.draw(shape);
                GRAPHICS.setState(ConstantRasterIds.PILLAR);
                GRAPHICS.drawVertices(area);
            }).adapt(adapter).addTo(data);

            GRAPHICS.setStroke(new BasicStroke(3));
            GRAPHICS.draw(area, shape -> {
                GRAPHICS.setState(ConstantRasterIds.ROOF);
                GRAPHICS.draw(shape);
            }).adapt(adapter).addTo(data);
        }
    }
}
