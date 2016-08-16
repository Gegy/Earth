package net.gegy1000.earth.client.map;

import net.gegy1000.earth.client.texture.AdvancedDynamicTexture;
import net.gegy1000.earth.google.MapOverlayTile;
import net.gegy1000.earth.server.util.TempFileUtil;
import net.gegy1000.earth.server.world.gen.EarthGenerator;
import net.gegy1000.earth.server.world.gen.WorldTypeEarth;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.LinkedBlockingDeque;

public class MapOverlayHandler {
    public static final Map<BlockPos, AdvancedDynamicTexture> TILES = new HashMap<>();

    private static final Minecraft MC = Minecraft.getMinecraft();

    private static final Queue<BlockPos> WRITE_QUEUE = new LinkedBlockingDeque<>();
    private static final Queue<BlockPos> READ_QUEUE = new LinkedBlockingDeque<>();
    private static final Queue<BlockPos> DOWNLOAD_QUEUE = new LinkedBlockingDeque<>();
    private static final Map<BlockPos, BufferedImage> UNLOADED_IMAGES = new HashMap<>();

    private static final Set<BlockPos> CACHE_INDEX = new HashSet<>();

    public static final int BASE_RES = 64;

    private static boolean indexing = false;

    private static boolean failedDownload = false;

    private static final Thread WRITE_THREAD = new Thread(() -> {
        while (true) {
            while (WRITE_QUEUE.size() > 0) {
                BlockPos pos = null;
                synchronized (WRITE_QUEUE) {
                    pos = WRITE_QUEUE.poll();
                }
                AdvancedDynamicTexture tile = TILES.get(pos);
                if (tile != null) {
                    try {
                        ImageIO.write(tile.toBufferedImage(), "png", TempFileUtil.createTempFile(getTileFileName(pos)));
                    } catch (IOException e) {
                    }
                }
            }
        }
    });

    private static final Thread READ_THREAD = new Thread(() -> {
        while (true) {
            synchronized (READ_QUEUE) {
                while (READ_QUEUE.size() > 0) {
                    BlockPos pos = READ_QUEUE.poll();
                    try {
                        BufferedImage image = ImageIO.read(TempFileUtil.getTempFile(MapOverlayHandler.getTileFileName(pos)));
                        if (image != null) {
                            synchronized (UNLOADED_IMAGES) {
                                UNLOADED_IMAGES.put(pos, image);
                            }
                        }
                    } catch (IOException e) {
                    }
                }
            }
        }
    });

    private static final Thread DOWNLOAD_THREAD = new Thread(() -> {
        while (true) {
            if (MC.theWorld != null && MC.theWorld.getWorldType() instanceof WorldTypeEarth) {
                BlockPos pos = null;
                synchronized (DOWNLOAD_QUEUE) {
                    if (DOWNLOAD_QUEUE.size() > 0) {
                        pos = DOWNLOAD_QUEUE.poll();
                    }
                }
                if (pos != null) {
                    try {
                        EarthGenerator generator = WorldTypeEarth.getGenerator(MC.theWorld);
                        int downloadScale = getDownloadScale(MC.theWorld);
                        MapOverlayTile downloadedTile = MapOverlayTile.get(generator.toLat(pos.getZ() + (downloadScale / 2.0)), generator.toLong(pos.getX() + (downloadScale / 2.0)));
                        BufferedImage downloadedImage = downloadedTile.getImage();
                        WorldTypeEarth worldType = (WorldTypeEarth) MC.theWorld.getWorldType();
                        int zoomX = worldType.getMapZoomX();
                        int zoomY = worldType.getMapZoomY();
                        int width = downloadedImage.getWidth() - zoomX;
                        int height = downloadedImage.getHeight() - zoomY;
                        int halfZoomX = zoomX / 2;
                        int halfZoomY = zoomY / 2;
                        BufferedImage zoomedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                        for (int x = halfZoomX; x < width + halfZoomX; x++) {
                            for (int y = halfZoomY; y < height + halfZoomY; y++) {
                                zoomedImage.setRGB(x - halfZoomX, y - halfZoomY, downloadedImage.getRGB(x, y));
                            }
                        }
                        for (int xOffset = 0; xOffset < downloadScale; xOffset++) {
                            for (int yOffset = 0; yOffset < downloadScale; yOffset++) {
                                int sectionWidth = width / downloadScale;
                                int sectionHeight = height / downloadScale;
                                BufferedImage tile = new BufferedImage(sectionWidth, sectionHeight, BufferedImage.TYPE_INT_ARGB);
                                int startX = xOffset * sectionWidth;
                                int startY = yOffset * sectionHeight;
                                for (int x = startX; x < startX + sectionWidth; x++) {
                                    for (int y = startY; y < startY + sectionHeight; y++) {
                                        tile.setRGB(x - startX, y - startY, zoomedImage.getRGB(x, y));
                                    }
                                }
                                BlockPos tilePos = new BlockPos(pos.add(xOffset, 0, yOffset));
                                synchronized (UNLOADED_IMAGES) {
                                    UNLOADED_IMAGES.put(tilePos, tile);
                                }
                            }
                        }
                    } catch (IOException e) {
                        failedDownload = true;
                    }
                }
            }
        }
    });

