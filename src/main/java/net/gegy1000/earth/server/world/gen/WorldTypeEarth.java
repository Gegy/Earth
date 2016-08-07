package net.gegy1000.earth.server.world.gen;

import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraft.world.chunk.IChunkGenerator;

public class WorldTypeEarth extends WorldType {
    private final EarthGenerator generator;

    public WorldTypeEarth(EarthGenerator generator) {
        super("earth");
        this.generator = generator;
    }

    @Override
    public double getHorizon(World world) {
        return 20.0;
    }

    @Override
    public IChunkGenerator getChunkGenerator(World world, String generatorOptions) {
        return new ChunkGeneratorEarth(world, world.getSeed(), this.generator);
    }

    @Override
    public BiomeProvider getBiomeProvider(World world) {
        return new BiomeProviderEarth(this.generator);
    }

    @Override
    public float getCloudHeight() {
        return 170;
    }
}
