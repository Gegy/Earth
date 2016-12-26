package net.gegy1000.earth.server.util;

import net.minecraft.util.math.BlockPos;

public class SubChunkPos {
    /*private final byte horizontal;
    private final byte y;

    public SubChunkPos(int x, int y, int z) {
        this.horizontal = (byte) ((x & 15) | ((z & 15) << 4));
        this.y = (byte) (y & 0xFF);
    }

    public int getX() {
        return this.horizontal & 15;
    }

    public int getZ() {
        return (this.horizontal >> 4) & 15;
    }

    public int getY() {
        return this.y & 0xFF;
    }

    @Override
    public int hashCode() {
        return this.horizontal | (this.y << 8);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SubChunkPos) {
            SubChunkPos pos = (SubChunkPos) obj;
            return pos.horizontal == this.horizontal && pos.y == this.y;
        }
        return false;
    }*/

    private final byte x;
    private final byte y;
    private final byte z;

    public SubChunkPos(int x, int y, int z) {
        this.x = (byte) (x & 15);
        this.y = (byte) y;
        this.z = (byte) (z & 15);
    }

    public SubChunkPos(BlockPos pos) {
        this(pos.getX(), pos.getY(), pos.getZ());
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y & 0xFF;
    }

    public int getZ() {
        return this.z;
    }

    @Override
    public int hashCode() {
        return this.x | (this.z << 4) | (this.y << 8);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SubChunkPos) {
            SubChunkPos pos = (SubChunkPos) obj;
            return pos.x == this.x && pos.y == this.y && pos.z == this.z;
        }
        return false;
    }
}
