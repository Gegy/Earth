package net.gegy1000.earth.client.map;

import net.gegy1000.earth.client.util.Triangulate;
import net.gegy1000.earth.server.util.MapPoint;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

import javax.vecmath.Vector2f;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Area implements MapObject {
    private final World world;
    private final List<MapPoint> points;
    private double lowestHeight = Double.MAX_VALUE;
    private double highestHeight = Double.MIN_VALUE;
    private final Type type;

    public Area(World world, List<MapPoint> points, Type type) {
        this.world = world;
        this.type = type;
        this.points = points;
    }

    @Override
    public void render(Tessellator tessellator, VertexBuffer builder, MapPoint center) {
        double centerX = center.getX();
        double centerZ = center.getZ();

        List<MapPoint> polygonPoints;

        List<Vector2f> contour = new ArrayList<>(this.points.size());
        for (int i = 0; i < this.points.size() - 1; i++) {
            MapPoint point = this.points.get(i);
            contour.add(new Vector2f((float) (point.getX() - centerX), (float) (point.getZ() - centerZ)));
        }

        List<Vector2f> result = new ArrayList<>();
        if (Triangulate.process(contour, result)) {
            List<MapPoint> triangulatedPoints = new ArrayList<>();
            for (Vector2f vector : result) {
                triangulatedPoints.add(new MapPoint(this.world, vector.getX() + centerX, vector.getY() + centerZ));
            }
            polygonPoints = triangulatedPoints;
        } else {
            polygonPoints = this.points;
        }

        builder.begin(GL11.GL_POLYGON, DefaultVertexFormats.POSITION_NORMAL);
        builder.setTranslation(-centerX, 0, -centerZ);

        for (MapPoint point : polygonPoints) {
            builder.pos(point.getX(), point.getY(), point.getZ()).normal(0.0F, 1.0F, 0.0F).endVertex();
        }

        tessellator.draw();
    }

    public enum Type implements MapObjectType<Area> {
        DEFAULT(255, 0, 0),
        PARK(0, 127, 40),
        SCHOOL(110, 60, 0);

        private float red;
        private float green;
        private float blue;

        Type(int red, int green, int blue) {
            this.red = red / 255.0F;
            this.green = green / 255.0F;
            this.blue = blue / 255.0F;
        }

        @Override
        public Area create(Map<String, String> tags, World world, List<MapPoint> points) {
            String leisure = tags.get("leisure");
            if (leisure != null) {
                switch (leisure) {
                    case "park":
                        return new Area(world, points, PARK);
                }
            }
            String amenity = tags.get("amenity");
            if (amenity != null) {
                switch (amenity) {
                    case "school":
                        return new Area(world, points, SCHOOL);
                }
            }
            return null;
        }
    }
}
