package net.gegy1000.earth.server.util.osm;

import net.gegy1000.earth.Earth;
import net.gegy1000.earth.server.util.MapPoint;
import net.gegy1000.earth.server.world.gen.EarthGenerator;
import net.gegy1000.earth.server.world.gen.WorldTypeEarth;
import net.gegy1000.earth.server.world.gen.raster.GenData;
import net.gegy1000.earth.server.world.gen.raster.object.ObjectRasterization;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkPrimer;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class MapTile {
    public static final double SIZE = 0.01;

    private final World world;
    private final MapPoint minPos;
    private final MapPoint maxPos;
    private final int tileLat;
    private final int tileLon;
    private final List<GenData> generationData = new ArrayList<>();

    public MapTile(World world, int tileLat, int tileLon) {
        this.world = world;
        this.tileLat = tileLat;
        this.tileLon = tileLon;
        this.minPos = new MapPoint(world, this.tileLat * SIZE, this.tileLon * SIZE);
        this.maxPos = new MapPoint(world, this.minPos.getLatitude() + SIZE, this.minPos.getLongitude() + SIZE);
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
        return this.tileLon + 18000 << 16 | this.tileLat + 9000;
    }

    public void load() {
        EarthGenerator generator = WorldTypeEarth.getGenerator(this.world);
        ObjectRasterization.GRAPHICS.frame(this.minPos, this.maxPos);
        try (InputStream in = OpenStreetMap.openStream(this)) {
            List<MapObject> mapObjects = OpenStreetMap.parse(in);
            for (MapObject object : mapObjects) {
                this.rasterize(generator, object);
            }
        } catch (Exception e) {
            Earth.LOGGER.error("Failed to load map tile at {} {}", this.tileLat, this.tileLon, e);
        }
    }

    private void rasterize(EarthGenerator generator, MapObject object) {
        ObjectRasterization rasterization = ObjectRasterization.get(object);
        if (rasterization != null) {
            rasterization.rasterize(generator, object, this.generationData);
        }
    }

    public void generate(ChunkPos chunk, ChunkPrimer primer) {
        for (GenData data : this.generationData) {
            data.generate(chunk, primer);
        }
    }

    public void clear() {
        this.generationData.clear();
    }

    public MapPoint getMinPos() {
        return this.minPos;
    }

    public MapPoint getMaxPos() {
        return this.maxPos;
    }

    public int getTileLat() {
        return this.tileLat;
    }

    public int getTileLon() {
        return this.tileLon;
    }
}
