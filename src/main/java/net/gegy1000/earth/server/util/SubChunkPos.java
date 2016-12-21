package net.gegy1000.earth.server.util;

public class SubChunkPos {
    private final byte horizontal;
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
    }
}
