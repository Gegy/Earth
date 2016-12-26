package net.gegy1000.earth.server.world.gen;

import net.gegy1000.earth.server.util.osm.MapHandler;
import net.gegy1000.earth.server.util.osm.MapTile;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldEntitySpawner;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.chunk.IChunkGenerator;
import net.minecraft.world.gen.NoiseGeneratorPerlin;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.terraingen.PopulateChunkEvent;
import net.minecraftforge.event.terraingen.TerrainGen;

import java.util.List;
import java.util.Random;

public class ChunkGeneratorEarth implements IChunkGenerator {
    protected final Random random;
    protected final World world;
    protected final boolean decorate;
    protected final boolean structures;

    protected Biome[] biomesForGeneration;

    protected final EarthGenerator earthGenerator;

    protected static final IBlockState STONE_BLOCK = Blocks.STONE.getDefaultState();
    protected static final IBlockState LIQUID_BLOCK = Blocks.WATER.getDefaultState();
    protected static final int OCEAN_HEIGHT = 21;

    protected NoiseGeneratorPerlin surfaceNoise;
    protected double[] depthBuffer = new double[256];

    public ChunkGeneratorEarth(World world, long seed, EarthGenerator earthGenerator, boolean decorate, boolean structures) {
        this.world = world;
        this.world.setSeaLevel(OCEAN_HEIGHT);
        this.random = new Random(seed);
        this.earthGenerator = earthGenerator;
        this.surfaceNoise = new NoiseGeneratorPerlin(this.random, 4);
        this.decorate = decorate;
        this.structures = structures;
    }

    public void setBlocksInChunk(int chunkX, int chunkZ, ChunkPrimer chunkPrimer) {
        this.biomesForGeneration = this.world.getBiomeProvider().getBiomesForGeneration(this.biomesForGeneration, chunkX * 4 - 2, chunkZ * 4 - 2, 10, 10);
        int chunkWorldX = chunkX << 4;
        int chunkWorldZ = chunkZ << 4;
        IBlockState bedrock = Blocks.BEDROCK.getDefaultState();
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int height = (this.earthGenerator.getGenerationHeight(x + chunkWorldX, z + chunkWorldZ));
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
        if (this.structures) {
            ChunkPos pos = new ChunkPos(chunkX, chunkZ);
            this.generateTile(pos, chunkPrimer, chunkWorldX, chunkWorldZ);
            this.generateTile(pos, chunkPrimer, chunkWorldX + 16, chunkWorldZ);
            this.generateTile(pos, chunkPrimer, chunkWorldX, chunkWorldZ + 16);
            this.generateTile(pos, chunkPrimer, chunkWorldX + 16, chunkWorldZ + 16);
        }
    }

    private void generateTile(ChunkPos pos, ChunkPrimer primer, int x, int z) {
        MapTile tile = MapHandler.getTile(this.world, x, z);
        tile.generate(pos, primer);
    }

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
        this.biomesForGeneration = this.world.getBiomeProvider().getBiomes(this.biomesForGeneration, x * 16, z * 16, 16, 16);
        this.generateBiomeBlocks(x, z, primer, this.biomesForGeneration);
        Chunk chunk = new Chunk(this.world, primer, x, z);

        byte[] biomeArray = chunk.getBiomeArray();

        for (int biomeIndex = 0; biomeIndex < biomeArray.length; ++biomeIndex) {
            biomeArray[biomeIndex] = (byte) Biome.getIdForBiome(this.biomesForGeneration[biomeIndex]);
        }

        chunk.generateSkylightMap();

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
        Biome biome = this.world.getBiome(pos.add(16, 0, 16));
        this.random.setSeed(this.world.getSeed());
        long k = this.random.nextLong() / 2L * 2L + 1L;
        long l = this.random.nextLong() / 2L * 2L + 1L;
        this.random.setSeed((long) chunkX * k + (long) chunkZ * l ^ this.world.getSeed());
        boolean generatedVillage = false;

        if (this.decorate) {
            ForgeEventFactory.onChunkPopulate(true, this, this.world, this.random, chunkX, chunkZ, generatedVillage);

            biome.decorate(this.world, this.random, new BlockPos(x, 0, z));
            if (TerrainGen.populate(this, this.world, this.random, chunkX, chunkZ, generatedVillage, PopulateChunkEvent.Populate.EventType.ANIMALS)) {
                WorldEntitySpawner.performWorldGenSpawning(this.world, biome, x + 8, z + 8, 16, 16, this.random);
            }
            pos = pos.add(8, 0, 8);

            if (TerrainGen.populate(this, this.world, this.random, chunkX, chunkZ, generatedVillage, PopulateChunkEvent.Populate.EventType.ICE)) {
                for (int offsetX = 0; offsetX < 16; ++offsetX) {
                    for (int offsetZ = 0; offsetZ < 16; ++offsetZ) {
                        BlockPos snowPos = this.world.getPrecipitationHeight(pos.add(offsetX, 0, offsetZ));
                        BlockPos groundPos = snowPos.down();

                        if (this.world.canBlockFreezeWater(groundPos)) {
                            this.world.setBlockState(groundPos, Blocks.ICE.getDefaultState(), 2);
                        }

                        if (this.world.canSnowAt(snowPos, true)) {
                            this.world.setBlockState(snowPos, Blocks.SNOW_LAYER.getDefaultState(), 2);
                        }
                    }
                }
            }

            ForgeEventFactory.onChunkPopulate(false, this, this.world, this.random, chunkX, chunkZ, generatedVillage);
        }

        BlockFalling.fallInstantly = false;
    }

    @Override
    public List<Biome.SpawnListEntry> getPossibleCreatures(EnumCreatureType creatureType, BlockPos pos) {
        return this.world.getBiome(pos).getSpawnableList(creatureType);
    }

    @Override
    public BlockPos getStrongholdGen(World world, String gen, BlockPos pos) {
        return pos;
    }

    @Override
    public void recreateStructures(Chunk chunk, int x, int z) {
    }
}