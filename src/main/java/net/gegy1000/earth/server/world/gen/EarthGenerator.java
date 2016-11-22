package net.gegy1000.earth.server.world.gen;

import com.google.common.collect.HashMultimap;
import net.gegy1000.earth.Earth;
import net.gegy1000.earth.server.biome.EarthBiome;
import net.minecraft.init.Biomes;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.common.ProgressManager;

import java.io.IOException;
import java.util.Map;
import java.util.Random;

public class EarthGenerator {
    protected DataMap heightmap;
    protected DataMap biomemap;

    protected static final String HEIGHTMAP_LOCATION = "/assets/earth/data/earth_heightmap.mchmap";
    protected static final String BIOMEMAP_LOCATION = "/assets/earth/data/earth_biomemap.mcbmap";

    protected static final double WORLD_SCALE = 15.0; //TODO 20

    protected static final int WORLD_OFFSET_X = 21600;
    protected static final int WORLD_OFFSET_Z = 10800;

    protected static final Biome DEFAULT_BIOME = Biomes.OCEAN;

    protected static final int HEIGHTMAP_VERSION = 1;
    protected static final int BIOMEMAP_VERSION = 4;

    protected static final float STANDARD_PARALLEL = 0.0F;
    protected static final float CENTRAL_MERIDIAN = 0.0F;

    public void load(ProgressManager.ProgressBar bar) throws IOException {
        bar.step("Heightmap");
        if (this.heightmap == null) {
            this.loadHeightmap();
        }
        bar.step("Biomemap");
        if (this.biomemap == null) {
            this.loadBiomemap();
        }
    }

    public void loadHeightmap() throws IOException {
        Earth.LOGGER.info("Loading Earth Heightmap...");
        this.heightmap = DataMap.construct(HEIGHTMAP_LOCATION, false, HEIGHTMAP_VERSION);
    }

    public void loadBiomemap() throws IOException {
        Earth.LOGGER.info("Loading Earth Biomemap...");
        this.biomemap = DataMap.construct(BIOMEMAP_LOCATION, true, BIOMEMAP_VERSION);
    }

    public int getHeightForCoords(int x, int z) {
        int width = this.heightmap.getWidth();
        int height = this.biomemap.getHeight();

        double worldScale = this.getWorldScale();

        int scaledWidth = (int) (width * worldScale);
        int scaledHeight = (int) (height * worldScale);

        x += (WORLD_OFFSET_X * worldScale);
        z += (WORLD_OFFSET_Z * worldScale);

        double[][] buffer = new double[4][4];

        double xScaled = (double) x / (scaledWidth - 1) * (width - 1);
        double yScaled = (double) z / (scaledHeight - 1) * (height - 1);
        int xOrigin = (int) xScaled;
        int yOrigin = (int) yScaled;
        double xIntermediate = xScaled - xOrigin;
        double yIntermediate = yScaled - yOrigin;

        for (int u = 0; u < 4; u++) {
            for (int v = 0; v < 4; v++) {
                buffer[u][v] = this.getHeight(xOrigin - 1 + u, yOrigin - 1 + v);
            }
        }

        int value = (int) Math.round(Bicubic.bicubic(buffer, xIntermediate, yIntermediate));
        value = Math.min(0xff, Math.max(value, 0));

        return value;
    }

    public Biome getBiomeForCoords(int x, int z) {
        int width = this.biomemap.getWidth();
        int height = this.biomemap.getHeight();

        double worldScale = this.getWorldScale();

        int scaledWidth = (int) (width * worldScale);
        int scaledHeight = (int) (height * worldScale);

        x += (WORLD_OFFSET_X * worldScale);
        z += (WORLD_OFFSET_Z * worldScale);

        if (x < 0 || z < 0 || x >= scaledWidth || z >= scaledHeight) {
            return DEFAULT_BIOME;
        }

        HashMultimap<Integer, Biome> heightToBiome = HashMultimap.create();

        double[][] buffer = new double[4][4];

        double xScaled = (double) x / (scaledWidth - 1) * (width - 1);
        double yScaled = (double) z / (scaledHeight - 1) * (height - 1);
        int xOrigin = (int) xScaled;
        int yOrigin = (int) yScaled;
        double xIntermediate = xScaled - xOrigin;
        double yIntermediate = yScaled - yOrigin;

        Biome prevBiome = null;
        boolean hasMultipleBiomes = false;

        for (int u = 0; u < 4; u++) {
            for (int v = 0; v < 4; v++) {
                int dataX = xOrigin - 1 + u;
                int dataY = yOrigin - 1 + v;
                int blockHeight = (int) this.getHeight(dataX, dataY);
                buffer[u][v] = blockHeight;
                Biome biome = this.getBiome(dataX, dataY);
                if (prevBiome != null && (!biome.equals(prevBiome))) {
                    hasMultipleBiomes = true;
                }
                heightToBiome.put(blockHeight, biome);
                prevBiome = biome;
            }
        }
        if (hasMultipleBiomes) {
            double interpolated = Bicubic.bicubic(buffer, xIntermediate, yIntermediate);

            double closestDistance = Double.POSITIVE_INFINITY;
            int closestHeight = 0;

            for (Map.Entry<Integer, Biome> entry : heightToBiome.entries()) {
                int blockHeight = entry.getKey();
                double diff = Math.abs(blockHeight - interpolated);
                if (diff < closestDistance) {
                    closestHeight = blockHeight;
                    closestDistance = diff;
                }
            }

            Object[] biomesForHeight = heightToBiome.get(closestHeight).toArray();
            if (biomesForHeight.length != 1) {
                if (xIntermediate * xIntermediate + yIntermediate * yIntermediate + new Random(xOrigin * yOrigin).nextDouble() * 0.02d > 0.25) {
                    double phi = Math.atan2(yIntermediate, xIntermediate);
                    int dirPhi = (int) (Math.floor((phi + Math.PI) / (2 * Math.PI) * 8.d + 0.5) % 8.0d);
                    if (dirPhi == 8) {
                        dirPhi = 7;
                    }
                    if (dirPhi == 0 || dirPhi == 1 || dirPhi == 7) {
                        xOrigin -= 1;
                    } else if (dirPhi == 3 || dirPhi == 4 || dirPhi == 5) {
                        xOrigin += 1;
                    }
                    if (dirPhi == 1 || dirPhi == 2 || dirPhi == 3) {
                        yOrigin -= 1;
                    } else if (dirPhi == 5 || dirPhi == 6 || dirPhi == 7) {
                        yOrigin += 1;
                    }
                }
                return this.getBiome(xOrigin, yOrigin);
            } else {
                return (Biome) biomesForHeight[0];
            }
        } else {
            return prevBiome;
        }
    }

