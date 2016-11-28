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

public class Building implements MapObject {
    private final World world;
    private final List<MapPoint> points;
    private final double height;
    private double lowestHeight = Double.MAX_VALUE;
    private double highestHeight = Double.MIN_VALUE;
    private final AxisAlignedBB bounds;
    private final Vector3d center;
    private final Type type;

    private VertexBuffer sideBuffer;
    private VertexBuffer topBuffer;
    private boolean built;

    public Building(World world, List<MapPoint> points, double height, Type type) {
        this.world = world;
        this.height = height;
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
        this.bounds = new AxisAlignedBB(minX, this.lowestHeight, minZ, maxX, this.highestHeight + (height * 0.0225), maxZ);
    }

    public double getHeight() {
        return this.height;
    }

    @Override
    public AxisAlignedBB getBounds() {
        return this.bounds;
    }

    @Override
    public void build() {
        this.delete();

        double top = this.highestHeight + (this.height * 0.0225);

        this.sideBuffer = new VertexBuffer(DefaultVertexFormats.POSITION_NORMAL);
        this.sideBuffer.bindBuffer();

        BUILDER.begin(GL11.GL_QUAD_STRIP, DefaultVertexFormats.POSITION_NORMAL);
        BUILDER.setTranslation(-this.center.x, -this.center.y, -this.center.z);
        for (MapPoint point : this.points) {
            double x = point.getX();
            double z = point.getZ();
            BUILDER.pos(x, this.lowestHeight, z).normal(0.0F, 0.0F, 1.0F).endVertex();
            BUILDER.pos(x, top, z).normal(0.0F, 0.0F, 1.0F).endVertex();
        }

        this.finish(this.sideBuffer);
        this.sideBuffer.unbindBuffer();

        this.topBuffer = new VertexBuffer(DefaultVertexFormats.POSITION_NORMAL);
        this.topBuffer.bindBuffer();

        BUILDER.begin(GL11.GL_POLYGON, DefaultVertexFormats.POSITION_NORMAL);
        BUILDER.setTranslation(-this.center.x, -this.center.y, -this.center.z);
        for (MapPoint point : this.points) {
            BUILDER.pos(point.getX(), top, point.getZ()).normal(0.0F, 1.0F, 0.0F).endVertex();
        }

        this.finish(this.topBuffer);
        this.topBuffer.unbindBuffer();

        this.built = true;
    }

    @Override
    public void render() {
        this.type.prepareRender();

        this.sideBuffer.bindBuffer();
        GlStateManager.glVertexPointer(3, GL11.GL_FLOAT, 16, 0);
        OpenGlHelper.setClientActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.glTexCoordPointer(3, GL11.GL_BYTE, 16, 12);
        OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
        this.sideBuffer.drawArrays(GL11.GL_QUAD_STRIP);
        this.sideBuffer.unbindBuffer();

        this.topBuffer.bindBuffer();
        GlStateManager.glVertexPointer(3, GL11.GL_FLOAT, 16, 0);
        OpenGlHelper.setClientActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.glTexCoordPointer(3, GL11.GL_BYTE, 16, 12);
        OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
        this.topBuffer.drawArrays(GL11.GL_POLYGON);
        this.topBuffer.unbindBuffer();
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
        if (this.sideBuffer != null) {
            this.sideBuffer.deleteGlBuffers();
        }
        if (this.topBuffer != null) {
            this.topBuffer.deleteGlBuffers();
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

    public enum Type implements MapObjectType<Building> {
        BUILDING;

        @Override
        public Building create(Map<String, String> tags, World world, List<MapPoint> points) {
            if (tags.containsKey("building")) {
                double height = 8.0;
                if (tags.containsKey("height")) {
                    try {
                        height = Double.parseDouble(tags.get("height"));
                    } catch (NumberFormatException e) {
                    }
                } else if (tags.containsKey("building:levels")) {
                    try {
                        height = Integer.parseInt(tags.get("building:levels")) * 8.0;
                    } catch (NumberFormatException e) {
                    }
                }
                return new Building(world, points, height, BUILDING);
            }
            return null;
        }

        @Override
        public void prepareRender() {
            GlStateManager.color(0.8F, 0.8F, 0.8F, 1.0F);
        }
    }
}
