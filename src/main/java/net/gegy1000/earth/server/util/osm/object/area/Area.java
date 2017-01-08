package net.gegy1000.earth.server.util.osm.object.area;

import com.vividsolutions.jts.geom.Geometry;
import net.gegy1000.earth.server.util.osm.WayType;
import net.gegy1000.earth.server.util.osm.object.MapObject;
import net.gegy1000.earth.server.util.osm.tag.Tags;
import net.gegy1000.earth.server.world.gen.EarthGenerator;

public abstract class Area extends MapObject {
    protected Geometry geometry;

    public Area(Geometry geometry, Tags tags) {
        super(tags);
        this.geometry = geometry;
    }

    @Override
    public WayType getWayType() {
        return WayType.AREA;
    }

    public static Area build(EarthGenerator generator, Geometry geometry, Tags tags) {
        if (tags.is("building", false)) {
            return new Building(generator, geometry, tags);
        } else if (tags.is("building:part", false)) {
            return new BuildingPart(generator, geometry, tags);
        } else if (tags.is("leisure", false)) {
            String leisure = tags.get("leisure");
            switch (leisure) {
                case "park":
                case "playground":
                    return new Park(geometry, tags);
                case "garden":
                    return new Garden(geometry, tags);
                case "swimming_pool":
                    return new SwimmingPool(generator, geometry, tags);
            }
        } else if (tags.is("water", false)) {
            String water = tags.get("water");
            if (water.equals("pond") || water.equals("lake")) {
                return new Watersource(geometry, tags);
            }
        } else if (tags.is("waterway", false)) {
            String waterway = tags.get("waterway");
            if (waterway.equals("riverbank")) {
                return new Watersource(geometry, tags);
            }
        } else if (tags.is("landuse", false)) {
            String landuse = tags.get("landuse");
            if (landuse.equals("pond") || landuse.equals("lake")) {
                return new Watersource(geometry, tags);
            } else if (landuse.equals("park") || landuse.equals("playground")) {
                return new Park(geometry, tags);
            }
        }
        return null;
    }
}
