package net.gegy1000.earth.client.map;

import net.gegy1000.earth.client.util.Triangulate;
import net.gegy1000.earth.server.util.MapPoint;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexBuffer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector3d;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Area implements MapObject {
    private final World world;
    private final List<MapPoint> points;
    private double lowestHeight = Double.MAX_VALUE;
    private double highestHeight = Double.MIN_VALUE;
    private final AxisAlignedBB bounds;
    private final Vector3d center;
    private final Type type;

    private VertexBuffer buffer;
    private boolean built;

    public Area(World world, List<MapPoint> points, Type type) {
        this.world = world;
        this.type = type;
        double averageX = 0.0;
        double averageY = 0.0;
        double averageZ = 0.0;
        double minX = Double.MAX_VALUE, minZ = Double.MAX_VALUE;
        double maxX = Double.MIN_VALUE, maxZ = Double.MIN_VALUE;
        for (MapPoint point : points) {
            double x = point.getX();
            double y = point.getY();
            double z = point.getZ();
            averageX += x;
            averageY += y;
            averageZ += z;
            if (y < this.lowestHeight) {
                this.lowestHeight = y;
            }
            if (y > this.highestHeight) {
                this.highestHeight = y;
            }
            if (x < minX) {
                minX = x;
            }
            if (x > maxX) {
                maxX = x;
            }
            if (z < minZ) {
                minZ = z;
            }
            if (z > maxZ) {
                maxZ = z;
            }
        }
        averageX /= points.size();
        averageY /= points.size();
        averageZ /= points.size();
        List<Vector2f> contour = new ArrayList<>(points.size());
        for (MapPoint point : points) {
            contour.add(new Vector2f((float) (point.getX() - averageX), (float) (point.getZ() - averageZ)));
        }
        List<Vector2f> result = new ArrayList<>();
        if (Triangulate.process(contour, result)) {
            List<MapPoint> triangulatedPoints = new ArrayList<>();
            for (Vector2f vector : result) {
                triangulatedPoints.add(new MapPoint(world, vector.getX() + averageX, vector.getY() + averageZ));
            }
            this.points = triangulatedPoints;
        } else {
            this.points = points;
        }
        this.center = new Vector3d(averageX, averageY, averageZ);
        this.bounds = new AxisAlignedBB(minX, this.lowestHeight, minZ, maxX, this.highestHeight, maxZ);
    }

    public List<MapPoint> getPoints() {
        return this.points;
    }

    @Override
    public AxisAlignedBB getBounds() {
        return this.bounds;
    }

    @Override
    public void build() {
        this.delete();

        double top = this.highestHeight;

        this.buffer = new VertexBuffer(DefaultVertexFormats.POSITION_NORMAL);
        this.buffer.bindBuffer();

        BUILDER.begin(GL11.GL_POLYGON, DefaultVertexFormats.POSITION_NORMAL);
        BUILDER.setTranslation(-this.center.x, -this.center.y, -this.center.z);
        for (MapPoint point : this.points) {
            BUILDER.pos(point.getX(), top, point.getZ()).normal(0.0F, 1.0F, 0.0F).endVertex();
        }

        this.finish(this.buffer);
        this.buffer.unbindBuffer();

        this.built = true;
    }

    @Override
    public void render() {
        this.type.prepareRender();

        this.buffer.bindBuffer();
        GlStateManager.glVertexPointer(3, GL11.GL_FLOAT, 16, 0);
        OpenGlHelper.setClientActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.glTexCoordPointer(3, GL11.GL_BYTE, 16, 12);
        OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
        this.buffer.drawArrays(GL11.GL_POLYGON);
        this.buffer.unbindBuffer();
    }

    @Override
    public void enableState() {
        GlStateManager.glEnableClientState(GL11.GL_VERTEX_ARRAY);
        OpenGlHelper.setClientActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
        OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
    }

    @Override
    public void disableState() {
        GlStateManager.glDisableClientState(GL11.GL_VERTEX_ARRAY);
        OpenGlHelper.setClientActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
        OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
    }

    @Override
    public void delete() {
        if (this.buffer != null) {
            this.buffer.deleteGlBuffers();
        }
        this.built = false;
    }

    @Override
    public Vector3d getCenter() {
        return this.center;
    }

    @Override
    public boolean hasBuilt() {
        return this.built;
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

        @Override
        public void prepareRender() {
            GlStateManager.color(this.red, this.green, this.blue, 0.5F);
        }
    }
}