    private static int getDownloadScale(World world) {
        if (world.getWorldType() instanceof WorldTypeEarth) {
            return ((WorldTypeEarth) world.getWorldType()).getMapDownloadScale();
        }
        return 8;
    }

    private static final Thread INDEX_CACHE = new Thread(() -> {
        while (true) {
            try {
                Thread.sleep(5000);
                indexing = true;
                CACHE_INDEX.clear();
                File file = TempFileUtil.getTempFile("map_overlay/");
                File[] children = file.listFiles();
                if (file.exists() && children != null) {
                    for (File child : children) {
                        try {
                            String[] split = child.getName().split(".png")[0].split("_");
                            CACHE_INDEX.add(new BlockPos(Integer.parseInt(split[0]), 0, Integer.parseInt(split[1])));
                        } catch (Exception e) {
                        }
                    }
                }
                indexing = false;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    });

    static {
        WRITE_THREAD.setName("Map Overlay Write Thread");
        WRITE_THREAD.start();
        READ_THREAD.setName("Map Overlay Read Thread");
        READ_THREAD.start();
        DOWNLOAD_THREAD.setName("Map Overlay Download Thread");
        DOWNLOAD_THREAD.start();
        INDEX_CACHE.setName("Map Overlay Index Thread");
        INDEX_CACHE.start();
    }

    public static void update() {
        if (UNLOADED_IMAGES.size() > 0) {
            synchronized (UNLOADED_IMAGES) {
                for (Map.Entry<BlockPos, BufferedImage> entry : UNLOADED_IMAGES.entrySet()) {
                    BlockPos pos = entry.getKey();
                    synchronized (TILES) {
                        TILES.put(pos, new AdvancedDynamicTexture(pos.getX() + "_" + pos.getZ(), entry.getValue()));
                    }
                    synchronized (WRITE_QUEUE) {
                        WRITE_QUEUE.add(pos);
                    }
                }
                UNLOADED_IMAGES.clear();
            }
        }
    }

    public static AdvancedDynamicTexture get(BlockPos pos) {
        AdvancedDynamicTexture tile = TILES.get(pos);
        if (tile == null) {
            boolean cached = !indexing && CACHE_INDEX.contains(pos);
            if (cached) {
                if (!READ_QUEUE.contains(pos)) {
                    synchronized (READ_QUEUE) {
                        READ_QUEUE.add(pos);
                    }
                }
            } else {
                if (!failedDownload) {
                    int downloadScale = getDownloadScale(MC.theWorld);
                    BlockPos corner = new BlockPos(pos.getX() - modulus(pos.getX(), downloadScale), 0, pos.getZ() - modulus(pos.getZ(), downloadScale));
                    if (!DOWNLOAD_QUEUE.contains(corner)) {
                        synchronized (DOWNLOAD_QUEUE) {
                            DOWNLOAD_QUEUE.add(corner);
                        }
                    }
                }
            }
        }
        return tile;
    }

    private static int modulus(int n, int d) {
        int modulus = n % d;
        if (modulus < 0) {
            modulus += d;
        }
        return modulus;
    }

    private static String getTileFileName(BlockPos pos) {
        return "map_overlay/" + pos.getX() + "_" + pos.getZ() + ".png";
    }
}