    public static double cubic(double[] p, double x) {
        return p[1] + 0.5 * x * (p[2] - p[0] + x * (2.0 * p[0] - 5.0 * p[1] + 4.0 * p[2] - p[3] + x * (3.0 * (p[1] - p[2]) + p[3] - p[0])));
    }

    public void clearCache() {
        this.heightmap.clearCache();
        this.biomemap.clearCache();
    }

    protected static class Bicubic {
        protected static final ThreadLocal<double[]> ARR_THREADSAFE = new ThreadLocal<double[]>() {
            @Override
            protected double[] initialValue() {
                return new double[4];
            }
        };

        public static double bicubic(double[][] p, double x, double y) {
            double[] arr = ARR_THREADSAFE.get();
            arr[0] = cubic(p[0], y);
            arr[1] = cubic(p[1], y);
            arr[2] = cubic(p[2], y);
            arr[3] = cubic(p[3], y);
            return cubic(arr, x);
        }
    }

    public double extractHeight(int x, int y) {
        if (x < 0 || x >= this.heightmap.getWidth() || y < 0 || y >= this.heightmap.getHeight()) {
            return 0;
        }
        return this.heightmap.getData(x, y);
    }

    public double getHeight(int x, int y) {
        double height = this.extractHeight(x, y);
        if (height < 62) {
            height /= 4;
        } else {
            height -= 44;
        }
        return Math.max(1, height * 1.2);
    }

    public Biome getBiome(int x, int y) {
        try {
            return EarthBiome.values()[(this.biomemap.getData(x, y))].get();
        } catch (Exception e) {
            e.printStackTrace();
            return DEFAULT_BIOME;
        }
    }

    public double toLatitude(double z) {
        return this.fromZ(z + STANDARD_PARALLEL);
    }

    public double toLongitude(double x) {
        return this.fromX((x / (MathHelper.cos((float) Math.toRadians(STANDARD_PARALLEL)))) + CENTRAL_MERIDIAN);
    }

    public double fromLatitude(double latitude) {
        return this.toZ(latitude - STANDARD_PARALLEL);
    }

    public double fromLongitude(double longitude) {
        return this.toX((longitude - CENTRAL_MERIDIAN) * MathHelper.cos((float) Math.toRadians(STANDARD_PARALLEL)));
    }

    protected double fromZ(double z) {
        double scale = this.getWorldScale();
        return 90.0 - (z + WORLD_OFFSET_Z * scale) / (this.heightmap.getHeight() * scale) * 180.0;
    }

    protected double fromX(double x) {
        double scale = this.getWorldScale();
        return (x + WORLD_OFFSET_X * scale) / (this.heightmap.getWidth() * scale) * 360.0 - 180.0;
    }

    protected double toZ(double latitude) {
        int height = this.heightmap.getHeight();
        double scale = this.getWorldScale();
        double scaledZ = height - (latitude + 90.0) / 180.0 * (double) height;
        return (scaledZ - WORLD_OFFSET_Z) * scale;
    }

    protected double toX(double longitude) {
        double scale = this.getWorldScale();
        double scaledX = (longitude + 180.0) / 360.0 * (double) this.heightmap.getWidth();
        return (scaledX - WORLD_OFFSET_X) * scale;
    }

    protected double getWorldScale() {
        return WORLD_SCALE;
    }
}