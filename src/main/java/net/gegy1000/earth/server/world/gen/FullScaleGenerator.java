package net.gegy1000.earth.server.world.gen;

import net.minecraft.init.Biomes;
import net.minecraft.world.biome.Biome;

public class FullScaleGenerator extends EarthGenerator {
    protected static final double WORLD_SCALE = 926.62;
    protected static final Biome DEFAULT_BIOME = Biomes.PLAINS;

    @Override
    public void load() {
    }

    @Override
    public void loadHeightmap() {
    }

    @Override
    public void loadBiomemap() {
    }

    @Override
    public int getGenerationHeight(int x, int z) {
        return 64;
    }

    @Override
    public Biome getGenerationBiome(int x, int z) {
        return DEFAULT_BIOME;
    }

    @Override
    protected double getWorldScale() {
        return WORLD_SCALE;
    }

    @Override
    protected int getWidth() {
        return 43200;
    }

    @Override
    protected int getHeight() {
        return 21600;
    }
}