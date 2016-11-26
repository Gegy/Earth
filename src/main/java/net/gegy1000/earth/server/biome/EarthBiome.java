package net.gegy1000.earth.server.biome;

import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.common.Loader;

import java.util.HashMap;
import java.util.Map;

public enum EarthBiome {
    DEEP_OCEAN("deep_ocean", "deep_ocean", 0x0000AA),
    OCEAN("ocean", "ocean", 0x0000FF),
    BEACH("beaches", "beaches", 0xFFFF00),
    GRAVEL_BEACH("stone_beach", "BiomesOPlenty:gravel_beach", 0xB69B84),
    PLAINS("plains", "plains", 0x80FF00),
    FOREST("forest", "BiomesOPlenty:seasonal_forest", 0x008000),
    CONIFEROUS_FOREST("redwood_taiga", "BiomesOPlenty:coniferous_forest", 0x004A18),
    TROPICAL("jungle_edge", "BiomesOPlenty:tropical_island", 0x00FF40),
    REDWOOD_FOREST("redwood_taiga", "BiomesOPlenty:redwood_forest", 0xAA4A00),
    SHRUBLAND("desert", "BiomesOPlenty:xeric_shrubland", 0xAF7957),
    BAMBOO_FOREST("jungle_edge", "BiomesOPlenty:bamboo_forest", 0x65C646),
    MANGROVE("swampland", "BiomesOPlenty:mangrove", 0x0094FF),
    BAYOU("swampland", "BiomesOPlenty:bayou", 0x376043),
    EUCALYPTUS_FOREST("jungle_edge", "BiomesOPlenty:eucalyptus_forest", 0x378443),
    ARCTIC("ice_flats", "ice_flats", 0xFFFFFF),
    BOG("swampland", "BiomesOPlenty:bog", 0x895A2E),
    BADLANDS("mesa_rock", "BiomesOPlenty:badlands", 0xC94E1C),
    MESA("mesa_rock", "mesa_rock", 0xC99B1C),
    OUTBACK("mesa", "BiomesOPlenty:outback", 0x886812),
    SAVANNA("savanna", "savanna", 0xB28F18),
    BOREAL_FOREST("forest", "BiomesOPlenty:boreal_forest", 0x376118),
    CHAPARRAL("plains", "BiomesOPlenty:chaparral", 0x748C18),
    MOOR("plains", "BiomesOPlenty:moor", 0x006D3A),
    ORCHARD("forest", "BiomesOPlenty:orchard", 0x6D933A),
    FUNGI_FOREST("redwood_taiga", "BiomesOPlenty:fungi_forest", 0x6D3E3A),
    DESERT("desert", "desert", 0xFFD53E),
    JUNGLE("jungle", "jungle", 0x2F840E),
    TROPICAL_RAINFOREST("redwood_taiga", "BiomesOPlenty:tropical_rainforest", 0x9FB20E),
    DECIDUOUS_RAINFOREST("forest", "BiomesOPlenty:deciduous_forest", 0x6D800E),
    SHIELD("taiga", "BiomesOPlenty:shield", 0x557F59),
    OASIS("jungle_edge", "BiomesOPlenty:oasis", 0x52A72F),
    BRUSHLAND("savanna", "BiomesOPlenty:brushland", 0x997E64),
    MEADOW("plains", "BiomesOPlenty:meadow", 0x009755),
    RAINFOREST("forest", "BiomesOPlenty:rainforest", 0x1F6D00),
    TEMPERATE_RAINFOREST("redwood_taiga", "BiomesOPlenty:temperate_rainforest", 0x598F00),
    SNOWY_CONIFEROUS_FOREST("taiga_cold", "BiomesOPlenty:snowy_coniferous_forest", 0x356B35),
    TUNDRA("plains", "BiomesOPlenty:tundra", 0x7C5449),
    TAIGA("taiga", "taiga", 0x2E5449),
    MOUNTAIN("extreme_hills", "BiomesOPlenty:alps", 0x808080);

    private static final Map<Integer, EarthBiome> BIOMES = new HashMap<>();

    private ResourceLocation vanilla;
    private ResourceLocation bop;
    private Biome biome;
    private int colour;

    private static final boolean HAS_BIOMES_O_PLENTY = Loader.isModLoaded("BiomesOPlenty");

    EarthBiome(String vanilla, String bop, int colour) {
        this.vanilla = new ResourceLocation(vanilla);
        this.bop = new ResourceLocation(bop);
        this.colour = colour;
    }

    public Biome get() {
        if (this.biome == null) {
            ResourceLocation resource = HAS_BIOMES_O_PLENTY ? this.bop : this.vanilla;
            this.biome = Biome.REGISTRY.getObject(resource);
            if (this.biome == null) {
                System.err.println("Could not find biome " + resource);
            }
        }
        return this.biome;
    }

    static {
        for (EarthBiome biome : values()) {
            BIOMES.put(biome.colour, biome);
        }
    }

    public static EarthBiome get(int colour) {
        return BIOMES.get(colour);
    }
}
