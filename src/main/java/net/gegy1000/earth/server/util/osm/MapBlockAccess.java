package net.gegy1000.earth.server.util.osm;

import net.gegy1000.earth.server.util.SubChunkPos;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;

import java.util.HashMap;
import java.util.Map;

public class MapBlockAccess {
    private Map<ChunkPos, Map<SubChunkPos, IBlockState>> creationChunks = new HashMap<>();
    private Map<ChunkPos, Map<SubChunkPos, IBlockState>> chunks;

    public MapBlockAccess(Map<ChunkPos, Map<SubChunkPos, IBlockState>> chunks) {
        this.chunks = chunks;
    }

    public void set(BlockPos pos, IBlockState state) {
        ChunkPos chunk = new ChunkPos(pos.getX() >> 4, pos.getZ() >> 4);
        Map<SubChunkPos, IBlockState> chunkBlocks = this.creationChunks.computeIfAbsent(chunk, chunkPos -> new HashMap<>());
        chunkBlocks.put(new SubChunkPos(pos), state);
    }

    public void fillY(BlockPos pos, IBlockState state, int height) {
        ChunkPos chunk = new ChunkPos(pos.getX() >> 4, pos.getZ() >> 4);
        Map<SubChunkPos, IBlockState> chunkBlocks = this.creationChunks.computeIfAbsent(chunk, chunkPos -> new HashMap<>());
        int x = pos.getX();
        int z = pos.getZ();
        for (int offset = 0; offset < height; offset++) {
            chunkBlocks.put(new SubChunkPos(x, pos.getY() + offset, z), state);
        }
    }

    public IBlockState get(BlockPos pos) {
        ChunkPos chunk = new ChunkPos(pos.getX() >> 4, pos.getZ() >> 4);
        Map<SubChunkPos, IBlockState> chunkBlocks = this.chunks.get(chunk);
        if (chunkBlocks != null) {
            return chunkBlocks.get(new SubChunkPos(pos));
        }
        return null;
    }

    public Map<ChunkPos, Map<SubChunkPos, IBlockState>> getCreationChunks() {
        return this.creationChunks;
    }
}
