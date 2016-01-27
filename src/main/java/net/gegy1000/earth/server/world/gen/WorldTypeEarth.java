package net.gegy1000.earth.server.world.gen;

import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.WorldChunkManager;

public class WorldTypeEarth extends WorldType
{
    private EarthGen generator;

    public WorldTypeEarth(EarthGen generator)
    {
        super("earth");
        this.generator = generator;
    }

    public net.minecraft.world.chunk.IChunkProvider getChunkGenerator(World world, String generatorOptions)
    {
        return new ChunkProviderEarth(world, world.getSeed(), generator);
    }

    public WorldChunkManager getChunkManager(World world)
    {
        return new WorldChunkManagerEarth(world, generator);
    }

    @Override
    public float getCloudHeight()
    {
        return 170;
    }
}
