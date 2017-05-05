package net.gegy1000.earth.server.world.gen.raster.object.meta;

import net.minecraft.block.BlockColored;
import net.minecraft.block.BlockOldLog;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.BlockQuartz;
import net.minecraft.block.BlockSandStone;
import net.minecraft.block.BlockStoneSlab;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;

public class BuildingMaterial {
    public static final BuildingMaterial BRICK = new BuildingMaterial("brick")
            .withWall(Blocks.BRICK_BLOCK.getDefaultState())
            .withLining(Blocks.STONEBRICK.getDefaultState())
            .withFloor(Blocks.STONEBRICK.getDefaultState())
            .withDefaultRoofSlab(Blocks.STONE_SLAB.getDefaultState().withProperty(BlockStoneSlab.VARIANT, BlockStoneSlab.EnumType.SMOOTHBRICK))
            .withGlass(Blocks.STAINED_GLASS.getDefaultState().withProperty(BlockColored.COLOR, EnumDyeColor.BLACK));
    public static final BuildingMaterial WOOD = new BuildingMaterial("wood")
            .withWall(Blocks.PLANKS.getDefaultState().withProperty(BlockPlanks.VARIANT, BlockPlanks.EnumType.BIRCH))
            .withLining(Blocks.QUARTZ_BLOCK.getDefaultState().withProperty(BlockQuartz.VARIANT, BlockQuartz.EnumType.LINES_Y))
            .withFloor(Blocks.QUARTZ_BLOCK.getDefaultState())
            .withDefaultRoofSlab(Blocks.STONE_SLAB.getDefaultState().withProperty(BlockStoneSlab.VARIANT, BlockStoneSlab.EnumType.SMOOTHBRICK))
            .withGlass(Blocks.STAINED_GLASS.getDefaultState().withProperty(BlockColored.COLOR, EnumDyeColor.WHITE));
    public static final BuildingMaterial CONCRETE = new BuildingMaterial("concrete")
            .withWall(Blocks.STONEBRICK.getDefaultState())
            .withLining(Blocks.STAINED_HARDENED_CLAY.getDefaultState().withProperty(BlockColored.COLOR, EnumDyeColor.CYAN))
            .withFloor(Blocks.STONEBRICK.getDefaultState())
            .withDefaultRoofSlab(Blocks.STONE_SLAB.getDefaultState().withProperty(BlockStoneSlab.VARIANT, BlockStoneSlab.EnumType.QUARTZ))
            .withGlass(Blocks.STAINED_GLASS.getDefaultState().withProperty(BlockColored.COLOR, EnumDyeColor.BLACK));
    public static final BuildingMaterial STONE = new BuildingMaterial("stone")
            .withWall(Blocks.COBBLESTONE.getDefaultState())
            .withLining(Blocks.STONEBRICK.getDefaultState())
            .withFloor(Blocks.STONEBRICK.getDefaultState())
            .withDefaultRoofSlab(Blocks.STONE_SLAB.getDefaultState().withProperty(BlockStoneSlab.VARIANT, BlockStoneSlab.EnumType.NETHERBRICK))
            .withGlass(Blocks.STAINED_GLASS.getDefaultState().withProperty(BlockColored.COLOR, EnumDyeColor.BLACK));
    public static final BuildingMaterial SANDSTONE = new BuildingMaterial("sandstone")
            .withWall(Blocks.SANDSTONE.getDefaultState())
            .withLining(Blocks.SANDSTONE.getDefaultState().withProperty(BlockSandStone.TYPE, BlockSandStone.EnumType.SMOOTH))
            .withFloor(Blocks.SANDSTONE.getDefaultState())
            .withDefaultRoofSlab(Blocks.STONE_SLAB.getDefaultState().withProperty(BlockStoneSlab.VARIANT, BlockStoneSlab.EnumType.SAND))
            .withGlass(Blocks.STAINED_GLASS.getDefaultState().withProperty(BlockColored.COLOR, EnumDyeColor.WHITE));
    public static final BuildingMaterial METAL = new BuildingMaterial("metal")
            .withWall(Blocks.IRON_BLOCK.getDefaultState())
            .withLining(Blocks.IRON_BLOCK.getDefaultState())
            .withFloor(Blocks.STONEBRICK.getDefaultState())
            .withDefaultRoofSlab(Blocks.STONE_SLAB.getDefaultState().withProperty(BlockStoneSlab.VARIANT, BlockStoneSlab.EnumType.QUARTZ))
            .withGlass(Blocks.GLASS.getDefaultState());
    public static final BuildingMaterial TIMBER_FRAMING = new BuildingMaterial("timber_framing")
            .withWall(Blocks.SANDSTONE.getDefaultState().withProperty(BlockSandStone.TYPE, BlockSandStone.EnumType.SMOOTH))
            .withBase(Blocks.BRICK_BLOCK.getDefaultState())
            .withLining(Blocks.LOG.getDefaultState().withProperty(BlockOldLog.VARIANT, BlockPlanks.EnumType.SPRUCE))
            .withFloor(Blocks.SANDSTONE.getDefaultState())
            .withDefaultRoofSlab(Blocks.STONE_SLAB.getDefaultState().withProperty(BlockStoneSlab.VARIANT, BlockStoneSlab.EnumType.NETHERBRICK))
            .withGlass(Blocks.STAINED_GLASS.getDefaultState().withProperty(BlockColored.COLOR, EnumDyeColor.BLACK));

