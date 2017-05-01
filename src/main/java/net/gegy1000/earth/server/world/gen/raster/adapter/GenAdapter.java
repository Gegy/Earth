package net.gegy1000.earth.server.world.gen.raster.adapter;

import net.minecraft.block.state.IBlockState;

public abstract class GenAdapter {
    protected final GenAdapter parent;

    public GenAdapter() {
        this(null);
    }

    public GenAdapter(GenAdapter parent) {
        this.parent = parent;
    }

    public abstract boolean adapt(int x, int z, IBlockState state, IBlockState[] states);
}
