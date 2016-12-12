package net.gegy1000.earth.client.map;

import net.gegy1000.earth.client.util.Triangulate;
import net.gegy1000.earth.server.util.MapPoint;
import net.gegy1000.earth.server.world.gen.EarthGenerator;
import net.gegy1000.earth.server.world.gen.WorldTypeEarth;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector3d;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Building implements MapObject {
    private final World world;
    private final EarthGenerator generator;
    private final List<MapPoint> points;
    private final double height;
    private double lowestHeight = Double.MAX_VALUE;
    private double highestHeight = Double.MIN_VALUE;
    private final Type type;

    public Building(World world, List<MapPoint> points, double height, Type type) {
        this.world = world;
        this.generator = WorldTypeEarth.getGenerator(world);
        this.type = type;
        this.height = height;
        for (MapPoint point : points) {
            double y = point.getY();
            if (y < this.lowestHeight) {
                this.lowestHeight = y;
            }
            if (y > this.highestHeight) {
                this.highestHeight = y;
            }
        }
        this.points = points;
    }

    public double getHeight() {
        return this.height;
    }

    @Override
    public void render(Tessellator tessellator, VertexBuffer builder, MapPoint center) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        double top = this.highestHeight + (this.height / this.generator.getRatio());

        double centerX = center.getX();
        double centerZ = center.getZ();

        builder.begin(GL11.GL_QUAD_STRIP, DefaultVertexFormats.POSITION_NORMAL);
        builder.setTranslation(-centerX, 0, -centerZ);
        for (int i = 0; i < this.points.size(); i++) {
            MapPoint point = this.points.get(i);
            int previous = i - 1;
            if (previous < 0) {
                previous = this.points.size() - 1;
            }
            MapPoint previousPoint = this.points.get(previous);
            double x = point.getX();
            double z = point.getZ();
            Vector3d v1 = new Vector3d(x, this.lowestHeight, z);
            Vector3d v2 = new Vector3d(previousPoint.getX(), this.lowestHeight, previousPoint.getZ());
            Vector3d normal = new Vector3d();
            v2.sub(new Vector3d(x, top, z));
            v1.sub(new Vector3d(previousPoint.getX(), top, previousPoint.getZ()));
            normal.cross(v1, v2);
            normal.normalize();
            builder.pos(x, this.lowestHeight, z).normal((float) normal.x, (float) normal.y, (float) normal.z).endVertex();
            builder.pos(x, top, z).normal((float) normal.x, (float) normal.y, (float) normal.z).endVertex();
        }

        tessellator.draw();

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
                triangulatedPoints.add(new MapPoint(this.world, vector.getX() + centerX, top,vector.getY() + centerZ));
            }
            polygonPoints = triangulatedPoints;
        } else {
            polygonPoints = this.points;
        }

        builder.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_NORMAL);
        builder.setTranslation(-centerX, 0, -centerZ);

        for (MapPoint point : polygonPoints) {
            builder.pos(point.getX(), top, point.getZ()).normal(0.0F, 1.0F, 0.0F).endVertex();
        }

        tessellator.draw();
    }

    public enum Type implements MapObjectType<Building> {
        BUILDING;

        @Override
        public Building create(Map<String, String> tags, World world, List<MapPoint> points) {
            if (tags.containsKey("building")) {
                double height = 3.0;
                if (tags.containsKey("height")) {
                    try {
                        height = Double.parseDouble(tags.get("height"));
                    } catch (NumberFormatException e) {
                    }
                } else if (tags.containsKey("building:levels")) {
                    try {
                        height = Integer.parseInt(tags.get("building:levels")) * 3.0;
                    } catch (NumberFormatException e) {
                    }
                }
                return new Building(world, points, height, BUILDING);
            }
            return null;
        }
    }
}
