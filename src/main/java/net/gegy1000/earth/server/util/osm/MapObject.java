package net.gegy1000.earth.server.util.osm;

import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import net.gegy1000.earth.server.util.osm.tag.Tags;

import java.util.Collection;
import java.util.List;

public interface MapObject {
    Tags getTags();

    int getLayer();

    Collection<LineString> toLines();

    MultiPolygon toArea();

    List<Point> toPoints();
}
