package net.gegy1000.earth.server.world.gen.raster.adapter;

import net.gegy1000.earth.server.world.gen.HeightProvider;
import net.gegy1000.earth.server.world.gen.raster.GenData;
import net.minecraft.block.BlockColored;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.world.chunk.ChunkPrimer;

public class RoadAdapter extends GenAdapter {
    private static final IBlockState SURFACE_STATE = Blocks.STAINED_HARDENED_CLAY.getDefaultState().withProperty(BlockColored.COLOR, EnumDyeColor.BLACK);

    private final HeightProvider provider;

    public RoadAdapter(HeightProvider provider) {
        this.provider = provider;
    }

    @Override
    public void adapt(GenData data, ChunkPrimer primer) {
        this.iterate(data, state -> {
            switch (state.getState()) {
                case SURFACE:
                    primer.setBlockState(state.getChunkX(), this.provider.provideHeight(state.getX(), state.getZ()), state.getChunkZ(), SURFACE_STATE);
                    break;
            }
        });
    }
}
