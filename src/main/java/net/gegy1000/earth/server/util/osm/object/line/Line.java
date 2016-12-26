package net.gegy1000.earth.server.util.osm.object.line;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;
import net.gegy1000.earth.server.util.osm.object.MapObject;
import net.gegy1000.earth.server.util.osm.object.line.highway.Highway;
import net.gegy1000.earth.server.util.osm.tag.TagHandler;
import net.gegy1000.earth.server.util.osm.tag.TagType;
import net.gegy1000.earth.server.world.gen.EarthGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class Line extends MapObject {
    protected LineString line;
    protected boolean bridge;

    public Line(LineString line, Map<String, String> tags) {
        super(tags);
        this.line = line;
        this.bridge = TagHandler.getFull(TagType.BOOLEAN, tags, false, "bridge");
    }

    protected List<Coordinate> toQuad(Coordinate start, Coordinate end, double width) {
        double deltaX = end.x - start.x;
        double deltaZ = end.y - start.y;
        double length = Math.sqrt((deltaX * deltaX) + (deltaZ * deltaZ));
        double offsetX = (width * deltaZ / length) / 2;
        double offsetZ = (width * deltaX / length) / 2;
        List<Coordinate> points = new ArrayList<>();
        points.add(new Coordinate(Math.round(start.x - offsetX), Math.round(start.y + offsetZ)));
        points.add(new Coordinate(Math.round(start.x + offsetX), Math.round(start.y - offsetZ)));
        points.add(new Coordinate(Math.round(end.x - offsetX), Math.round(end.y + offsetZ)));
        points.add(new Coordinate(Math.round(end.x + offsetX), Math.round(end.y - offsetZ)));
        return points;
    }

    public static Line build(EarthGenerator generator, LineString line, Map<String, String> tags) {
        if (TagHandler.is(tags, "highway", false)) {
            Highway highway = Highway.get(generator, line, tags);
            if (highway != null) {
                return highway;
            }
        }
        if (TagHandler.is(tags, "barrier", false)) {
            return new Barrier(generator, line, tags);
        }
        if (TagHandler.is(tags, "railway", false)) {
            return new Railway(generator, line, tags);
        }
        if (TagHandler.is(tags, "waterway", false)) {
            String waterway = tags.get("waterway");
            if (waterway.equals("river") || waterway.equals("stream") || waterway.equals("canal")) {
                return new River(generator, line, tags);
            }
        }
        if (TagHandler.is(tags, "water", false)) {
            String water = tags.get("water");
            if (water.equals("river") || water.equals("stream") || water.equals("canal")) {
                return new River(generator, line, tags);
            }
        }
        return null;
    }
}
