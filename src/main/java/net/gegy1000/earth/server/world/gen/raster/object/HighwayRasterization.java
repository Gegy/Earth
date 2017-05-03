package net.gegy1000.earth.server.world.gen.raster.object;

import com.vividsolutions.jts.geom.LineString;
import net.gegy1000.earth.server.util.osm.MapObject;
import net.gegy1000.earth.server.util.osm.MapWay;
import net.gegy1000.earth.server.util.osm.tag.TagType;
import net.gegy1000.earth.server.util.osm.tag.Tags;
import net.gegy1000.earth.server.world.gen.EarthGenerator;
import net.gegy1000.earth.server.world.gen.raster.GenData;
import net.gegy1000.earth.server.world.gen.raster.adapter.RoadAdapter;
import net.minecraft.util.math.MathHelper;

import java.awt.BasicStroke;
import java.awt.geom.Path2D;
import java.util.Collection;
import java.util.List;

public class HighwayRasterization implements ObjectRasterization {
    @Override
    public boolean applies(MapObject object) {
        return object instanceof MapWay && object.getTags().is("highway");
    }

    @Override
    public void rasterize(EarthGenerator generator, MapObject object, List<GenData> data) {
        Tags tags = object.getTags();
        int lanes = tags.tag("lanes").get(TagType.INTEGER, 1);
        double defaultWidth = lanes * LANE_WIDTH;
        int width = MathHelper.ceil(MathHelper.clamp(tags.tag("width").get(TagType.DOUBLE, defaultWidth), 1, MAXIMUM_HIGHWAY_WIDTH));
        Collection<LineString> lines = object.toLines();
        GRAPHICS.setStroke(new BasicStroke(width, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        GRAPHICS.setState(SURFACE);
        RoadAdapter adapter = new RoadAdapter(generator);
        for (LineString line : lines) {
            Path2D path = GRAPHICS.toPath(generator, line);
            GRAPHICS.draw(path, GRAPHICS::draw).adapt(adapter).addTo(data);
        }
    }
}