    public static final BuildingMaterial[] MATERIALS = new BuildingMaterial[] { BRICK, WOOD, CONCRETE, STONE, SANDSTONE, METAL, TIMBER_FRAMING };

    private final String name;
    private IBlockState wall;
    private IBlockState lining;
    private IBlockState base;
    private IBlockState floor;
    private IBlockState defaultRoofSlab;
    private IBlockState glass;

    public BuildingMaterial(String name) {
        this.name = name;
    }

    public BuildingMaterial withWall(IBlockState wall) {
        this.wall = wall;
        return this;
    }

    public BuildingMaterial withLining(IBlockState pillar) {
        this.lining = pillar;
        return this;
    }

    public BuildingMaterial withBase(IBlockState lining) {
        this.base = lining;
        return this;
    }

    public BuildingMaterial withFloor(IBlockState floor) {
        this.floor = floor;
        return this;
    }

    public BuildingMaterial withDefaultRoofSlab(IBlockState defaultRoofSlab) {
        this.defaultRoofSlab = defaultRoofSlab;
        return this;
    }

    public BuildingMaterial withGlass(IBlockState glass) {
        this.glass = glass;
        return this;
    }

    public String getName() {
        return this.name;
    }

    public IBlockState getDefaultRoofSlab() {
        return this.defaultRoofSlab;
    }

    public IBlockState getFloor() {
        return this.floor;
    }

    public IBlockState getGlass() {
        return this.glass;
    }

    public IBlockState getBase() {
        return this.base;
    }

    public IBlockState getLining() {
        return this.lining;
    }

    public IBlockState getWall() {
        return this.wall;
    }

    public boolean applies(String material) {
        return material.equalsIgnoreCase(this.name);
    }

    public static BuildingMaterial get(String tag) {
        if (tag != null) {
            for (BuildingMaterial material : BuildingMaterial.MATERIALS) {
                if (material.applies(tag)) {
                    return material;
                }
            }
        }
        return null;
    }

    public static BuildingMaterial getDefault(String building) {
        if (building != null && !building.equalsIgnoreCase("yes")) {
            switch (building) {
                case "apartments":
                case "terrace":
                    return BRICK;
                case "bungalow":
                case "farm":
                case "barn":
                    return TIMBER_FRAMING;
                case "hotel":
                case "temple":
                case "synagogue":
                case "cathedral":
                case "chapel":
                case "ruins":
                    return SANDSTONE;
                case "dormitory":
                case "school":
                case "cabin":
                case "cowshed":
                case "stable":
                case "hut":
                    return WOOD;
                case "industrial":
                case "church":
                case "mosque":
                case "garage":
                    return STONE;
            }
        }
        return CONCRETE;
    }
}
