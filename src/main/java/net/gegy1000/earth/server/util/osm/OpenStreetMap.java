package net.gegy1000.earth.server.util.osm;

import de.topobyte.osm4j.core.access.OsmIterator;
import de.topobyte.osm4j.core.model.iface.EntityContainer;
import de.topobyte.osm4j.core.model.iface.EntityType;
import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.iface.OsmWay;
import de.topobyte.osm4j.core.model.util.OsmModelUtil;
import de.topobyte.osm4j.xml.dynsax.OsmXmlIterator;
import net.gegy1000.earth.Earth;
import net.gegy1000.earth.client.map.MapHandler;
import net.gegy1000.earth.client.map.MapObject;
import net.gegy1000.earth.client.map.MapObjectType;
import net.gegy1000.earth.server.util.MapPoint;
import net.minecraft.world.World;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class OpenStreetMap {
    private static final String URL = "http://api.openstreetmap.org/api/0.6";

    public static InputStream openStream(MapPoint start, MapPoint end) throws Exception {
        String bounds = start.getLongitude() + "," + start.getLatitude() + "," + end.getLongitude() + "," + end.getLatitude();
        URL url = new URL(URL + "/map?bbox=" + bounds);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            return connection.getInputStream();
        } else {
            Earth.LOGGER.error(url + " returned response code " + responseCode + "!");
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                Earth.LOGGER.error(line);
            }
        }
        return null;
    }

    public static Set<MapObject> parse(World world, InputStream in) throws IOException {
        Set<MapObject> mapObjects = new HashSet<>();
        try {
            OsmIterator iterator = new OsmXmlIterator(in, false);
            Map<Long, OsmNode> nodes = new HashMap<>();
            Set<OsmWay> osmWays = new HashSet<>();
            for (EntityContainer container : iterator) {
                if (container.getType() == EntityType.Node) {
                    OsmNode node = (OsmNode) container.getEntity();
                    nodes.put(node.getId(), node);
                } else if (container.getType() == EntityType.Way) {
                    osmWays.add((OsmWay) container.getEntity());
                }
            }
            for (OsmWay way : osmWays) {
                Map<String, String> tags = OsmModelUtil.getTagsAsMap(way);
                List<MapPoint> points = new ArrayList<>();
                for (int i = 0; i < way.getNumberOfNodes(); i++) {
                    long nodeID = way.getNodeId(i);
                    OsmNode node = nodes.get(nodeID);
                    MapPoint point = new MapPoint(world, node.getLatitude(), node.getLongitude());
                    points.add(point);
                }
                for (Class<? extends MapObjectType> typeClass : MapHandler.MAP_OBJECT_TYPES) {
                    try {
                        MapObjectType defaultType = (MapObjectType) typeClass.getDeclaredFields()[0].get(null);
                        MapObject object = defaultType.create(tags, world, points);
                        if (object != null) {
                            mapObjects.add(object);
                            break;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } finally {
            in.close();
        }
        return mapObjects;
    }
}
