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
import net.minecraft.world.gen.NoiseGeneratorPerlin;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.terraingen.PopulateChunkEvent;
import net.minecraftforge.event.terraingen.TerrainGen;

import java.util.List;
import java.util.Random;

import static net.minecraftforge.event.terraingen.PopulateChunkEvent.Populate.EventType.ANIMALS;
import static net.minecraftforge.event.terraingen.PopulateChunkEvent.Populate.EventType.ICE;

public class ChunkGeneratorEarth implements IChunkGenerator {
    private final Random random;
    private final World world;

    private Biome[] biomesForGeneration;

    private final EarthGenerator earthGenerator;

    private static final IBlockState STONE_BLOCK = Blocks.STONE.getDefaultState();
    private static final IBlockState LIQUID_BLOCK = Blocks.WATER.getDefaultState();
    private static final int OCEAN_HEIGHT = 21;

    private NoiseGeneratorPerlin surfaceNoise;
    private double[] depthBuffer = new double[256];

    public ChunkGeneratorEarth(World world, long seed, EarthGenerator earthGenerator) {
        this.world = world;
        this.world.setSeaLevel(OCEAN_HEIGHT);
        this.random = new Random(seed);
        this.earthGenerator = earthGenerator;
        this.surfaceNoise = new NoiseGeneratorPerlin(this.random, 4);
    }

