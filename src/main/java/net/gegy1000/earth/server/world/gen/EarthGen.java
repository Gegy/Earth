package net.gegy1000.earth.server.world.gen;

import com.google.common.collect.HashMultimap;
import net.minecraft.world.biome.BiomeGenBase;

import java.io.IOException;
import java.util.Map;
import java.util.Random;

public class EarthGen
{
    private DataMap heightmap;
    private DataMap biomemap;

    private static final String HEIGHTMAP_LOCATION = "/assets/earth/textures/heightmap/earth_heightmap.mchmap";
    private static final String BIOMEMAP_LOCATION = "/assets/earth/textures/heightmap/earth_biomemap.mcbmap";

    private static final double WORLD_SCALE = 10.0;

    private static final int WORLD_OFFSET_X = 21600;
    private static final int WORLD_OFFSET_Z = 10800;

    public static final BiomeGenBase DEFAULT_BIOME = BiomeGenBase.ocean;

    public void load() throws IOException
    {
        if (heightmap == null)
        {
            loadHeightmap();
        }

        if (biomemap == null)
        {
            loadBiomemap();
        }
    }

    public void loadHeightmap() throws IOException
    {
        System.out.println("Loading Earth Heightmap...");

        heightmap = DataMap.construct(HEIGHTMAP_LOCATION, true);
    }

    public void loadBiomemap() throws IOException
    {
        System.out.println("Loading Earth Biomemap...");

        biomemap = DataMap.construct(BIOMEMAP_LOCATION, false);
    }

    public int getHeightForCoords(int x, int z)
    {
        int width = heightmap.getWidth();
        int height = biomemap.getHeight();

        int scaledWidth = (int) (width * WORLD_SCALE);
        int scaledHeight = (int) (height * WORLD_SCALE);

        x += (WORLD_OFFSET_X * WORLD_SCALE);
        z += (WORLD_OFFSET_Z * WORLD_SCALE);

        double[][] buffer = new double[4][4];

        double xScaled = (double) x / (scaledWidth - 1) * (width - 1);
        double yScaled = (double) z / (scaledHeight - 1) * (height - 1);
        int xOrigin = (int) xScaled;
        int yOrigin = (int) yScaled;
        double xIntermediate = xScaled - xOrigin;
        double yIntermediate = yScaled - yOrigin;

        for (int u = 0; u < 4; u++)
        {
            for (int v = 0; v < 4; v++)
            {
                buffer[u][v] = getHeight(xOrigin - 1 + u, yOrigin - 1 + v);
            }
        }

        int value = (int) Math.round(Bicubic.bicubic(buffer, xIntermediate, yIntermediate));
        value = Math.min(0xff, Math.max(value, 0));

        return value;
    }

