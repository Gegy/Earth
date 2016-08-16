package net.gegy1000.earth.server.world.gen;

import net.gegy1000.earth.Earth;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraft.world.chunk.IChunkGenerator;

public class WorldTypeEarth extends WorldType {
    private final EarthGenerator generator;

    public WorldTypeEarth(String name, EarthGenerator generator) {
        super(name);
        this.generator = generator;
    }

    public static EarthGenerator getGenerator(World world) {
        return world != null && world.getWorldType() instanceof WorldTypeEarth ? ((WorldTypeEarth) world.getWorldType()).getGenerator() : Earth.GENERATOR;
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

    public EarthGenerator getGenerator() {
        return this.generator;
    }

    public int getMapZoomX() {
        return 300;
    }

    public int getMapZoomY() {
        return 257;
    }

    public int getMapZoom() {
        return 16;
    }

    public int getMapDownloadScale() {
        return 8;
    }
}
