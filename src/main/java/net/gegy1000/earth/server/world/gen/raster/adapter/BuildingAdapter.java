package net.gegy1000.earth.server.world.gen.raster.adapter;

import net.gegy1000.earth.server.world.gen.HeightProvider;
import net.gegy1000.earth.server.world.gen.raster.ConstantRasterIds;
import net.gegy1000.earth.server.world.gen.raster.GenData;
import net.gegy1000.earth.server.world.gen.raster.object.meta.BuildingMaterial;
import net.gegy1000.earth.server.world.gen.raster.object.meta.BuildingMetadata;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.chunk.ChunkPrimer;

public class BuildingAdapter extends GenAdapter {
    private final HeightProvider heightProvider;
    private final BuildingMetadata metadata;

    public BuildingAdapter(HeightProvider heightProvider, BuildingMetadata metadata) {
        this.heightProvider = heightProvider;
        this.metadata = metadata;
    }

    @Override
    public void adapt(GenData data, ChunkPrimer primer) {
        this.iterate(data, state -> {
            int groundHeight = this.heightProvider.provideHeight(state.getX(), state.getZ()) + 1;
            int minHeight = groundHeight + this.metadata.getMinHeight();
            int x = state.getChunkX();
            int z = state.getChunkZ();
            int[] levelHeights = this.metadata.getLevelHeights();
            int totalHeight = MathHelper.clamp(groundHeight + levelHeights[levelHeights.length - 1], 0, 255);
            BuildingMaterial material = this.metadata.getMaterial();
            IBlockState wall = material.getWall();
            IBlockState base = material.getBase();
            IBlockState lining = material.getLining();
            IBlockState floor = material.getFloor();
            IBlockState roof = material.getDefaultRoofSlab();
            IBlockState upperRoof = roof.withProperty(BlockSlab.HALF, BlockSlab.EnumBlockHalf.TOP);
            IBlockState glass = material.getGlass();
            switch (state.getState()) {
                case ConstantRasterIds.WALL:
                    for (int y = minHeight; y < totalHeight; y++) {
                        if (y == groundHeight && base != null) {
                            primer.setBlockState(x, y, z, base);
                        } else {
                            primer.setBlockState(x, y, z, wall);
                        }
                    }
                    this.setSafe(x, totalHeight, z, roof, primer);
                    break;
                case ConstantRasterIds.WALL_LINING:
                    for (int i = 1; i < levelHeights.length; i++) {
                        int levelHeight = levelHeights[i];
                        int height = groundHeight + levelHeight;
                        if (height < totalHeight) {
                            primer.setBlockState(x, height, z, lining);
                        } else {
                            break;
                        }
                    }
                    break;
                case ConstantRasterIds.PILLAR:
                    for (int y = minHeight; y < totalHeight; y++) {
                        primer.setBlockState(x, y, z, lining);
                    }
                    break;
                case ConstantRasterIds.SURFACE:
                    for (int levelHeight : levelHeights) {
                        int height = groundHeight + levelHeight;
                        if (height <= totalHeight) {
                            if (primer.getBlockState(x, height - 1, z) != wall) {
                                primer.setBlockState(x, height - 1, z, floor);
                            }
                        } else {
                            break;
                        }
                    }
                    this.setSafe(x, totalHeight, z, roof, primer);
                    break;
                case ConstantRasterIds.ROOF:
                    this.setSafe(x, totalHeight - 1, z, upperRoof, primer);
                    break;
                case ConstantRasterIds.WINDOW:
                    int previousHeight = minHeight + 1;
                    for (int levelHeight : levelHeights) {
                        int height = groundHeight + levelHeight + 1;
                        if (height <= totalHeight) {
                            int windowHeight = Math.max(1, height - previousHeight - 3);
                            for (int y = height; y < height + windowHeight; y++) {
                                IBlockState currentState = primer.getBlockState(x, y, z);
                                if (currentState == AIR_STATE || currentState == wall) {
                                    primer.setBlockState(x, y, z, glass);
                                }
                            }
                        } else {
                            break;
                        }
                        previousHeight = height;
                    }
                    break;
            }
        });
    }
}
