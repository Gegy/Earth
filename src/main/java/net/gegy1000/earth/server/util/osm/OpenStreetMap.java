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
import de.topobyte.osm4j.geometry.MissingEntitiesStrategy;
import de.topobyte.osm4j.geometry.NodeBuilder;
import de.topobyte.osm4j.geometry.RegionBuilder;
import de.topobyte.osm4j.geometry.RegionBuilderResult;
import de.topobyte.osm4j.geometry.WayBuilder;
import de.topobyte.osm4j.geometry.WayBuilderResult;
import de.topobyte.osm4j.xml.dynsax.OsmXmlIterator;
import net.gegy1000.earth.Earth;
import net.gegy1000.earth.server.util.MapPoint;
import net.gegy1000.earth.server.util.osm.tag.Tags;
import org.apache.commons.io.IOUtils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class OpenStreetMap {
    public static final File CACHE = new File(".", "mods/earth/cache/osm");

    private static final String API = "http://api.openstreetmap.org/api/0.6/";
    private static final String MAP = "map";

    private static final RegionBuilder REGION_BUILDER = new RegionBuilder();
    private static final WayBuilder WAY_BUILDER = new WayBuilder();
    private static final NodeBuilder NODE_BUILDER = new NodeBuilder();

    static {
        REGION_BUILDER.setMissingEntitiesStrategy(MissingEntitiesStrategy.BUILD_PARTIAL);
        WAY_BUILDER.setMissingEntitiesStrategy(MissingEntitiesStrategy.BUILD_PARTIAL);
    }

    public static InputStream openStream(MapTile tile) throws Exception {
        File file = new File(CACHE, tile.getTileLat() + "_" + tile.getTileLon() + ".tile");
        if (file.exists()) {
            return new GZIPInputStream(new FileInputStream(file));
        } else {
            MapPoint start = tile.getMinPos();
            MapPoint end = tile.getMaxPos();
            String bounds = start.getLongitude() + "," + start.getLatitude() + "," + end.getLongitude() + "," + end.getLatitude();
            java.net.URL url = new URL(API + MAP + "?bbox=" + bounds);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("Accept-Encoding", "gzip");
            connection.setRequestMethod("GET");
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                GZIPInputStream in = new GZIPInputStream(connection.getInputStream());
                byte[] data = IOUtils.toByteArray(in);
                in.close();
                if (!CACHE.exists()) {
                    CACHE.mkdirs();
                }
                OutputStream out = new GZIPOutputStream(new FileOutputStream(file));
                out.write(data);
                out.close();
                return new ByteArrayInputStream(data);
            } else {
                Earth.LOGGER.error("{} returned response code {}!", url, responseCode);
                InputStream in = connection.getErrorStream();
                if (in != null) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        Earth.LOGGER.error(line);
                    }
                }
            }
        }
        return null;
    }

    public static List<MapObject> parse(InputStream in) throws IOException {
        OsmIterator iterator = new OsmXmlIterator(in, false);
        InMemoryMapDataSet data = MapDataSetLoader.read(iterator, true, true, true);
        EntityFinder finder = EntityFinders.create(data, EntityNotFoundStrategy.IGNORE);
        Collection<OsmRelation> relations = data.getRelations().valueCollection();
        Collection<OsmWay> ways = data.getWays().valueCollection();
        List<MapObject> objects = new ArrayList<>(relations.size() + ways.size());
        Set<OsmWay> relationMembers = new HashSet<>();
        for (OsmRelation relation : relations) {
            Tags tags = Tags.from(OsmModelUtil.getTagsAsMap(relation));
            Set<OsmWay> members = new HashSet<>();
            try {
                finder.findMemberWays(relation, members);
                relationMembers.addAll(members);
                objects.add(new MapRelation(data, tags, relation, members));
                objects.addAll(members.stream().map(way -> new MapWay(data, Tags.from(OsmModelUtil.getTagsAsMap(way)), way)).collect(Collectors.toList()));
            } catch (EntityNotFoundException e) {
                Earth.LOGGER.error("Failed to find OSM relation members", e);
            }
        }
        for (OsmWay way : ways) {
            if (!relationMembers.contains(way)) {
                Tags tags = Tags.from(OsmModelUtil.getTagsAsMap(way));
                objects.add(new MapWay(data, tags, way));
            }
        }
        objects.sort(Comparator.comparingInt(MapObject::getLayer));
        return objects;
    }

    static Collection<LineString> createLines(OsmEntityProvider data, OsmWay way) {
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

    static MultiPolygon createArea(OsmEntityProvider data, OsmWay way) {
        try {
            RegionBuilderResult region = REGION_BUILDER.build(way, data);
            return region.getMultiPolygon();
        } catch (EntityNotFoundException e) {
            Earth.LOGGER.warn("Couldn't find OSM relation entity", e);
            return null;
        }
    }

    static MultiPolygon createArea(OsmEntityProvider data, OsmRelation relation) {
        try {
            RegionBuilderResult region = REGION_BUILDER.build(relation, data);
            return region.getMultiPolygon();
        } catch (EntityNotFoundException e) {
            Earth.LOGGER.warn("Couldn't find OSM relation entity", e);
            return null;
        }
    }

    static List<Point> createPoints(OsmEntityProvider data, OsmWay way) {
        List<Point> points = new ArrayList<>();
        try {
            for (int i = 0; i < way.getNumberOfNodes(); i++) {
                points.add(NODE_BUILDER.build(data.getNode(way.getNodeId(i))));
            }
        } catch (EntityNotFoundException e) {
        }
        return points;
    }
}
