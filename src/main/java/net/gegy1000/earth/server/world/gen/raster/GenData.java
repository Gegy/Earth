package net.gegy1000.earth.server.world.gen.raster;

import net.gegy1000.earth.server.world.gen.raster.adapter.DefaultAdapter;
import net.gegy1000.earth.server.world.gen.raster.adapter.GenAdapter;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.ChunkPrimer;

import java.util.List;

public class GenData {
    private final int originX;
    private final int originZ;
    private final byte[] data;
    private final int width;
    private final int height;
    private GenAdapter adapter = new DefaultAdapter();

    public GenData(int originX, int originZ, int width, int height) {
        this.originX = originX;
        this.originZ = originZ;
        this.data = new byte[width * height];
        this.width = width;
        this.height = height;
    }

    public void put(int x, int z, byte state) {
        this.data[this.index(x, z)] = state;
    }

    public byte get(int x, int z) {
        return this.data[this.index(x, z)];
    }

    public GenData adapt(GenAdapter adapter) {
        this.adapter = adapter;
        return this;
    }

    public void generate(ChunkPos mask, ChunkPrimer primer) {
        if (this.originX + this.width >= mask.getXStart() && this.originZ + this.height >= mask.getZStart() && this.originX <= mask.getXEnd() && this.originZ <= mask.getZEnd()) {
            int minX = Math.max(0, mask.getXStart() - this.originX);
            int minZ = Math.max(0, mask.getZStart() - this.originZ);
            int maxX = Math.min(this.width, (mask.getXEnd() + 1) - this.originX);
            int maxZ = Math.min(this.height, (mask.getZEnd() + 1) - this.originZ);
            this.adapter.setup(minX, minZ, maxX, maxZ, this.originX, this.originZ);
            this.adapter.adapt(this, primer);
        }
    }

    public int getOriginX() {
        return this.originX;
    }

    public int getOriginZ() {
        return this.originZ;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public int index(int x, int z) {
        return x + z * this.width;
    }

    public GenData addTo(List<GenData> data) {
        data.add(this);
        return this;
    }
}
