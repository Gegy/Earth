package net.gegy1000.earth.server.world.gen;

import net.minecraft.block.BlockFalling;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldEntitySpawner;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.chunk.IChunkGenerator;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.terraingen.ChunkGeneratorEvent;
import net.minecraftforge.event.terraingen.PopulateChunkEvent;
import net.minecraftforge.event.terraingen.TerrainGen;
import net.minecraftforge.fml.common.eventhandler.Event.Result;

import java.io.IOException;
import java.util.List;
import java.util.Random;

import static net.minecraftforge.event.terraingen.PopulateChunkEvent.Populate.EventType.ANIMALS;
import static net.minecraftforge.event.terraingen.PopulateChunkEvent.Populate.EventType.ICE;

public class ChunkGeneratorEarth implements IChunkGenerator {
    private final Random RANDOM;
    private final World WORLD;

    private Biome[] biomesForGeneration;

    private final EarthGen EARTH_GEN;

    private static final IBlockState STONE_BLOCK = Blocks.STONE.getDefaultState();
    private static final IBlockState LIQUID_BLOCK = Blocks.WATER.getDefaultState();
    private static final int OCEAN_HEIGHT = 59;

    public ChunkGeneratorEarth(World world, long seed, EarthGen earthGen) {
        this.WORLD = world;
        this.RANDOM = new Random(seed);
        this.EARTH_GEN = earthGen;
    }

