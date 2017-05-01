package net.gegy1000.earth.server.util.osm;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDirt;
import net.minecraft.block.BlockStone;
import net.minecraft.block.BlockTallGrass;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MapMaterial {
    private static Map<String, IBlockState> MATERIALS = new HashMap<>();
    private static Map<String, IBlockState> MATERIAL_STAIRS = new HashMap<>();
    private static Map<String, IBlockState> MATERIAL_BARRIER = new HashMap<>();
    private static Map<Integer, Set<Block>> BLOCK_COLOURS = new HashMap<>();

    public static void init() {
        IBlockState coarseDirt = Blocks.DIRT.getDefaultState().withProperty(BlockDirt.VARIANT, BlockDirt.DirtType.COARSE_DIRT);
        MapMaterial.addMaterial("concrete", Blocks.STONEBRICK, Blocks.STONE_BRICK_STAIRS, Blocks.COBBLESTONE_WALL);
        MapMaterial.addMaterial("metal", Blocks.IRON_BLOCK, Blocks.QUARTZ_STAIRS, Blocks.IRON_BARS);
        MapMaterial.addMaterial("bars", Blocks.IRON_BARS, Blocks.IRON_BARS, Blocks.IRON_BARS);
        MapMaterial.addMaterial("railing", Blocks.IRON_BARS, Blocks.IRON_BARS, Blocks.IRON_BARS);
        MapMaterial.addMaterial("chain", Blocks.IRON_BARS, Blocks.IRON_BARS, Blocks.IRON_BARS);
        MapMaterial.addMaterial("wood", Blocks.PLANKS, Blocks.OAK_STAIRS, Blocks.OAK_FENCE);
        MapMaterial.addMaterial("asphalt", Blocks.COAL_BLOCK, Blocks.NETHER_BRICK_STAIRS, Blocks.NETHER_BRICK_FENCE);
        MapMaterial.addMaterial("glass", Blocks.GLASS, Blocks.GLASS, Blocks.GLASS_PANE);
        MapMaterial.addMaterial("grass", coarseDirt, coarseDirt, Blocks.TALLGRASS.getDefaultState().withProperty(BlockTallGrass.TYPE, BlockTallGrass.EnumType.GRASS));
        MapMaterial.addMaterial("cobblestone", Blocks.COBBLESTONE, Blocks.STONE_STAIRS, Blocks.COBBLESTONE_WALL);
        MapMaterial.addMaterial("pebbles", Blocks.COBBLESTONE, Blocks.STONE_STAIRS, Blocks.COBBLESTONE_WALL);
        MapMaterial.addMaterial("pebblestone", Blocks.COBBLESTONE, Blocks.STONE_STAIRS, Blocks.COBBLESTONE_WALL);
        MapMaterial.addMaterial("cobblestone:flattened", Blocks.COBBLESTONE, Blocks.STONE_STAIRS, Blocks.COBBLESTONE_WALL);
        MapMaterial.addMaterial("paved", Blocks.DOUBLE_STONE_SLAB, Blocks.DOUBLE_STONE_SLAB, Blocks.DOUBLE_STONE_SLAB);
        MapMaterial.addMaterial("sidewalk", Blocks.DOUBLE_STONE_SLAB, Blocks.DOUBLE_STONE_SLAB, Blocks.DOUBLE_STONE_SLAB);
        MapMaterial.addMaterial("brick", Blocks.BRICK_BLOCK, Blocks.NETHER_BRICK_STAIRS, Blocks.NETHER_BRICK_FENCE);
        MapMaterial.addMaterial("bricks", Blocks.BRICK_BLOCK, Blocks.NETHER_BRICK_STAIRS, Blocks.NETHER_BRICK_FENCE);
        MapMaterial.addMaterial("stone", Blocks.STONE, Blocks.STONE_BRICK_STAIRS, Blocks.COBBLESTONE_WALL);
        MapMaterial.addMaterial("gravel", Blocks.GRAVEL, Blocks.GRAVEL, Blocks.GRAVEL);
        MapMaterial.addMaterial("fine_gravel", Blocks.GRAVEL, Blocks.GRAVEL, Blocks.GRAVEL);
        MapMaterial.addMaterial("sand", Blocks.SAND, Blocks.SANDSTONE_STAIRS, Blocks.BIRCH_FENCE);
        MapMaterial.addMaterial("granite", Blocks.STONE.getDefaultState().withProperty(BlockStone.VARIANT, BlockStone.EnumType.GRANITE), Blocks.STONE_STAIRS.getDefaultState(), Blocks.COBBLESTONE_WALL.getDefaultState());
        MapMaterial.addMaterial("dirt", coarseDirt, coarseDirt, coarseDirt);
        MapMaterial.addMaterial("mud", coarseDirt, coarseDirt, coarseDirt);
        MapMaterial.addMaterial("earth", coarseDirt, coarseDirt, coarseDirt);
        MapMaterial.addMaterial("ground", coarseDirt, coarseDirt, coarseDirt);

        Block.REGISTRY.iterator().forEachRemaining(block -> {
            Material material = block.getDefaultState().getMaterial();
            if (material == Material.ROCK || material == Material.IRON) {
                ResourceLocation identifier = Block.REGISTRY.getNameForObject(block);
                if (identifier.getResourceDomain().equals("minecraft")) {
                    int colour = block.getDefaultState().getMapColor().colorValue;
                    Set<Block> blocks = BLOCK_COLOURS.computeIfAbsent(colour, (key) -> new HashSet<>());
                    blocks.add(block);
                }
            }
        });
    }

    private static void addMaterial(String key, IBlockState main, IBlockState stairs, IBlockState barrier) {
        MATERIALS.put(key, main);
        MATERIAL_BARRIER.put(key, barrier);
        MATERIAL_STAIRS.put(key, stairs);
    }

    private static void addMaterial(String key, Block main, Block stairs, Block barrier) {
        MapMaterial.addMaterial(key, main.getDefaultState(), stairs.getDefaultState(), barrier.getDefaultState());
    }

    public static IBlockState get(String material, IBlockState defaultState) {
        return MATERIALS.getOrDefault(material, defaultState);
    }

    public static IBlockState getStairs(String material, IBlockState defaultState) {
        return MATERIAL_STAIRS.getOrDefault(material, defaultState);
    }

    public static IBlockState getBarrier(String material, IBlockState defaultState) {
        return MATERIAL_BARRIER.getOrDefault(material, defaultState);
    }

    public static IBlockState getColour(int colour) {
        int red = (colour & 0xFF0000) >> 16;
        int green = (colour & 0xFF00) >> 8;
        int blue = colour & 0xFF;
        int lowestDelta = Integer.MAX_VALUE;
        Set<Block> bestBlocks = null;
        for (Map.Entry<Integer, Set<Block>> entry : BLOCK_COLOURS.entrySet()) {
            int entryColour = entry.getKey();
            int entryRed = (entryColour & 0xFF0000) >> 16;
            int entryGreen = (entryColour & 0xFF00) >> 8;
            int entryBlue = entryColour & 0xFF;
            int delta = Math.abs(red - entryRed) + Math.abs(green - entryGreen) + Math.abs(blue - entryBlue);
            if (delta < lowestDelta) {
                lowestDelta = delta;
                bestBlocks = entry.getValue();
            }
        }
        if (bestBlocks != null) {
            for (Block block : bestBlocks) {
                IBlockState state = block.getDefaultState();
                if (state.isFullBlock()) {
                    return state;
                }
            }
        }
        return null;
    }
}
