package net.gegy1000.earth.server.util.osm.object;

import net.gegy1000.earth.server.util.osm.object.area.Building;
import net.gegy1000.earth.server.util.osm.object.area.BuildingPart;
import net.gegy1000.earth.server.util.osm.object.area.Garden;
import net.gegy1000.earth.server.util.osm.object.area.Lake;
import net.gegy1000.earth.server.util.osm.object.area.Park;
import net.gegy1000.earth.server.util.osm.object.line.Barrier;
import net.gegy1000.earth.server.util.osm.object.line.Railway;
import net.gegy1000.earth.server.util.osm.object.line.River;
import net.gegy1000.earth.server.util.osm.object.line.highway.Path;
import net.gegy1000.earth.server.util.osm.object.line.highway.Street;
import net.gegy1000.earth.server.util.osm.object.line.highway.Track;

public enum MapObjectType {
    LAKE(Lake.class),
    RIVER(River.class),
    BUILDING_PART(BuildingPart.class),
    BUILDING(Building.class),
    BARRIER(Barrier.class),
    STREET(Street.class),
    PATH(Path.class),
    TRACK(Track.class),
    RAILWAY(Railway.class),
    GARDEN(Garden.class),
    PARK(Park.class);

    private Class<? extends MapObject> mapObject;

    MapObjectType(Class<? extends MapObject> mapObject) {
        this.mapObject = mapObject;
    }

    public static MapObjectType get(Class<? extends MapObject> mapObject) {
        for (MapObjectType type : MapObjectType.values()) {
            if (type.mapObject.equals(mapObject)) {
                return type;
            }
        }
        return null;
    }
}
