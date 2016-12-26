package net.gegy1000.earth.server.util.osm.object.area;

import com.vividsolutions.jts.geom.Geometry;
import net.gegy1000.earth.server.util.osm.object.MapObject;
import net.gegy1000.earth.server.util.osm.tag.TagHandler;
import net.gegy1000.earth.server.world.gen.EarthGenerator;

import java.util.Map;

public abstract class Area extends MapObject {
    protected Geometry geometry;

    public Area(Geometry geometry, Map<String, String> tags) {
        super(tags);
        this.geometry = geometry;
    }

    public static Area build(EarthGenerator generator, Geometry geometry, Map<String, String> tags) {
        if (TagHandler.is(tags, "building", false)) {
            return new Building(generator, geometry, tags);
        } else if (TagHandler.is(tags, "building:part", false)) {
            return new BuildingPart(generator, geometry, tags);
        } else if (TagHandler.is(tags, "leisure", false)) {
            String leisure = tags.get("leisure");
            if (leisure.equals("park") || leisure.equals("playground")) {
                return new Park(geometry, tags);
            } else if (leisure.equals("garden")) {
                return new Garden(geometry, tags);
            }
        } else if (TagHandler.is(tags, "water", false)) {
            String water = tags.get("water");
            if (water.equals("pond") || water.equals("lake")) {
                return new Lake(geometry, tags);
            }
        } else if (TagHandler.is(tags, "landuse", false)) {
            String landuse = tags.get("landuse");
            if (landuse.equals("pond") || landuse.equals("lake")) {
                return new Lake(geometry, tags);
            } else if (landuse.equals("park") || landuse.equals("playground")) {
                return new Park(geometry, tags);
            }
        }
        return null;
    }
}
