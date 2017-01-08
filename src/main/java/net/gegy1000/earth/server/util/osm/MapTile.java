package net.gegy1000.earth.server.util.osm;

import net.gegy1000.earth.server.util.MapPoint;
import net.gegy1000.earth.server.util.SubChunkPos;
import net.gegy1000.earth.server.util.TempFileUtil;
import net.gegy1000.earth.server.util.osm.object.MapObject;
import net.gegy1000.earth.server.util.osm.object.MapObjectType;
import net.gegy1000.earth.server.util.raster.Rasterize;
import net.gegy1000.earth.server.world.gen.EarthGenerator;
import net.gegy1000.earth.server.world.gen.WorldTypeEarth;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkPrimer;
import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class MapTile {
    public static final double SIZE = 0.01;

    private final World world;
    private final MapPoint minPos;
    private final MapPoint maxPos;
    private final int tileLat;
    private final int tileLon;
    private final Map<ChunkPos, Map<SubChunkPos, IBlockState>> chunks = new HashMap<>();

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
        double minLatitude = Math.min(this.minPos.getLatitude(), this.maxPos.getLatitude());
        double minLongitude = Math.min(this.minPos.getLongitude(), this.maxPos.getLongitude());
        double maxLatitude = Math.max(this.minPos.getLatitude(), this.maxPos.getLatitude());
        double maxLongitude = Math.max(this.minPos.getLongitude(), this.maxPos.getLongitude());
        MapPoint min = new MapPoint(this.world, minLatitude, minLongitude);
        MapPoint max = new MapPoint(this.world, maxLatitude, maxLongitude);
        Rasterize.setLimits(MathHelper.floor(min.getX()), MathHelper.floor(min.getZ()), MathHelper.ceil(max.getX()), MathHelper.ceil(max.getZ()));
        try {
            File file = TempFileUtil.getTempFile("osm/" + this.tileLat + "_" + this.tileLon + ".tile");
            if (file.exists()) {
                EnumMap<MapObjectType, List<MapObject>> mapObjects = OpenStreetMap.parse(this.world, new GZIPInputStream(new FileInputStream(file)));
                this.generateBlockCache(generator, mapObjects);
            } else {
                file.getParentFile().mkdirs();
                InputStream in = OpenStreetMap.openStream(min, max);
                if (in != null) {
                    byte[] bytes = IOUtils.toByteArray(in);
                    ByteArrayInputStream writeIn = new ByteArrayInputStream(bytes);
                    OutputStream fileOut = new GZIPOutputStream(new FileOutputStream(file));
                    IOUtils.copy(writeIn, fileOut);
                    writeIn.close();
                    fileOut.close();
                    EnumMap<MapObjectType, List<MapObject>> mapObjects = OpenStreetMap.parse(this.world, new ByteArrayInputStream(bytes));
                    this.generateBlockCache(generator, mapObjects);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void generateBlockCache(EarthGenerator generator, EnumMap<MapObjectType, List<MapObject>> mapObjects) {
        for (int pass = 0; pass < 2; pass++) {
            for (MapObjectType type : MapObjectType.values()) {
                List<MapObject> typeObjects = mapObjects.get(type);
                if (typeObjects != null) {
                    if (pass == 0) {
                        typeObjects.sort(Comparator.comparingInt(MapObject::getLayer));
                    }
                    for (MapObject mapObject : typeObjects) {
                        this.generatePass(generator, pass, mapObject);
                    }
                }
            }
        }
    }

    private void generatePass(EarthGenerator generator, int pass, MapObject object) {
        MapBlockAccess storage = new MapBlockAccess(this.chunks);
        object.generate(generator, storage, pass);
        this.applyStorage(storage);
    }

    private void applyStorage(MapBlockAccess storage) {
        Map<ChunkPos, Map<SubChunkPos, IBlockState>> chunks = storage.getCreationChunks();
        for (Map.Entry<ChunkPos, Map<SubChunkPos, IBlockState>> entry : chunks.entrySet()) {
            Map<SubChunkPos, IBlockState> chunkBlocks = this.chunks.computeIfAbsent(entry.getKey(), pos -> new HashMap<>());
            Map<SubChunkPos, IBlockState> objectBlocks = entry.getValue();
            for (Map.Entry<SubChunkPos, IBlockState> block : objectBlocks.entrySet()) {
                chunkBlocks.putIfAbsent(block.getKey(), block.getValue());
            }
        }
    }

    public void generate(ChunkPos chunk, ChunkPrimer primer) {
        Map<SubChunkPos, IBlockState> blocks = this.chunks.remove(chunk);
        if (blocks != null) {
            for (Map.Entry<SubChunkPos, IBlockState> entry : blocks.entrySet()) {
                SubChunkPos pos = entry.getKey();
                primer.setBlockState(pos.getX(), pos.getY(), pos.getZ(), entry.getValue());
            }
        }
    }

    public void clear() {
        this.chunks.clear();
    }
}
