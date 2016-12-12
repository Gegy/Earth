package net.gegy1000.earth.client.map;

import net.gegy1000.earth.server.util.MapPoint;
import net.gegy1000.earth.server.util.osm.OpenStreetMap;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

public class MapTile {
    public static final double SIZE = 0.01;

    private final MapPoint minPos;
    private final MapPoint maxPos;
    private final MapPoint center;
    private final int tileLat;
    private final int tileLon;
    private final BlockPos.MutableBlockPos centerBlock;

    private final Set<MapObject> mapObjects = new HashSet<>();

    private int displayList;
    private boolean built;

    public MapTile(World world, int tileLat, int tileLon) {
        this.tileLat = tileLat;
        this.tileLon = tileLon;
        this.minPos = new MapPoint(world, this.tileLat * SIZE, this.tileLon * SIZE);
        this.maxPos = new MapPoint(world, this.minPos.getLatitude() + SIZE, this.minPos.getLongitude() + SIZE);
        this.center = new MapPoint(world, this.minPos.getLatitude() + (SIZE / 2), this.minPos.getLongitude() + (SIZE / 2));
        this.centerBlock = new BlockPos.MutableBlockPos((int) this.center.getX(), 0, (int) this.center.getZ());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof MapTile) {
            MapTile tile = (MapTile) obj;
            return tile.tileLat == this.tileLat && tile.tileLon == this.tileLon;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return this.tileLat << 8 | this.tileLon;
    }

    public BlockPos getCenter(int y) {
        this.centerBlock.setY(y);
        return this.centerBlock;
    }

    public void load(World world) {
        double minLatitude = Math.min(this.minPos.getLatitude(), this.maxPos.getLatitude());
        double minLongitude = Math.min(this.minPos.getLongitude(), this.maxPos.getLongitude());
        double maxLatitude = Math.max(this.minPos.getLatitude(), this.maxPos.getLatitude());
        double maxLongitude = Math.max(this.minPos.getLongitude(), this.maxPos.getLongitude());
        MapPoint min = new MapPoint(world, minLatitude, minLongitude);
        MapPoint max = new MapPoint(world, maxLatitude, maxLongitude);
        try {
            InputStream in = OpenStreetMap.openStream(min, max);
            if (in != null) {
                this.mapObjects.addAll(OpenStreetMap.parse(world, in));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Set<MapObject> getMapObjects() {
        return this.mapObjects;
    }

    public void render(Tessellator tessellator, VertexBuffer builder, double viewX, double viewY, double viewZ) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(this.center.getX() - viewX, -viewY, this.center.getZ() - viewZ);
        if (this.built) {
            GlStateManager.callList(this.displayList);
        } else {
            this.displayList = GLAllocation.generateDisplayLists(1);
            GlStateManager.glNewList(this.displayList, GL11.GL_COMPILE);
            for (MapObject mapObject : this.mapObjects) {
                builder.setTranslation(0, 0, 0);
                mapObject.render(tessellator, builder, this.center);
            }
            GlStateManager.glEndList();
            this.built = true;
        }
        GlStateManager.popMatrix();
    }

    public void delete() {
        if (this.built) {
            GlStateManager.glDeleteLists(this.displayList, 1);
        }
    }
}
