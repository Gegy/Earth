package net.gegy1000.earth.server.util.osm;

import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import de.topobyte.osm4j.core.access.OsmIterator;
import de.topobyte.osm4j.core.dataset.InMemoryMapDataSet;
import de.topobyte.osm4j.core.dataset.MapDataSetLoader;
import de.topobyte.osm4j.core.model.iface.OsmRelation;
import de.topobyte.osm4j.core.model.iface.OsmWay;
import de.topobyte.osm4j.core.model.util.OsmModelUtil;
import de.topobyte.osm4j.core.resolve.EntityFinder;
import de.topobyte.osm4j.core.resolve.EntityFinders;
import de.topobyte.osm4j.core.resolve.EntityNotFoundException;
import de.topobyte.osm4j.core.resolve.EntityNotFoundStrategy;
import de.topobyte.osm4j.core.resolve.OsmEntityProvider;
import de.topobyte.osm4j.geometry.NodeBuilder;
import de.topobyte.osm4j.geometry.RegionBuilder;
import de.topobyte.osm4j.geometry.RegionBuilderResult;
import de.topobyte.osm4j.geometry.WayBuilder;
import de.topobyte.osm4j.geometry.WayBuilderResult;
import de.topobyte.osm4j.xml.dynsax.OsmXmlIterator;
import net.gegy1000.earth.Earth;
import net.gegy1000.earth.server.util.MapPoint;
import net.gegy1000.earth.server.util.osm.object.MapObject;
import net.gegy1000.earth.server.util.osm.object.MapObjectType;
import net.gegy1000.earth.server.util.osm.object.area.Area;
import net.gegy1000.earth.server.util.osm.object.line.Line;
import net.gegy1000.earth.server.util.osm.object.point.Marker;
import net.gegy1000.earth.server.util.osm.tag.Tags;
import net.gegy1000.earth.server.world.gen.EarthGenerator;
import net.gegy1000.earth.server.world.gen.WorldTypeEarth;
import net.minecraft.world.World;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPInputStream;

public class OpenStreetMap {
    private static final String URL = "http://api.openstreetmap.org/api/0.6";

    private static final RegionBuilder REGION_BUILDER = new RegionBuilder();
    private static final WayBuilder WAY_BUILDER = new WayBuilder();
    private static final NodeBuilder NODE_BUILDER = new NodeBuilder();

