package net.gegy1000.earth.server.world.gen.raster.adapter;

import net.gegy1000.earth.server.world.gen.raster.GenData;
import net.minecraft.block.state.IBlockState;

public class HeightExpandAdapter extends GenAdapter {
    private final int amount;

    public HeightExpandAdapter(GenAdapter parent, int amount) {
        super(parent);
        this.amount = amount;
    }

    @Override
    public boolean adapt(int x, int z, IBlockState state, IBlockState[] states) {
        if (this.parent.adapt(x, z, state, states)) {
            IBlockState placing = null;
            int place = this.amount - 1;
            for (int y = 0; y < 256; y++) {
                if (placing == null) {
                    IBlockState s = states[y];
                    if (s != null && s != GenData.DEFAULT) {
                        placing = s;
                    }
                } else {
                    states[y] = placing;
                    if (--place <= 0) {
                        return true;
                    }
                }
            }
            return true;
        }
        return false;
    }
}
