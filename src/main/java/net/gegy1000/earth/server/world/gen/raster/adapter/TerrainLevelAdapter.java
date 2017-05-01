package net.gegy1000.earth.server.world.gen.raster.adapter;

import net.gegy1000.earth.server.world.gen.HeightProvider;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.MathHelper;

public class TerrainLevelAdapter extends GenAdapter {
    private final HeightProvider provider;

    public TerrainLevelAdapter(HeightProvider provider) {
        this.provider = provider;
    }

    @Override
    public boolean adapt(int x, int z, IBlockState state, IBlockState[] states) {
        states[MathHelper.clamp(this.provider.provideHeight(x, z), 0, 255)] = state;
        return true;
    }
}