    public void setBlocksInChunk(int chunkX, int chunkZ, ChunkPrimer chunkPrimer) {
        this.biomesForGeneration = this.world.getBiomeProvider().getBiomesForGeneration(this.biomesForGeneration, chunkX * 4 - 2, chunkZ * 4 - 2, 10, 10);
        int chunkWorldX = chunkX * 16;
        int chunkWorldZ = chunkZ * 16;
        IBlockState bedrock = Blocks.BEDROCK.getDefaultState();
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int height = (this.earthGenerator.getHeightForCoords(x + chunkWorldX, z + chunkWorldZ));
                chunkPrimer.setBlockState(x, 0, z, bedrock);
                for (int y = 1; y <= height; y++) {
                    chunkPrimer.setBlockState(x, y, z, STONE_BLOCK);
                }
                if (height < OCEAN_HEIGHT) {
                    for (int y = height + 1; y <= OCEAN_HEIGHT; y++) {
                        chunkPrimer.setBlockState(x, y, z, LIQUID_BLOCK);
                    }
                }
            }
        }
    }

    /*public void generateGrass(int chunkX, int chunkZ, ChunkPrimer chunkPrimer, Biome[] biomes) {
        ChunkGeneratorEvent.ReplaceBiomeBlocks event = new ChunkGeneratorEvent.ReplaceBiomeBlocks(this, chunkX, chunkZ, chunkPrimer, this.WORLD);
        MinecraftForge.EVENT_BUS.post(event);
        if (event.getResult() == Result.DENY) {
            return;
        }
        int worldChunkX = chunkX * 16;
        int worldChunkZ = chunkZ * 16;
        IBlockState sand = Blocks.SAND.getDefaultState();
        IBlockState bedrock = Blocks.BEDROCK.getDefaultState();
        for (int x = 0; x < 16; ++x) {
            for (int z = 0; z < 16; ++z) {
                Biome biome = biomes[x + z * 16];
                if (biome != null) {
                    IBlockState topBlock = biome.topBlock;
                    IBlockState fillerBlock = biome.fillerBlock;
                    int surfaceDepth = 0;
                    int height = this.EARTH_GEN.getHeightForCoords(x + worldChunkX, z + worldChunkZ);
                    if (height < 59) {
                        topBlock = sand;
                        fillerBlock = topBlock;
                    }
                    for (int y = height; y >= 0; y--) {
                        if (y > 0 && y < 5 && y <= this.RANDOM.nextInt(5)) {
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
    }*/

    public void generateBiomeBlocks(int chunkX, int chunkZ, ChunkPrimer primer, Biome[] biomes) {
        MockChunkPrimer mockPrimer = new MockChunkPrimer(primer, 62 - OCEAN_HEIGHT);
        double scale = 0.03125D;
        this.depthBuffer = this.surfaceNoise.getRegion(this.depthBuffer, chunkX * 16, chunkZ * 16, 16, 16, scale * 2.0D, scale * 2.0D, 1.0D);
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                Biome biome = biomes[z + x * 16];
                biome.genTerrainBlocks(this.world, this.random, mockPrimer, chunkX * 16 + x, chunkZ * 16 + z, this.depthBuffer[z + x * 16]);
            }
        }
    }

    @Override
    public Chunk provideChunk(int x, int z) {
        this.random.setSeed((long) x * 341873128712L + (long) z * 132897987541L);
        ChunkPrimer primer = new ChunkPrimer();
        this.setBlocksInChunk(x, z, primer);
        this.biomesForGeneration = this.world.getBiomeProvider().loadBlockGeneratorData(this.biomesForGeneration, x * 16, z * 16, 16, 16);
        this.generateBiomeBlocks(x, z, primer, this.biomesForGeneration);
        Chunk chunk = new Chunk(this.world, primer, x, z);

        byte[] biomeArray = chunk.getBiomeArray();

        for (int biomeIndex = 0; biomeIndex < biomeArray.length; ++biomeIndex) {
            biomeArray[biomeIndex] = (byte) Biome.getIdForBiome(this.biomesForGeneration[biomeIndex]);
        }

        chunk.generateSkylightMap();

        this.earthGenerator.clearCache();

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
        Biome biome = this.world.getBiomeGenForCoords(pos.add(16, 0, 16));
        this.random.setSeed(this.world.getSeed());
        long i1 = this.random.nextLong() / 2L * 2L + 1L;
        long j1 = this.random.nextLong() / 2L * 2L + 1L;
        this.random.setSeed((long) chunkX * i1 + (long) chunkZ * j1 ^ this.world.getSeed());
        boolean hasVillageGenerated = false;

        MinecraftForge.EVENT_BUS.post(new PopulateChunkEvent.Pre(this, this.world, this.random, chunkX, chunkZ, hasVillageGenerated));

        biome.decorate(this.world, this.random, new BlockPos(x, 0, z));
        if (TerrainGen.populate(this, this.world, this.random, chunkX, chunkZ, hasVillageGenerated, ANIMALS)) {
            WorldEntitySpawner.performWorldGenSpawning(this.world, biome, x + 8, z + 8, 16, 16, this.random);
        }
        pos = pos.add(8, 0, 8);

        boolean freeze = TerrainGen.populate(this, this.world, this.random, chunkX, chunkZ, hasVillageGenerated, ICE);
        for (int xOffset = 0; freeze && xOffset < 16; ++xOffset) {
            for (int zOffset = 0; zOffset < 16; ++zOffset) {
                BlockPos top = this.world.getPrecipitationHeight(pos.add(xOffset, 0, zOffset));
                BlockPos ground = top.down();

                if (this.world.canBlockFreezeWater(ground)) {
                    this.world.setBlockState(ground, Blocks.ICE.getDefaultState(), 2);
                }

                if (this.world.canSnowAt(top, true)) {
                    this.world.setBlockState(top, Blocks.SNOW_LAYER.getDefaultState(), 2);
                }
            }
        }
        MinecraftForge.EVENT_BUS.post(new PopulateChunkEvent.Post(this, this.world, this.random, chunkX, chunkZ, hasVillageGenerated));

        BlockFalling.fallInstantly = false;
    }

    @Override
    public List<Biome.SpawnListEntry> getPossibleCreatures(EnumCreatureType creatureType, BlockPos pos) {
        return this.world.getBiomeGenForCoords(pos).getSpawnableList(creatureType);
    }

    @Override
    public BlockPos getStrongholdGen(World world, String gen, BlockPos pos) {
        return pos;
    }

    @Override
    public void recreateStructures(Chunk chunk, int x, int z) {
    }
}