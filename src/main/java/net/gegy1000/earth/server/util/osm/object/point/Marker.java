package net.gegy1000.earth.server.util.osm.object.point;

import com.vividsolutions.jts.geom.Point;
import net.gegy1000.earth.server.util.osm.object.MapObject;
import net.gegy1000.earth.server.world.gen.EarthGenerator;

import java.util.Map;

public abstract class Marker extends MapObject {
    protected Point point;

    public Marker(Point point, Map<String, String> tags) {
        super(tags);
        this.point = point;
    }

    public static Marker build(EarthGenerator generator, Point point, Map<String, String> tags) {
        return null;
    }
}
