package net.gegy1000.earth.server.world.gen.raster;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.ChunkPrimer;

public class BlankGenData extends GenData {
    public BlankGenData() {
        super(BlockPos.ORIGIN, 0, 0);
    }

    @Override
    public void put(int i, IBlockState state) {
    }

    @Override
    public IBlockState get(int i) {
        return DEFAULT;
    }

    @Override
    public void generate(ChunkPos mask, ChunkPrimer primer) {
    }
}
