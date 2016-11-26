package net.gegy1000.earth.server.world.gen;

import com.google.common.collect.Lists;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeCache;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraft.world.gen.layer.GenLayer;
import net.minecraft.world.gen.layer.IntCache;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.terraingen.WorldTypeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;
import java.util.Random;

public class BiomeProviderEarth extends BiomeProvider {
    private final BiomeCache biomeCache;
    private final List<Biome> SPAWN_BIOMES;

    private final EarthGenerator GENERATOR;

    public BiomeProviderEarth(EarthGenerator generator) {
        this.biomeCache = new BiomeCache(this);
        this.SPAWN_BIOMES = Lists.newArrayList();
        this.SPAWN_BIOMES.addAll(allowedBiomes);
        this.GENERATOR = generator;
    }

    @Override
    public List<Biome> getBiomesToSpawnIn() {
        return this.SPAWN_BIOMES;
    }

    @Override
    public Biome getBiome(BlockPos pos) {
        return this.getBiome(pos, null);
    }

    @Override
    public Biome getBiome(BlockPos pos, Biome defaultBiome) {
        return this.biomeCache.getBiome(pos.getX(), pos.getZ(), defaultBiome);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public float getTemperatureAtHeight(float biomeTemperature, int height) {
        return biomeTemperature;
    }

    @Override
    public Biome[] getBiomesForGeneration(Biome[] biomes, int x, int z, int width, int height) {
        IntCache.resetIntCache();
        if (biomes == null || biomes.length < width * height) {
            biomes = new Biome[width * height];
        }
        int i = 0;
        for (int partZ = 0; partZ < height; partZ++) {
            for (int partX = 0; partX < width; partX++) {
                biomes[i] = this.getBiomeAt(partX + x, partZ + z);
                i++;
            }
        }
        return biomes;
    }

    @Override
    public Biome[] getBiomes(Biome[] oldBiomes, int x, int z, int width, int depth) {
        return this.getBiomes(oldBiomes, x, z, width, depth, true);
    }

    @Override
    public Biome[] getBiomes(Biome[] biomes, int x, int z, int width, int length, boolean cache) {
        IntCache.resetIntCache();
        if (biomes == null || biomes.length < width * length) {
            biomes = new Biome[width * length];
        }
        if (cache && width == 16 && length == 16 && (x & 15) == 0 && (z & 15) == 0) {
            Biome[] cachedBiomes = this.biomeCache.getCachedBiomes(x, z);
            System.arraycopy(cachedBiomes, 0, biomes, 0, width * length);
            return biomes;
        } else {
            int i = 0;
            for (int partZ = 0; partZ < length; ++partZ) {
                for (int partX = 0; partX < width; ++partX) {
                    biomes[i] = this.getBiomeAt(partX + x, partZ + z);
                    i++;
                }
            }
            return biomes;
        }
    }

    @Override
    public boolean areBiomesViable(int x, int z, int radius, List<Biome> allowed) {
        IntCache.resetIntCache();
        int minX = x - radius >> 2;
        int minZ = z - radius >> 2;
        int maxX = x + radius >> 2;
        int maxZ = z + radius >> 2;
        int width = maxX - minX + 1;
        int length = maxZ - minZ + 1;
        for (int partZ = 0; partZ < length; ++partZ) {
            for (int partX = 0; partX < width; ++partX) {
                Biome biome = this.getBiomeAt(partX + x, partZ + z);
                if (!allowed.contains(biome)) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public BlockPos findBiomePosition(int x, int z, int radius, List<Biome> biomes, Random random) {
        IntCache.resetIntCache();
        int minX = x - radius >> 2;
        int minZ = z - radius >> 2;
        int maxX = x + radius >> 2;
        int maxZ = z + radius >> 2;
        int width = maxX - minX + 1;
        int length = maxZ - minZ + 1;
        BlockPos pos = null;
        int j2 = 0;
        int i = 0;
        for (int partZ = 0; partZ < length; ++partZ) {
            for (int partX = 0; partX < width; ++partX) {
                int chunkX = minX + i % width << 2;
                int chunkZ = minZ + i / width << 2;
                Biome biome = this.getBiomeAt(partX + x, partZ + z);
                if (biomes.contains(biome) && (pos == null || random.nextInt(j2 + 1) == 0)) {
                    pos = new BlockPos(chunkX, 0, chunkZ);
                    ++j2;
                }
                i++;
            }
        }
        return pos;
    }

    private Biome getBiomeAt(int x, int z) {
        return this.GENERATOR.getGenerationBiome(x, z);
    }

    @Override
    public void cleanupCache() {
        this.biomeCache.cleanupCache();
    }

    @Override
    public GenLayer[] getModdedBiomeGenerators(WorldType type, long seed, GenLayer[] original) {
        WorldTypeEvent.InitBiomeGens event = new WorldTypeEvent.InitBiomeGens(type, seed, original);
        MinecraftForge.TERRAIN_GEN_BUS.post(event);
        return event.getNewBiomeGens();
    }
}