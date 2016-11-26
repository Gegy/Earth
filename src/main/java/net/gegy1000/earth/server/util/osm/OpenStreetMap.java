package net.gegy1000.earth.server.util.osm;

import de.topobyte.osm4j.core.access.OsmIterator;
import de.topobyte.osm4j.core.model.iface.EntityContainer;
import de.topobyte.osm4j.core.model.iface.EntityType;
import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.iface.OsmWay;
import de.topobyte.osm4j.core.model.util.OsmModelUtil;
import de.topobyte.osm4j.xml.dynsax.OsmXmlIterator;
import net.gegy1000.earth.Earth;
import net.gegy1000.earth.client.map.Building;
import net.gegy1000.earth.client.map.Way;
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

    public static TileData parse(World world, InputStream in) throws IOException {
        Set<Way> ways = new HashSet<>();
        Set<Building> buildings = new HashSet<>();
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
                /*Earth.LOGGER.info("========");
                for (Map.Entry<String, String> entry : tags.entrySet()) {
                    Earth.LOGGER.info(entry.getKey() + ": " + entry.getValue());
                }
                Earth.LOGGER.info("");*/
                boolean railway = tags.containsKey("railway");
                boolean highway = tags.containsKey("highway");
                boolean waterway = tags.containsKey("waterway");
                boolean building = tags.containsKey("building");
                if (highway || waterway || railway) {
                    int lanes = 1;
                    if (tags.containsKey("lanes")) {
                        try {
                            lanes = Integer.parseInt(tags.get("lanes"));
                        } catch (NumberFormatException e) {
                        }
                    }
                    List<MapPoint> points = new ArrayList<>();
                    for (int i = 0; i < way.getNumberOfNodes(); i++) {
                        long nodeID = way.getNodeId(i);
                        OsmNode node = nodes.get(nodeID);
                        MapPoint point = new MapPoint(world, node.getLatitude(), node.getLongitude());
                        points.add(point);
                    }
                    ways.add(new Way(tags.get("name"), points, 0.1 * lanes, Way.Type.fromTags(tags)));
                } else if (building) {
                    double height = 15.0;
                    if (tags.containsKey("height")) {
                        try {
                            height = Double.parseDouble(tags.get("height"));
                        } catch (NumberFormatException e) {
                        }
                    }
                    List<MapPoint> points = new ArrayList<>();
                    for (int i = 0; i < way.getNumberOfNodes(); i++) {
                        long nodeID = way.getNodeId(i);
                        OsmNode node = nodes.get(nodeID);
                        points.add(new MapPoint(world, node.getLatitude(), node.getLongitude()));
                    }
                    buildings.add(new Building(world, points, height));
                }
            }
        } finally {
            in.close();
        }
        return new TileData(ways, buildings);
    }

    public static class TileData {
        private Set<Way> ways;
        private Set<Building> buildings;

        public TileData(Set<Way> ways, Set<Building> buildings) {
            this.ways = ways;
            this.buildings = buildings;
        }

        public Set<Way> getWays() {
            return this.ways;
        }

        public Set<Building> getBuildings() {
            return this.buildings;
        }
    }
}
