package net.gegy1000.earth.client.map;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexBuffer;
import net.minecraft.util.math.AxisAlignedBB;
import org.lwjgl.opengl.GL11;

import javax.vecmath.Vector3d;
import java.util.List;

public class Street implements MapObject {
    private final String name;
    private final List<MapPoint> points;
    private final boolean waterway;
    private final AxisAlignedBB bounds;
    private final Vector3d center;

    private VertexBuffer buffer;
    private boolean built;

    public Street(String name, List<MapPoint> points, boolean waterway) {
        this.name = name;
        this.points = points;
        this.waterway = waterway;
        double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE, minZ = Double.MAX_VALUE;
        double maxX = Double.MIN_VALUE, maxY = Double.MIN_VALUE, maxZ = Double.MIN_VALUE;
        double averageX = 0.0;
        double averageY = 0.0;
        double averageZ = 0.0;
        for (MapPoint point : points) {
            double x = point.getX();
            double y = point.getY();
            double z = point.getZ();
            averageX += x;
            averageY += y;
            averageZ += z;
            if (x < minX) {
                minX = x;
            }
            if (x > maxX) {
                maxX = x;
            }
            if (y < minY) {
                minY = y;
            }
            if (y > maxY) {
                maxY = y;
            }
            if (z < minZ) {
                minZ = z;
            }
            if (z > maxZ) {
                maxZ = z;
            }
        }
        this.center = new Vector3d(averageX, averageY, averageZ);
        this.bounds = new AxisAlignedBB(minX, minY - 0.5, minZ, maxX, maxY + 0.5, maxZ);
    }

    public String getName() {
        return this.name;
    }

    public List<MapPoint> getPoints() {
        return this.points;
    }

    public boolean isWaterway() {
        return this.waterway;
    }

    @Override
    public AxisAlignedBB getBounds() {
        return this.bounds;
    }

    @Override
    public boolean hasBuilt() {
        return this.built;
    }

    @Override
    public void build() {
        this.buffer = new VertexBuffer(DefaultVertexFormats.POSITION);
        this.buffer.bindBuffer();

        BUILDER.begin(GL11.GL_QUAD_STRIP, DefaultVertexFormats.POSITION);

        double streetThickness = 0.1;
        for (int i = 0; i < this.points.size(); i++) {
            MapPoint point = this.points.get(i);
            if (i < this.points.size() - 1) {
                MapPoint next = this.points.get(i + 1);
                double deltaX = next.getX() - point.getX();
                double deltaZ = next.getZ() - point.getZ();
                double length = Math.sqrt((deltaX * deltaX) + (deltaZ * deltaZ));
                double offsetX = (streetThickness * deltaZ / length) / 2;
                double offsetZ = (streetThickness * deltaX / length) / 2;
                BUILDER.pos(point.getX() - offsetX, point.getY(), point.getZ() + offsetZ).endVertex();
                BUILDER.pos(point.getX() + offsetX, point.getY(), point.getZ() - offsetZ).endVertex();
                BUILDER.pos(next.getX() - offsetX, next.getY(), next.getZ() + offsetZ).endVertex();
                BUILDER.pos(next.getX() + offsetX, next.getY(), next.getZ() - offsetZ).endVertex();
            }
        }

        this.finish(this.buffer);
        this.buffer.unbindBuffer();

        this.built = true;
    }

    @Override
    public void render() {
        GlStateManager.enableCull();
        GlStateManager.cullFace(GlStateManager.CullFace.FRONT);
        if (this.waterway) {
            GlStateManager.doPolygonOffset(-1.0F, 3.0F);
            GlStateManager.color(0.19F, 0.89F, 1.0F, 1.0F);
        } else {
            GlStateManager.doPolygonOffset(-1.0F, 0.0F);
            GlStateManager.color(0.1F, 0.1F, 0.1F, 1.0F);
        }

        this.buffer.bindBuffer();
        GlStateManager.glVertexPointer(3, GL11.GL_FLOAT, 12, 0);
        this.buffer.drawArrays(GL11.GL_QUAD_STRIP);
        this.buffer.unbindBuffer();

        GlStateManager.cullFace(GlStateManager.CullFace.BACK);
        GlStateManager.disableCull();
    }

    @Override
    public void enableState() {
        GlStateManager.glEnableClientState(GL11.GL_VERTEX_ARRAY);
    }

    @Override
    public void disableState() {
        GlStateManager.glDisableClientState(GL11.GL_VERTEX_ARRAY);
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
}
