package net.gegy1000.earth.server.biome;

import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.common.Loader;

public enum EarthBiome {
    DEEP_OCEAN("deep_ocean", "deep_ocean"),
    OCEAN("ocean", "ocean"),
    BEACH("beaches", "beaches"),
    GRAVEL_BEACH("stone_beach", "BiomesOPlenty:gravel_beach"),
    PLAINS("plains", "plains"),
    FOREST("forest", "BiomesOPlenty:seasonal_forest"),
    CONIFEROUS_FOREST("redwood_taiga", "BiomesOPlenty:coniferous_forest"),
    TROPICAL("jungle_edge", "BiomesOPlenty:tropical_island"),
    REDWOOD_FOREST("redwood_taiga", "BiomesOPlenty:redwood_forest"),
    SHRUBLAND("desert", "BiomesOPlenty:xeric_shrubland"),
    BAMBOO_FOREST("jungle_edge", "BiomesOPlenty:bamboo_forest"),
    MANGROVE("swampland", "BiomesOPlenty:mangrove"),
    BAYOU("swampland", "BiomesOPlenty:bayou"),
    EUCALYPTUS_FOREST("jungle_edge", "BiomesOPlenty:eucalyptus_forest"),
    ARCTIC("ice_flats", "ice_flats"),
    BOG("swampland", "BiomesOPlenty:bog"),
    BADLANDS("mesa_rock", "BiomesOPlenty:badlands"),
    MESA("mesa_rock", "mesa_rock"),
    OUTBACK("mesa", "BiomesOPlenty:outback"),
    SAVANNA("savanna", "savanna"),
    BOREAL_FOREST("forest", "BiomesOPlenty:boreal_forest"),
    CHAPARRAL("plains", "BiomesOPlenty:chaparral"),
    MOOR("plains", "BiomesOPlenty:moor"),
    ORCHARD("forest", "BiomesOPlenty:orchard"),
    FUNGI_FOREST("redwood_taiga", "BiomesOPlenty:fungi_forest"),
    DESERT("desert", "desert"),
    JUNGLE("jungle", "jungle"),
    TROPICAL_RAINFOREST("redwood_taiga", "BiomesOPlenty:tropical_rainforest"),
    DECIDUOUS_RAINFOREST("forest", "BiomesOPlenty:deciduous_forest"),
    SHIELD("taiga", "BiomesOPlenty:shield"),
    OASIS("jungle_edge", "BiomesOPlenty:oasis"),
    BRUSHLAND("savanna", "BiomesOPlenty:brushland"),
    MEADOW("plains", "BiomesOPlenty:meadow"),
    RAINFOREST("forest", "BiomesOPlenty:rainforest"),
    TEMPERATE_RAINFOREST("redwood_taiga", "BiomesOPlenty:temperate_rainforest"),
    SNOWY_CONIFEROUS_FOREST("taiga_cold", "BiomesOPlenty:snowy_coniferous_forest"),
    TUNDRA("plains", "BiomesOPlenty:tundra"),
    TAIGA("taiga", "taiga"),
    MOUNTAIN("extreme_hills", "BiomesOPlenty:alps");

    private ResourceLocation vanilla;
    private ResourceLocation bop;
    private Biome biome;

    private static final boolean HAS_BIOMES_O_PLENTY = Loader.isModLoaded("BiomesOPlenty");

    EarthBiome(String vanilla, String bop) {
        this.vanilla = new ResourceLocation(vanilla);
        this.bop = new ResourceLocation(bop);
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
}
