package net.gegy1000.earth.server.util.osm.object.point;

import com.vividsolutions.jts.geom.Point;
import net.gegy1000.earth.server.util.osm.WayType;
import net.gegy1000.earth.server.util.osm.object.MapObject;
import net.gegy1000.earth.server.util.osm.tag.Tags;
import net.gegy1000.earth.server.world.gen.EarthGenerator;

public abstract class Marker extends MapObject {
    protected Point point;

    public Marker(Point point, Tags tags) {
        super(tags);
        this.point = point;
    }

    @Override
    public WayType getWayType() {
        return WayType.POINT;
    }

    public static Marker build(EarthGenerator generator, Point point, Tags tags) {
        return null;
    }
}
