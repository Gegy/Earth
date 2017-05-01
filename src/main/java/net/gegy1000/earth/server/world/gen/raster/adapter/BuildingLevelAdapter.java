package net.gegy1000.earth.server.world.gen.raster.adapter;

import net.gegy1000.earth.server.world.gen.HeightProvider;
import net.minecraft.block.state.IBlockState;

public class BuildingLevelAdapter extends GenAdapter {
    private final HeightProvider provider;
    private final int[] levelHeights;

    public BuildingLevelAdapter(HeightProvider provider, int[] levelHeights) {
        this.provider = provider;
        this.levelHeights = levelHeights;
    }

    @Override
    public boolean adapt(int x, int z, IBlockState state, IBlockState[] states) {
        int baseHeight = this.provider.provideHeight(x, z);
        for (int levelHeight : this.levelHeights) {
            int height = baseHeight + levelHeight;
            if (height < 256) {
                states[height] = state;
            }
        }
        return true;
    }
}
