package net.gegy1000.earth.client.map;

import net.gegy1000.earth.client.util.PolygonTesselator;
import net.gegy1000.earth.server.util.MapPoint;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexBuffer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

import javax.vecmath.Vector3d;
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
    private List<VertexBuffer> topBuffers;
    private boolean built;

    public Building(World world, List<MapPoint> points, double height, Type type) {
        this.world = world;
        this.type = type;
        this.height = height;
        double minX = Double.MAX_VALUE, minZ = Double.MAX_VALUE;
        double maxX = Double.MIN_VALUE, maxZ = Double.MIN_VALUE;
        double centerX = 0.0;
        double centerY = 0.0;
        double centerZ = 0.0;
        int size = points.size();
        for (MapPoint point : points) {
            double x = point.getX();
            double y = point.getY();
            double z = point.getZ();
            centerX += x / size;
            centerY += y / size;
            centerZ += z / size;
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
        this.points = points;
        this.center = new Vector3d(centerX, centerY, centerZ);
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
            BUILDER.pos(x, this.lowestHeight, z).normal((float) normal.x, (float) normal.y, (float) normal.z).endVertex();
            BUILDER.pos(x, top, z).normal((float) normal.x, (float) normal.y, (float) normal.z).endVertex();
        }

        this.finish(this.sideBuffer);
        this.sideBuffer.unbindBuffer();

        PolygonTesselator.TessellationObject tessellationObject = new PolygonTesselator.TessellationObject(DefaultVertexFormats.POSITION_NORMAL, BUILDER, this.points, this.center, top);
        PolygonTesselator.drawPoints(tessellationObject);
        this.topBuffers = tessellationObject.getBuffers();

        this.built = true;
    }

    @Override
    public void render() {
        this.type.prepareRender();

        this.sideBuffer.bindBuffer();
        GlStateManager.glVertexPointer(3, GL11.GL_FLOAT, 16, 0);
        GL11.glNormalPointer(GL11.GL_BYTE, 16, 12);
        this.sideBuffer.drawArrays(GL11.GL_QUAD_STRIP);
        this.sideBuffer.unbindBuffer();

        for (VertexBuffer buffer : this.topBuffers) {
            buffer.bindBuffer();
            GlStateManager.glVertexPointer(3, GL11.GL_FLOAT, 16, 0);
            GL11.glNormalPointer(GL11.GL_BYTE, 16, 12);
            buffer.drawArrays(GL11.GL_POLYGON);
            buffer.unbindBuffer();
        }
    }

    @Override
    public void enableState() {
        GlStateManager.glEnableClientState(GL11.GL_VERTEX_ARRAY);
        GlStateManager.glEnableClientState(GL11.GL_NORMAL_ARRAY);
    }

    @Override
    public void disableState() {
        GlStateManager.glDisableClientState(GL11.GL_VERTEX_ARRAY);
        GlStateManager.glDisableClientState(GL11.GL_NORMAL_ARRAY);
    }

    @Override
    public void delete() {
        if (this.sideBuffer != null) {
            this.sideBuffer.deleteGlBuffers();
            this.sideBuffer = null;
        }
        if (this.topBuffers != null) {
            for (VertexBuffer buffer : this.topBuffers) {
                buffer.deleteGlBuffers();
            }
            this.topBuffers = null;
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
