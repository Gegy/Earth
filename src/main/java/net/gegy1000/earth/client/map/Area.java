package net.gegy1000.earth.client.map;

import net.gegy1000.earth.client.util.PolygonTessellator;
import net.gegy1000.earth.server.util.MapPoint;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

import javax.vecmath.Vector3d;
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

    private PolygonTessellator.TessellationObject area;
    private boolean built;

    public Area(World world, List<MapPoint> points, Type type) {
        this.world = world;
        this.type = type;
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
        this.bounds = new AxisAlignedBB(minX, this.lowestHeight, minZ, maxX, this.highestHeight, maxZ);
    }

    @Override
    public AxisAlignedBB getBounds() {
        return this.bounds;
    }

    @Override
    public void build() {
        this.delete();

        double top = this.highestHeight;

        this.area = new PolygonTessellator.TessellationObject(DefaultVertexFormats.POSITION_NORMAL, BUILDER, this.points, this.center, top);
        PolygonTessellator.draw(this.area);

        this.built = true;
    }

    @Override
    public void render() {
        this.type.prepareRender();

        this.area.draw();
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
        if (this.area != null) {
            this.area.delete();
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