    public static InputStream openStream(MapPoint start, MapPoint end) throws Exception {
        String bounds = start.getLongitude() + "," + start.getLatitude() + "," + end.getLongitude() + "," + end.getLatitude();
        URL url = new URL(URL + "/map?bbox=" + bounds);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestProperty("Accept-Encoding", "gzip");
        connection.setRequestMethod("GET");
        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            return new GZIPInputStream(connection.getInputStream());
        } else {
            Earth.LOGGER.error(url + " returned response code " + responseCode + "!");
            InputStream in = connection.getErrorStream();
            if (in != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                String line;
                while ((line = reader.readLine()) != null) {
                    Earth.LOGGER.error(line);
                }
            }
        }
        return null;
    }

    public static EnumMap<MapObjectType, List<MapObject>> parse(World world, InputStream in) throws IOException {
        EarthGenerator generator = WorldTypeEarth.getGenerator(world);
        EnumMap<MapObjectType, List<MapObject>> mapObjects = new EnumMap<>(MapObjectType.class);
        Map<Long, MapObject> mapObjectIds = new HashMap<>();
        Map<Long, Set<Long>> relationIds = new HashMap<>();
        try {
            OsmIterator iterator = new OsmXmlIterator(in, false);
            InMemoryMapDataSet data = MapDataSetLoader.read(iterator, true, true, true);
            Set<OsmWay> processedRelations = new HashSet<>();
            EntityFinder finder = EntityFinders.create(data, EntityNotFoundStrategy.IGNORE);
            Collection<OsmRelation> relations = data.getRelations().valueCollection();
            Collection<OsmWay> ways = data.getWays().valueCollection();
            for (OsmRelation relation : relations) {
                Tags tags = Tags.from(OsmModelUtil.getTagsAsMap(relation));
                WayType wayType = WayType.get(tags, null);
                switch (wayType) {
                    case AREA:
                        MultiPolygon geometry = createArea(data, relation);
                        if (geometry != null && geometry.getNumPoints() > 0) {
                            Area area = Area.build(generator, geometry, tags);
                            if (area != null) {
                                add(mapObjects, area);
                            }
                        }
                        try {
                            Set<OsmWay> members = new HashSet<>();
                            finder.findMemberWays(relation, members);
                            Set<Long> memberIds = new HashSet<>();
                            for (OsmWay member : members) {
                                memberIds.add(member.getId());
                            }
                            relationIds.put(relation.getId(), memberIds);
                            processedRelations.addAll(members);
                        } catch (EntityNotFoundException e) {
                        }
                        break;
                }
            }
            for (OsmWay way : ways) {
                if (!processedRelations.contains(way)) {
                    Tags tags = Tags.from(OsmModelUtil.getTagsAsMap(way));
                    WayType wayType = WayType.get(tags, OsmModelUtil.nodesAsList(way));
                    switch (wayType) {
                        case AREA:
                            MultiPolygon geometry = createArea(data, way);
                            if (geometry != null && geometry.getNumPoints() > 0) {
                                Area area = Area.build(generator, geometry, tags);
                                if (area != null) {
                                    add(mapObjects, area);
                                    mapObjectIds.put(way.getId(), area);
                                }
                            }
                            break;
                        case LINE:
                            Collection<LineString> wayLines = createLines(data, way);
                            for (LineString lineString : wayLines) {
                                Line line = Line.build(generator, lineString, tags);
                                if (line != null) {
                                    add(mapObjects, line);
                                    mapObjectIds.put(way.getId(), line);
                                }
                            }
                            break;
                        case POINT:
                            List<Point> wayPoints = createPoints(data, way);
                            for (Point point : wayPoints) {
                                Marker marker = Marker.build(generator, point, tags);
                                if (marker != null) {
                                    add(mapObjects, marker);
                                    mapObjectIds.put(way.getId(), marker);
                                }
                            }
                            break;
                    }
                }
            }
        } finally {
            in.close();
        }
        for (Map.Entry<Long, MapObject> entry : mapObjectIds.entrySet()) {
            Set<Long> memberIds = relationIds.get(entry.getKey());
            if (memberIds != null) {
                Set<MapObject> relations = new HashSet<>();
                for (Long memberId : memberIds) {
                    relations.add(mapObjectIds.get(memberId));
                }
                entry.getValue().init(relations);
            }
        }
        return mapObjects;
    }

    private static void add(EnumMap<MapObjectType, List<MapObject>> mapObjects, MapObject mapObject) {
        List<MapObject> typeObjects = mapObjects.computeIfAbsent(mapObject.getType(), mapObjectType -> new ArrayList<>());
        typeObjects.add(mapObject);
    }

    private static Collection<LineString> createLines(OsmEntityProvider data, OsmWay way) {
        List<LineString> results = new ArrayList<>();
        try {
            WayBuilderResult lines = WAY_BUILDER.build(way, data);
            results.addAll(lines.getLineStrings());
            if (lines.getLinearRing() != null) {
                results.add(lines.getLinearRing());
            }
        } catch (EntityNotFoundException e) {
        }
        return results;
    }

    private static MultiPolygon createArea(OsmEntityProvider data, OsmWay way) {
        try {
            RegionBuilderResult region = REGION_BUILDER.build(way, data);
            return region.getMultiPolygon();
        } catch (EntityNotFoundException e) {
            return null;
        }
    }

    private static MultiPolygon createArea(OsmEntityProvider data, OsmRelation relation) {
        try {
            RegionBuilderResult region = REGION_BUILDER.build(relation, data);
            return region.getMultiPolygon();
        } catch (EntityNotFoundException e) {
            return null;
        }
    }

    private static List<Point> createPoints(OsmEntityProvider data, OsmWay way) {
        List<Point> points = new ArrayList<>();
        for (int i = 0; i < way.getNumberOfNodes(); i++) {
            try {
                points.add(NODE_BUILDER.build(data.getNode(way.getNodeId(i))));
            } catch (EntityNotFoundException e) {
            }
        }
        return points;
    }
}
