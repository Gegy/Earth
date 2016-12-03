package net.gegy1000.earth.client.map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayer;

import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * TODO:
 * Fix lighting
 * MapObject textures
 * Fix roads on different heights
 * Split mapobjects per block
 * Batch mapobjects
 * Way.Type color config
 * Objects being added multiple times
 * Single VBO for each MapTile
 */
public class MapHandler {
    public static final Set<Class<? extends MapObjectType>> MAP_OBJECT_TYPES = new HashSet<>();

    public static final int TILE_RANGE = 4;

    public static final Set<MapTile> MAP_TILES = new HashSet<>();
    public static final Object MAP_LOCK = new Object();

    private static final Minecraft MC = Minecraft.getMinecraft();
    private static final Queue<MapTile> LOAD_QUEUE = new PriorityBlockingQueue<>(11, (tile1, tile2) -> {
        EntityPlayerSP player = MC.thePlayer;
        if (player != null) {
            double distance1 = player.getDistanceSqToCenter(tile1.getCenter((int) player.posY));
            double distance2 = player.getDistanceSqToCenter(tile2.getCenter((int) player.posY));
            return Double.compare(distance1, distance2);
        }
        return 0;
    });

    static {
        MAP_OBJECT_TYPES.add(Area.Type.class);
        MAP_OBJECT_TYPES.add(Building.Type.class);
        MAP_OBJECT_TYPES.add(Way.Type.class);

        Thread loadThread = new Thread(() -> {
            while (true) {
                if (LOAD_QUEUE.size() > 0) {
                    EntityPlayerSP player = MC.thePlayer;
                    if (player != null) {
                        MapTile tile = LOAD_QUEUE.poll();
                        tile.load(MC.theWorld);
                        synchronized (MAP_LOCK) {
                            MAP_TILES.add(tile);
                        }
                    }
                }
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        loadThread.setName("Map Load Thread");
        loadThread.setDaemon(true);
        loadThread.start();
    }

    public static void loadTile(MapTile tile) {
        if (!MAP_TILES.contains(tile) && !LOAD_QUEUE.contains(tile)) {
            LOAD_QUEUE.add(tile);
        }
    }

    public static void update(EntityPlayer player) {
        Set<MapTile> newTiles = new HashSet<>();
        Set<MapTile> oldTiles = new HashSet<>();

        oldTiles.addAll(MAP_TILES);
        oldTiles.addAll(LOAD_QUEUE);

        int tileOffsetX = ((int) player.posX >>> MapTile.SHIFT) << MapTile.SHIFT;
        int tileOffsetZ = ((int) player.posZ >>> MapTile.SHIFT) << MapTile.SHIFT;
        for (int x = -TILE_RANGE; x < TILE_RANGE; x++) {
            for (int z = -TILE_RANGE; z < TILE_RANGE; z++) {
                MapTile tile = new MapTile(tileOffsetX + (x << MapTile.SHIFT), tileOffsetZ + (z << MapTile.SHIFT));
                if (MAP_TILES.contains(tile) || LOAD_QUEUE.contains(tile)) {
                    oldTiles.remove(tile);
                } else {
                    newTiles.add(tile);
                }
            }
        }

        synchronized (MAP_LOCK) {
            for (MapTile old : oldTiles) {
                old.delete();
            }
            MAP_TILES.removeAll(oldTiles);
            LOAD_QUEUE.removeAll(oldTiles);
        }

        for (MapTile newTile : newTiles) {
            MapHandler.loadTile(newTile);
        }
    }
}