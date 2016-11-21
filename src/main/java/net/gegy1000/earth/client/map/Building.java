package net.gegy1000.earth.client.map;

import net.gegy1000.earth.client.util.Triangulate;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexBuffer;
import net.minecraft.util.math.AxisAlignedBB;
import org.lwjgl.opengl.GL11;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector3d;
import java.util.ArrayList;
import java.util.List;

public class Building implements MapObject {
    private final List<MapPoint> points;
    private final double height;
    private double lowestHeight = Double.MAX_VALUE;
    private double highestHeight = Double.MIN_VALUE;
    private final AxisAlignedBB bounds;
    private final Vector3d center;

    private VertexBuffer sideBuffer;
    private VertexBuffer topBuffer;
    private boolean built;

    public Building(List<MapPoint> points, double height) {
        this.height = height;
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
                triangulatedPoints.add(new MapPoint(vector.getX() + averageX, vector.getY() + averageZ));
            }
            this.points = triangulatedPoints;
        } else {
            this.points = points;
        }
        this.center = new Vector3d(averageX, averageY, averageZ);
        this.bounds = new AxisAlignedBB(minX, this.lowestHeight, minZ, maxX, this.highestHeight + (height * 0.0225), maxZ);
    }

    public List<MapPoint> getPoints() {
        return this.points;
    }

    public double getHeight() {
        return this.height;
    }

    public double getLowestHeight() {
        return this.lowestHeight;
    }

    public double getHighestHeight() {
        return this.highestHeight;
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
        for (MapPoint point : this.points) {
            BUILDER.pos(point.getX(), top, point.getZ()).normal(0.0F, 1.0F, 0.0F).endVertex();
        }

        this.finish(this.topBuffer);
        this.topBuffer.unbindBuffer();

        this.built = true;
    }

    @Override
    public void render() {
        GlStateManager.color(0.8F, 0.8F, 0.8F);

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
}
