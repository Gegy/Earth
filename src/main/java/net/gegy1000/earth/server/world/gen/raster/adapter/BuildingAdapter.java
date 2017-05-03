package net.gegy1000.earth.server.world.gen.raster.adapter;

import net.gegy1000.earth.server.world.gen.HeightProvider;
import net.gegy1000.earth.server.world.gen.raster.GenData;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.chunk.ChunkPrimer;

public class BuildingAdapter extends GenAdapter {
    private static final IBlockState WALL_STATE = Blocks.PLANKS.getDefaultState();
    private static final IBlockState FLOOR_STATE = Blocks.COBBLESTONE.getDefaultState();
    private static final IBlockState PILLAR_STATE = Blocks.LOG.getDefaultState();
    private static final IBlockState ROOF_STATE = Blocks.WOODEN_SLAB.getDefaultState().withProperty(BlockPlanks.VARIANT, BlockPlanks.EnumType.SPRUCE);

    private final HeightProvider heightProvider;
    private final int[] levelHeights;
    private final int buildingHeight;

    public BuildingAdapter(HeightProvider heightProvider, int[] levelHeights) {
        this.heightProvider = heightProvider;
        this.levelHeights = levelHeights;
        this.buildingHeight = levelHeights[levelHeights.length - 1];
    }

    @Override
    public void adapt(GenData data, ChunkPrimer primer) {
        this.iterate(data, state -> {
            int groundHeight = this.heightProvider.provideHeight(state.getX(), state.getZ()) + 1;
            int x = state.getChunkX();
            int z = state.getChunkZ();
            int totalHeight = MathHelper.clamp(groundHeight + this.buildingHeight, 0, 255);
            switch (state.getState()) {
                case WALL:
                    for (int y = groundHeight; y < totalHeight; y++) {
                        if (y == groundHeight) {
                            primer.setBlockState(x, y, z, FLOOR_STATE);
                        } else {
                            primer.setBlockState(x, y, z, WALL_STATE);
                        }
                    }
                    primer.setBlockState(x, totalHeight, z, ROOF_STATE);
                    break;
                case WALL_LINING:
                    for (int i = 1; i < this.levelHeights.length; i++) {
                        int levelHeight = this.levelHeights[i];
                        int height = groundHeight + levelHeight;
                        if (height < totalHeight) {
                            primer.setBlockState(x, height, z, PILLAR_STATE);
                        } else {
                            break;
                        }
                    }
                    break;
                case PILLAR:
                    for (int y = groundHeight; y < totalHeight; y++) {
                        primer.setBlockState(x, y, z, PILLAR_STATE);
                    }
                    break;
                case SURFACE:
                    for (int i = 0; i < this.levelHeights.length; i++) {
                        int levelHeight = this.levelHeights[i];
                        int height = groundHeight + levelHeight;
                        if (height <= totalHeight) {
                            primer.setBlockState(x, height - 1, z, i == 0 ? FLOOR_STATE : WALL_STATE);
                        } else {
                            break;
                        }
                    }
                    primer.setBlockState(x, totalHeight, z, ROOF_STATE);
                    break;
            }
        });
    }
}
