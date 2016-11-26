package net.gegy1000.earth.server.world.gen;

import net.gegy1000.earth.Earth;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

public class ImageDataMap {
    private static final TileFactory DEFAULT_FACTORY = (image, width, height) -> {
        byte[] heights = null;
        if (image != null) {
            heights = new byte[width * height];
            int i = 0;
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    heights[i++] = (byte) (image.getRGB(x, y) & 0xFF);
                }
            }
        }
        return new Tile(heights, width, height);
    };

    private static final long DEAD_TIME = 30000;

    private final int width;
    private final int height;

    private final int tileWidth;
    private final int tileHeight;

    private final int tileCountX;
    private final int tileCountY;

    private final String[] tileAccess;
    private final Tile[] tiles;
    private final long[] lastAccess;

    private final TileFactory factory;

    private final Object lock = new Object();

    public ImageDataMap(int width, int height, int tileWidth, int tileHeight, String dataPath) {
        this(width, height, tileWidth, tileHeight, dataPath, DEFAULT_FACTORY);
    }

    public ImageDataMap(int width, int height, int tileWidth, int tileHeight, String dataPath, TileFactory factory) {
        this.width = width;
        this.height = height;
        this.tileWidth = tileWidth;
        this.tileHeight = tileHeight;
        this.tileCountX = width / tileWidth;
        this.tileCountY = height / tileHeight;
        this.tileAccess = new String[this.tileCountX * this.tileCountY];
        this.tiles = new Tile[this.tileCountX * this.tileCountY];
        this.lastAccess = new long[this.tileCountX * this.tileCountY];
        for (int x = 0; x < this.tileCountX; x++) {
            for (int y = 0; y < this.tileCountY; y++) {
                this.tileAccess[x + (y * this.tileCountX)] = "/" + dataPath + "_" + x + "_" + y + ".png";
            }
        }
        this.factory = factory;
        Thread thread = new Thread(() -> {
            while (true) {
                try {
                    boolean remove = false;
                    long time = System.currentTimeMillis();
                    synchronized (this.lock) {
                        for (int i = 0; i < this.tiles.length; i++) {
                            if (this.tiles[i] != null && time - this.lastAccess[i] > DEAD_TIME) {
                                this.tiles[i] = null;
                                Earth.LOGGER.info("Unload " + (i % this.tileCountX) + ", " + (i / this.tileCountX));
                                remove = true;
                            }
                        }
                    }
                    if (remove) {
                        System.gc();
                    }
                    Thread.sleep(1000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        thread.setName("ImageDataMap Dead Tile");
        thread.setDaemon(true);
        thread.start();
    }

    public int sample(int x, int y) {
        int tileX = x / this.tileWidth;
        int tileY = y / this.tileHeight;
        int tileSampleX = x % this.tileWidth;
        int tileSampleY = y % this.tileHeight;
        Tile tile = this.get(tileX, tileY);
        if (tile != null) {
            return tile.get(tileSampleX, tileSampleY);
        }
        return 0;
    }

    protected Tile get(int x, int y) {
        int index = x + (y * this.tileCountX);
        Tile tile;
        synchronized (this.lock) {
            tile = this.tiles[index];
            this.lastAccess[index] = System.currentTimeMillis();
        }
        if (tile == null) {
            tile = this.load(x, y);
            synchronized (this.lock) {
                this.tiles[index] = tile;
            }
        }
        return tile;
    }

    protected Tile load(int x, int y) {
        InputStream in = ImageDataMap.class.getResourceAsStream(this.tileAccess[x + (y * this.tileCountX)]);
        if (in != null) {
            try {
                Earth.LOGGER.info("Load " + x + ", " + y);
                return this.factory.create(ImageIO.read(in), this.tileWidth, this.tileHeight);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            return new Tile(null, this.tileWidth, this.tileHeight);
        }
        return null;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public static class Tile {
        private final int width;
        private final int height;
        private final byte[] heights;

        public Tile(byte[] heights, int width, int height) {
            this.width = width;
            this.height = height;
            this.heights = heights;
        }

        public int get(int x, int y) {
            if (this.heights == null) {
                return 0;
            }
            return this.heights[this.getIndex(x, y)] & 0xFF;
        }

        public int getWidth() {
            return this.width;
        }

        public int getHeight() {
            return this.height;
        }

        private int getIndex(int x, int y) {
            return x + (y * this.width);
        }
    }

    public static interface TileFactory {
        Tile create(BufferedImage image, int width, int height);
    }
}
