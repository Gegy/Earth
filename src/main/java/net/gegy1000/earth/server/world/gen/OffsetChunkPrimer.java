package net.gegy1000.earth.server.world.gen;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.world.chunk.ChunkPrimer;

public class OffsetChunkPrimer extends ChunkPrimer {
    private ChunkPrimer parent;
    private int offsetY;

    public OffsetChunkPrimer(ChunkPrimer parent, int offsetY) {
        this.parent = parent;
        this.offsetY = offsetY;
    }

    @Override
    public IBlockState getBlockState(int x, int y, int z) {
        y -= this.offsetY;
        if (y < 0) {
            return Blocks.BEDROCK.getDefaultState();
        }
        return this.parent.getBlockState(x, y, z);
    }

    @Override
    public void setBlockState(int x, int y, int z, IBlockState state) {
        y -= this.offsetY;
        if (y > 0) {
            this.parent.setBlockState(x, y, z, state);
        }
    }

    @Override
    public int findGroundBlockIdx(int x, int z) {
        return this.parent.findGroundBlockIdx(x, z) - this.offsetY;
    }
}