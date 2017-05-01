package net.gegy1000.earth.server.world.gen.raster;

import net.gegy1000.earth.server.world.gen.raster.adapter.GenAdapter;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.ChunkPrimer;

public class GenData {
    public static final IBlockState DEFAULT = Blocks.AIR.getDefaultState();

    private final BlockPos origin;
    private final IBlockState[] states;
    private final int width;
    private final int height;
    private GenAdapter adapter;

    public GenData(BlockPos origin, int width, int height) {
        this.origin = origin;
        this.states = new IBlockState[width * height];
        this.width = width;
        this.height = height;
    }

    public void put(int x, int z, IBlockState state) {
        if (x >= 0 && z >= 0 && x <= this.width && z <= this.height) {
            this.put(this.index(x, z), state);
        }
    }

    public void put(int i, IBlockState state) {
        this.states[i] = state;
    }

    public IBlockState get(int x, int z) {
        return this.get(this.index(x, z));
    }

    public IBlockState get(int i) {
        IBlockState state = this.states[i];
        if (state == null) {
            return DEFAULT;
        }
        return state;
    }

    public GenData adapt(GenAdapter adapter) {
        this.adapter = adapter;
        return this;
    }

    public void generate(ChunkPos mask, ChunkPrimer primer) {
        int originX = this.origin.getX();
        int originY = this.origin.getY();
        int originZ = this.origin.getZ();
        if (originX + this.width >= mask.getXStart() && originZ + this.height >= mask.getZStart() && originX <= mask.getXEnd() && originZ <= mask.getZEnd()) {
            int minX = mask.getXStart() - originX;
            int minZ = mask.getZStart() - originZ;
            int maxX = mask.getXEnd() - originX;
            int maxZ = mask.getZEnd() - originZ;
            int index = 0;
            IBlockState[] verticalColumn = new IBlockState[256];
            for (int z = 0; z < this.height; z++) {
                for (int x = 0; x < this.width; x++) {
                    if (x >= minX && z >= minZ && x <= maxX && z <= maxZ) {
                        int globalX = originX + x;
                        int globalZ = originZ + z;
                        IBlockState state = this.get(index);
                        if (this.adapter != null) {
                            if (this.adapter.adapt(globalX, globalZ, state, verticalColumn)) {
                                for (int y = originY; y < 256; y++) {
                                    IBlockState s = verticalColumn[y];
                                    if (s != null && s != DEFAULT) {
                                        primer.setBlockState(globalX & 15, y, globalZ & 15, s);
                                    }
                                    verticalColumn[y] = null;
                                }
                            }
                        } else {
                            primer.setBlockState(x, 0, z, state);
                        }
                    }
                    index++;
                }
            }
        }
    }

    public BlockPos getOrigin() {
        return this.origin;
    }

    private int index(int x, int z) {
        return x + z * this.width;
    }
}
