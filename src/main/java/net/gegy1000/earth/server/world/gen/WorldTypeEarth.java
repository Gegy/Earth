package net.gegy1000.earth.server.world.gen;

import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraft.world.chunk.IChunkGenerator;

public class WorldTypeEarth extends WorldType {
    private final EarthGen generator;

    public WorldTypeEarth(EarthGen generator) {
        super("earth");
        this.generator = generator;
    }

    @Override
    public IChunkGenerator getChunkGenerator(World world, String generatorOptions) {
        return new ChunkGeneratorEarth(world, world.getSeed(), generator);
    }

    @Override
    public BiomeProvider getBiomeProvider(World world) {
        return new BiomeProviderEarth(generator);
    }

    @Override
    public float getCloudHeight() {
        return 170;
    }
}
