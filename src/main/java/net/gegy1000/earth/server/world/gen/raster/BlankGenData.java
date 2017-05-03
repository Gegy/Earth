package net.gegy1000.earth.server.world.gen.raster;

import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.ChunkPrimer;

public class BlankGenData extends GenData {
    public BlankGenData() {
        super(0, 0, 0, 0);
    }

    @Override
    public void put(int x, int z, byte state) {
    }

    @Override
    public byte get(int x, int z) {
        return ConstantRasterIds.AIR;
    }

    @Override
    public void generate(ChunkPos mask, ChunkPrimer primer) {
    }
}
