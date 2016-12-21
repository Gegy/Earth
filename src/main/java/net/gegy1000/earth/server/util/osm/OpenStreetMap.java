package net.gegy1000.earth.server.util.osm;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiPolygon;
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
import de.topobyte.osm4j.geometry.RegionBuilder;
import de.topobyte.osm4j.geometry.RegionBuilderResult;
import de.topobyte.osm4j.geometry.WayBuilder;
import de.topobyte.osm4j.geometry.WayBuilderResult;
import de.topobyte.osm4j.xml.dynsax.OsmXmlIterator;
import net.gegy1000.earth.Earth;
import net.gegy1000.earth.server.util.MapPoint;
import net.gegy1000.earth.server.util.osm.object.MapObject;
import net.gegy1000.earth.server.util.osm.object.Street;
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPInputStream;

public class OpenStreetMap {
    private static final String URL = "http://api.openstreetmap.org/api/0.6";

    private static final RegionBuilder REGION_BUILDER = new RegionBuilder();
    private static final WayBuilder WAY_BUILDER = new WayBuilder();

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
                BufferedReader reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(in)));
                String line;
                while ((line = reader.readLine()) != null) {
                    Earth.LOGGER.error(line);
                }
            }
        }
        return null;
    }

    public static Set<MapObject> parse(World world, InputStream in) throws IOException {
        EarthGenerator generator = WorldTypeEarth.getGenerator(world);
        Set<MapObject> mapObjects = new HashSet<>();
        try {
            OsmIterator iterator = new OsmXmlIterator(in, false);
            InMemoryMapDataSet data = MapDataSetLoader.read(iterator, true, true, true);
            List<Geometry> buildings = new ArrayList<>();
            List<LineString> streets = new ArrayList<>();
            Set<OsmWay> buildingRelationWays = new HashSet<>();
            EntityFinder finder = EntityFinders.create(data, EntityNotFoundStrategy.IGNORE);
            Collection<OsmRelation> relations = data.getRelations().valueCollection();
            Collection<OsmWay> ways = data.getWays().valueCollection();
            for (OsmRelation relation : relations) {
                Map<String, String> tags = OsmModelUtil.getTagsAsMap(relation);
                if (tags.containsKey("building")) {
                    MultiPolygon area = createArea(data, relation);
                    if (area != null) {
                        buildings.add(area);
                    }
                    try {
                        finder.findMemberWays(relation, buildingRelationWays);
                    } catch (EntityNotFoundException e) {
                    }
                }
            }
            for (OsmWay way : ways) {
                if (!buildingRelationWays.contains(way)) {
                    Map<String, String> tags = OsmModelUtil.getTagsAsMap(way);
                    String building = tags.get("building");
                    if (building != null) {
                        MultiPolygon area = createArea(data, way);
                        if (area != null) {
                            buildings.add(area);
                        }
                    }
                    String highway = tags.get("highway");
                    if (highway != null) {
                        Collection<LineString> paths = createLine(data, way);
                        for (LineString path : paths) {
                            streets.add(path);
                        }
                    }
                }
            }
            for (LineString street : streets) {
                mapObjects.add(new Street(generator, street));
            }
        } finally {
            in.close();
        }
        return mapObjects;
    }

    private static Collection<LineString> createLine(OsmEntityProvider data, OsmWay way) {
        List<LineString> results = new ArrayList<>();
        try {
            WayBuilderResult lines = WAY_BUILDER.build(way, data);
            results.addAll(lines.getLineStrings());
            if (lines.getLinearRing() != null) {
                results.add(lines.getLinearRing());
            }
        } catch (EntityNotFoundException e) {
            return results;
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
}