    public BiomeGenBase getBiomeForCoords(int x, int z)
    {
        int width = biomemap.getWidth();
        int height = biomemap.getHeight();

        int scaledWidth = (int) (width * WORLD_SCALE);
        int scaledHeight = (int) (height * WORLD_SCALE);

        x += (WORLD_OFFSET_X * WORLD_SCALE);
        z += (WORLD_OFFSET_Z * WORLD_SCALE);

        if (x < 0 || z < 0 || x >= scaledWidth || z >= scaledHeight)
        {
            return DEFAULT_BIOME;
        }

        HashMultimap<Integer, Integer> heightToBiome = HashMultimap.create();

        double[][] buffer = new double[4][4];

        double xScaled = (double) x / (scaledWidth - 1) * (width - 1);
        double yScaled = (double) z / (scaledHeight - 1) * (height - 1);
        int xOrigin = (int) xScaled;
        int yOrigin = (int) yScaled;
        double xIntermediate = xScaled - xOrigin;
        double yIntermediate = yScaled - yOrigin;

        BiomeGenBase prevBiome = null;
        boolean hasMultipleBiomes = false;

        for (int u = 0; u < 4; u++)
        {
            for (int v = 0; v < 4; v++)
            {
                int dataX = xOrigin - 1 + u;
                int dataY = yOrigin - 1 + v;

                int blockHeight = (int) getHeight(dataX, dataY);
                buffer[u][v] = blockHeight;

                BiomeGenBase biome = getBiome(dataX, dataY);

                if (prevBiome != null && (!biome.equals(prevBiome)))
                {
                    hasMultipleBiomes = true;
                }

                heightToBiome.put(blockHeight, biome.biomeID);

                prevBiome = biome;
            }
        }

        if (hasMultipleBiomes)
        {
            double interpolated = Bicubic.bicubic(buffer, xIntermediate, yIntermediate);

            double closestDistance = Double.POSITIVE_INFINITY;
            int closestHeight = 0;

            for (Map.Entry<Integer, Integer> entry : heightToBiome.entries())
            {
                int blockHeight = entry.getKey();
                double diff = Math.abs(blockHeight - interpolated);

                if (diff < closestDistance)
                {
                    closestHeight = blockHeight;
                    closestDistance = diff;
                }
            }

            Object[] biomesForHeight = heightToBiome.get(closestHeight).toArray();

            if (biomesForHeight.length != 1)
            {
                if (xIntermediate * xIntermediate + yIntermediate * yIntermediate + new Random(xOrigin * yOrigin).nextDouble() * 0.02d > 0.25)
                {
                    // Ourside a radius of 0.5 from the middle and some jitter
                    double phi = Math.atan2(yIntermediate, xIntermediate);
                    int dirPhi = (int) (Math.floor((phi + Math.PI) / (2 * Math.PI) * 8.d + 0.5) % 8.0d);
                    if (dirPhi == 8)
                    {
                        dirPhi = 7;
                    }
                    // Direction 0 corresponds to negative x, 1 to negative x, y ...
                    if (dirPhi == 0 || dirPhi == 1 || dirPhi == 7)
                    {
                        xOrigin -= 1;
                    }
                    else if (dirPhi == 3 || dirPhi == 4 || dirPhi == 5)
                    {
                        xOrigin += 1;
                    }
                    if (dirPhi == 1 || dirPhi == 2 || dirPhi == 3)
                    {
                        yOrigin -= 1;
                    }
                    else if (dirPhi == 5 || dirPhi == 6 || dirPhi == 7)
                    {
                        yOrigin += 1;
                    }
                }

                return getBiome(xOrigin, yOrigin);
//              return BiomeGenBase.getBiome((Integer) biomesForHeight[new Random(x * z).nextInt(biomesForHeight.length)]);
            }
            else
            {
                return BiomeGenBase.getBiomeGenArray()[(Integer) biomesForHeight[0]];
            }
        }
        else
        {
            return prevBiome;
        }
    }

    public static double cubic(double[] p, double x)
    {
        return p[1] + 0.5 * x * (p[2] - p[0] + x * (2.0 * p[0] - 5.0 * p[1] + 4.0 * p[2] - p[3] + x * (3.0 * (p[1] - p[2]) + p[3] - p[0])));
    }

    public void clearCache()
    {
        heightmap.clearCache();
        biomemap.clearCache();
    }

    private static class Bicubic
    {
        private static ThreadLocal<double[]> ARR_THREADSAFE = new ThreadLocal<double[]>()
        {
            protected double[] initialValue()
            {
                return new double[4];
            }
        };

        public static double bicubic(double[][] p, double x, double y)
        {
            double[] arr = ARR_THREADSAFE.get();
            arr[0] = cubic(p[0], y);
            arr[1] = cubic(p[1], y);
            arr[2] = cubic(p[2], y);
            arr[3] = cubic(p[3], y);
            return cubic(arr, x);
        }
    }

    public double extractHeight(int x, int y)
    {
        if (x < 0 || x >= heightmap.getWidth() || y < 0 || y >= heightmap.getHeight())
        {
            return Math.min(Math.max(0, 0), 255);
        }

        return heightmap.getData(x, y);
    }

    public double getHeight(int x, int y)
    {
        return (int) ((extractHeight(x, y) * 0.8) + 10);
    }

    public BiomeGenBase getBiome(int x, int y)
    {
        try
        {
            BiomeGenBase biome = BiomeGenBase.getBiome(biomemap.getData(x, y));

            if (biome == null)
            {
                biome = DEFAULT_BIOME;
            }

            return biome;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return DEFAULT_BIOME;
        }
    }

    public double toLat(double z)
    {
        return 90.0 - (((z + (WORLD_OFFSET_Z * WORLD_SCALE)) / (heightmap.getHeight() * WORLD_SCALE)) * 180.0);
    }

    public double toLong(double x)
    {
        return (((x + (WORLD_OFFSET_X * WORLD_SCALE)) / (heightmap.getWidth() * WORLD_SCALE)) * 360.0) - 180.0;
    }

    public double fromLat(double lat)
    {
        int height = heightmap.getHeight();

        double scale = WORLD_SCALE;
        double scaledZ = height - ((lat + 90.0) / 180.0 * (double) height);
        return (scaledZ - WORLD_OFFSET_Z) * scale;
    }

    public double fromLong(double longitude)
    {
        double scale = WORLD_SCALE;
        double scaledX = ((longitude + 180.0) / 360.0 * (double) heightmap.getWidth());
        return (scaledX - WORLD_OFFSET_X) * scale;
    }
}