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
import net.gegy1000.earth.server.util.MapPoint;
import net.gegy1000.earth.client.map.Street;
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
        Set<Street> streets = new HashSet<>();
        Set<Building> buildings = new HashSet<>();
        try {
            OsmIterator iterator = new OsmXmlIterator(in, false);
            Map<Long, OsmNode> nodes = new HashMap<>();
            Set<OsmWay> ways = new HashSet<>();
            for (EntityContainer container : iterator) {
                if (container.getType() == EntityType.Node) {
                    OsmNode node = (OsmNode) container.getEntity();
                    nodes.put(node.getId(), node);
                } else if (container.getType() == EntityType.Way) {
                    ways.add((OsmWay) container.getEntity());
                }
            }
            for (OsmWay way : ways) {
                Map<String, String> tags = OsmModelUtil.getTagsAsMap(way);
                /*Earth.LOGGER.info("========");
                for (Map.Entry<String, String> entry : tags.entrySet()) {
                    Earth.LOGGER.info(entry.getKey() + ": " + entry.getValue());
                }
                Earth.LOGGER.info("");*/
                boolean highway = tags.containsKey("highway");
                boolean waterway = tags.containsKey("waterway");
                boolean building = tags.containsKey("building");
                if (highway || waterway) {
                    List<MapPoint> points = new ArrayList<>();
                    for (int i = 0; i < way.getNumberOfNodes(); i++) {
                        long nodeID = way.getNodeId(i);
                        OsmNode node = nodes.get(nodeID);
                        points.add(new MapPoint(world, node.getLatitude(), node.getLongitude()));
                    }
                    streets.add(new Street(tags.get("name"), points, waterway));
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
        return new TileData(streets, buildings);
    }

    public static class TileData {
        private Set<Street> streets;
        private Set<Building> buildings;

        public TileData(Set<Street> streets, Set<Building> buildings) {
            this.streets = streets;
            this.buildings = buildings;
        }

        public Set<Street> getStreets() {
            return this.streets;
        }

        public Set<Building> getBuildings() {
            return this.buildings;
        }
    }
}
