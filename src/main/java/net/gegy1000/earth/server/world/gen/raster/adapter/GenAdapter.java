package net.gegy1000.earth.server.world.gen.raster.adapter;

import net.gegy1000.earth.server.world.gen.raster.ConstantRasterIds;
import net.gegy1000.earth.server.world.gen.raster.GenData;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.world.chunk.ChunkPrimer;

import java.util.function.Consumer;

public abstract class GenAdapter {
    public static final IBlockState AIR_STATE = Blocks.AIR.getDefaultState();

    protected int minX;
    protected int minZ;
    protected int maxX;
    protected int maxZ;
    protected int originX;
    protected int originZ;

    public abstract void adapt(GenData data, ChunkPrimer primer);

    protected void iterate(GenData data, Consumer<State> consumer) {
        for (int z = this.minZ; z < this.maxZ; z++) {
            for (int x = this.minX; x < this.maxX; x++) {
                byte state = data.get(x, z);
                if (state != ConstantRasterIds.AIR) {
                    consumer.accept(new State(x + this.originX, z + this.originZ, state));
                }
            }
        }
    }

    protected void setSafe(int x, int y, int z, IBlockState state, ChunkPrimer primer) {
        if (x < 0 || y < 0 || z < 0 || x > 15 || y > 255 || z > 15) {
            return;
        }
        if (primer.getBlockState(x, y, z) == AIR_STATE) {
            primer.setBlockState(x, y, z, state);
        }
    }

    public final void setup(int minX, int minZ, int maxX, int maxZ, int originX, int originZ) {
        this.minX = minX;
        this.minZ = minZ;
        this.maxX = maxX;
        this.maxZ = maxZ;
        this.originX = originX;
        this.originZ = originZ;
    }

    protected class State {
        private int x;
        private int z;
        private byte state;

        public State(int x, int z, byte state) {
            this.x = x;
            this.z = z;
            this.state = state;
        }

        public int getX() {
            return this.x;
        }

        public int getZ() {
            return this.z;
        }

        public int getChunkX() {
            return this.x & 15;
        }

        public int getChunkZ() {
            return this.z & 15;
        }

        public int getState() {
            return this.state & 0xFF;
        }
    }
}
