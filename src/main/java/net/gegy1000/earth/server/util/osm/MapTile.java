package net.gegy1000.earth.server.util.osm;

import net.gegy1000.earth.server.util.MapPoint;
import net.gegy1000.earth.server.util.SubChunkPos;
import net.gegy1000.earth.server.util.TempFileUtil;
import net.gegy1000.earth.server.util.osm.object.MapObject;
import net.gegy1000.earth.server.world.gen.EarthGenerator;
import net.gegy1000.earth.server.world.gen.WorldTypeEarth;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkPrimer;
import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class MapTile {
    public static final double SIZE = 0.01;

    private final World world;
    private final MapPoint minPos;
    private final MapPoint maxPos;
    private final int tileLat;
    private final int tileLon;
    private final Map<ChunkPos, Map<SubChunkPos, IBlockState>> blocks = new HashMap<>();

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
        try {
            File file = TempFileUtil.getTempFile("osm/" + this.tileLat + "_" + this.tileLon + ".tile");
            if (file.exists()) {
                Set<MapObject> mapObjects = OpenStreetMap.parse(this.world, new GZIPInputStream(new FileInputStream(file)));
                this.generateBlockCache(generator, mapObjects);
            } else {
                file.getParentFile().mkdirs();
                double minLatitude = Math.min(this.minPos.getLatitude(), this.maxPos.getLatitude());
                double minLongitude = Math.min(this.minPos.getLongitude(), this.maxPos.getLongitude());
                double maxLatitude = Math.max(this.minPos.getLatitude(), this.maxPos.getLatitude());
                double maxLongitude = Math.max(this.minPos.getLongitude(), this.maxPos.getLongitude());
                MapPoint min = new MapPoint(this.world, minLatitude, minLongitude);
                MapPoint max = new MapPoint(this.world, maxLatitude, maxLongitude);
                InputStream in = OpenStreetMap.openStream(min, max);
                if (in != null) {
                    byte[] bytes = IOUtils.toByteArray(in);
                    ByteArrayInputStream writeIn = new ByteArrayInputStream(bytes);
                    OutputStream fileOut = new GZIPOutputStream(new FileOutputStream(file));
                    IOUtils.copy(writeIn, fileOut);
                    writeIn.close();
                    fileOut.close();
                    Set<MapObject> mapObjects = OpenStreetMap.parse(this.world, new ByteArrayInputStream(bytes));
                    this.generateBlockCache(generator, mapObjects);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void generateBlockCache(EarthGenerator generator, Set<MapObject> mapObjects) {
        for (MapObject mapObject : mapObjects) {
            Map<BlockPos, IBlockState> objectBlocks = mapObject.generate(generator);
            for (Map.Entry<BlockPos, IBlockState> entry : objectBlocks.entrySet()) {
                BlockPos pos = entry.getKey();
                ChunkPos chunkPos = new ChunkPos(pos.getX() >> 4, pos.getZ() >> 4);
                Map<SubChunkPos, IBlockState> chunkBlocks = this.getChunkBlocks(chunkPos);
                chunkBlocks.put(new SubChunkPos(pos.getX(), pos.getY(), pos.getZ()), entry.getValue());
            }
        }
        //Test code
        double minX = Math.min(this.minPos.getX(), this.maxPos.getX());
        double minZ = Math.min(this.minPos.getZ(), this.maxPos.getZ());
        double maxX = Math.max(this.minPos.getX(), this.maxPos.getX());
        double maxZ = Math.max(this.minPos.getZ(), this.maxPos.getZ());
        for (int x = (int) minX; x < maxX; x++) {
            for (int z = (int) minZ; z < maxZ; z++) {
                BlockPos pos = new BlockPos(x, 65, z);
                ChunkPos chunkPos = new ChunkPos(pos.getX() >> 4, pos.getZ() >> 4);
                Map<SubChunkPos, IBlockState> chunkBlocks = this.getChunkBlocks(chunkPos);
                chunkBlocks.put(new SubChunkPos(pos.getX(), pos.getY(), pos.getZ()), Blocks.REDSTONE_BLOCK.getDefaultState());
            }
        }
    }

    private Map<SubChunkPos, IBlockState> getChunkBlocks(ChunkPos chunkPos) {
        Map<SubChunkPos, IBlockState> chunkBlocks = this.blocks.get(chunkPos);
        if (chunkBlocks == null) {
            chunkBlocks = new HashMap<>();
            this.blocks.put(chunkPos, chunkBlocks);
        }
        return chunkBlocks;
    }

    public void generate(ChunkPos pos, ChunkPrimer primer) {
        Map<SubChunkPos, IBlockState> blocks = this.blocks.get(pos);
        if (blocks != null) {
            for (Map.Entry<SubChunkPos, IBlockState> entry : blocks.entrySet()) {
                SubChunkPos blockPos = entry.getKey();
                primer.setBlockState(blockPos.getX(), blockPos.getY(), blockPos.getZ(), entry.getValue());
            }
        }
    }
}
