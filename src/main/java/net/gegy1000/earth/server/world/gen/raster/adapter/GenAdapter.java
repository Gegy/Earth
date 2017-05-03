package net.gegy1000.earth.server.world.gen.raster.adapter;

import net.gegy1000.earth.server.world.gen.raster.ConstantRasterIds;
import net.gegy1000.earth.server.world.gen.raster.GenData;
import net.minecraft.world.chunk.ChunkPrimer;

import java.util.function.Consumer;

public abstract class GenAdapter implements ConstantRasterIds {
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
                if (state != AIR) {
                    consumer.accept(new State(x + this.originX, z + this.originZ, state));
                }
            }
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