    public void setBlocksInChunk(int chunkX, int chunkZ, ChunkPrimer chunkPrimer) {
        this.biomesForGeneration = this.WORLD.getBiomeProvider().getBiomesForGeneration(this.biomesForGeneration, chunkX * 4 - 2, chunkZ * 4 - 2, 10, 10);
        int chunkWorldX = chunkX * 16;
        int chunkWorldZ = chunkZ * 16;
        IBlockState stoneBlock = STONE_BLOCK;
        IBlockState liquidBlock = LIQUID_BLOCK;
        IBlockState bedrock = Blocks.BEDROCK.getDefaultState();
        int oceanHeight = OCEAN_HEIGHT;
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int height = (EARTH_GEN.getHeightForCoords(x + chunkWorldX, z + chunkWorldZ));
                chunkPrimer.setBlockState(x, 0, z, bedrock);
                for (int y = 1; y < height; y++) {
                    chunkPrimer.setBlockState(x, y, z, stoneBlock);
                }
                if (height < oceanHeight) {
                    for (int y = height + 1; y <= oceanHeight; y++) {
                        chunkPrimer.setBlockState(x, y, z, liquidBlock);
                    }
                }
            }
        }
    }

    public void generateGrass(int chunkX, int chunkZ, ChunkPrimer chunkPrimer, Biome[] biomes) {
        ChunkGeneratorEvent.ReplaceBiomeBlocks event = new ChunkGeneratorEvent.ReplaceBiomeBlocks(this, chunkX, chunkZ, chunkPrimer, this.WORLD);
        MinecraftForge.EVENT_BUS.post(event);
        if (event.getResult() == Result.DENY) {
            return;
        }
        int worldChunkX = chunkX * 16;
        int worldChunkZ = chunkZ * 16;
        IBlockState bedrock = Blocks.BEDROCK.getDefaultState();
        for (int x = 0; x < 16; ++x) {
            for (int z = 0; z < 16; ++z) {
                Biome biome = biomes[x + z * 16];
                if (biome != null) {
                    IBlockState topBlock = biome.topBlock;
                    IBlockState fillerBlock = biome.fillerBlock;
                    int surfaceDepth = 0;
                    int height = this.EARTH_GEN.getHeightForCoords(x + worldChunkX, z + worldChunkZ);
                    for (int y = height; y >= 0; y--) {
                        if (y > 0 && y < 5 && y <= RANDOM.nextInt(5)) {
                            chunkPrimer.setBlockState(x, y, z, bedrock);
                        } else {
                            if (surfaceDepth == 0) {
                                chunkPrimer.setBlockState(x, y, z, topBlock);
                            } else if (surfaceDepth < 4) {
                                chunkPrimer.setBlockState(x, y, z, fillerBlock);
                            } else if (surfaceDepth > 4 && y > 5) {
                                y = 5;
                            }
                            surfaceDepth++;
                        }
                    }
                }
            }
        }
    }

    @Override
    public Chunk provideChunk(int x, int z) {
        this.RANDOM.setSeed((long) x * 341873128712L + (long) z * 132897987541L);
        ChunkPrimer primer = new ChunkPrimer();
        this.setBlocksInChunk(x, z, primer);
        this.biomesForGeneration = this.WORLD.getBiomeProvider().loadBlockGeneratorData(this.biomesForGeneration, x * 16, z * 16, 16, 16);
        this.generateGrass(x, z, primer, this.biomesForGeneration);

        Chunk chunk = new Chunk(this.WORLD, primer, x, z);

        byte[] biomeArray = chunk.getBiomeArray();

        for (int biomeIndex = 0; biomeIndex < biomeArray.length; ++biomeIndex) {
            biomeArray[biomeIndex] = (byte) Biome.getIdForBiome(this.biomesForGeneration[biomeIndex]);
        }

        chunk.generateSkylightMap();

        EARTH_GEN.clearCache();

        return chunk;
    }

    @Override
    public boolean generateStructures(Chunk chunk, int x, int z) {
        return false;
    }

    @Override
    public void populate(int chunkX, int chunkZ) {
        BlockFalling.fallInstantly = true;
        int x = chunkX * 16;
        int z = chunkZ * 16;
        BlockPos pos = new BlockPos(x, 0, z);
        Biome biome = this.WORLD.getBiomeGenForCoords(pos.add(16, 0, 16));
        this.RANDOM.setSeed(this.WORLD.getSeed());
        long i1 = this.RANDOM.nextLong() / 2L * 2L + 1L;
        long j1 = this.RANDOM.nextLong() / 2L * 2L + 1L;
        this.RANDOM.setSeed((long) chunkX * i1 + (long) chunkZ * j1 ^ this.WORLD.getSeed());
        boolean hasVillageGenerated = false;

        MinecraftForge.EVENT_BUS.post(new PopulateChunkEvent.Pre(this, WORLD, RANDOM, chunkX, chunkZ, hasVillageGenerated));

        biome.decorate(this.WORLD, this.RANDOM, new BlockPos(x, 0, z));
        if (TerrainGen.populate(this, WORLD, RANDOM, chunkX, chunkZ, hasVillageGenerated, ANIMALS)) {
            WorldEntitySpawner.performWorldGenSpawning(this.WORLD, biome, x + 8, z + 8, 16, 16, this.RANDOM);
        }
        pos = pos.add(8, 0, 8);

        boolean freeze = TerrainGen.populate(this, WORLD, RANDOM, chunkX, chunkZ, hasVillageGenerated, ICE);
        for (int xOffset = 0; freeze && xOffset < 16; ++xOffset) {
            for (int zOffset = 0; zOffset < 16; ++zOffset) {
                BlockPos top = this.WORLD.getPrecipitationHeight(pos.add(xOffset, 0, zOffset));
                BlockPos ground = top.down();

                if (this.WORLD.canBlockFreezeWater(ground)) {
                    this.WORLD.setBlockState(ground, Blocks.ICE.getDefaultState(), 2);
                }

                if (this.WORLD.canSnowAt(top, true)) {
                    this.WORLD.setBlockState(top, Blocks.SNOW_LAYER.getDefaultState(), 2);
                }
            }
        }
        MinecraftForge.EVENT_BUS.post(new PopulateChunkEvent.Post(this, WORLD, RANDOM, chunkX, chunkZ, hasVillageGenerated));

        BlockFalling.fallInstantly = false;
    }

    @Override
    public List<Biome.SpawnListEntry> getPossibleCreatures(EnumCreatureType creatureType, BlockPos pos) {
        return WORLD.getBiomeGenForCoords(pos).getSpawnableList(creatureType);
    }

    @Override
    public BlockPos getStrongholdGen(World world, String gen, BlockPos pos) {
        return pos;
    }

    @Override
    public void recreateStructures(Chunk chunk, int x, int z) {
    }
}