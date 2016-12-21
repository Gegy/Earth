package net.gegy1000.earth.server.util.osm;

import net.gegy1000.earth.server.world.gen.EarthGenerator;
import net.gegy1000.earth.server.world.gen.WorldTypeEarth;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;

import java.util.Map;
import java.util.WeakHashMap;

public class MapHandler {
    private static final Map<ChunkPos, MapTile> MAP_TILES = new WeakHashMap<>();

    public static MapTile getTile(World world, int x, int z) {
        EarthGenerator generator = WorldTypeEarth.getGenerator(world);
        int latitude = (int) (generator.toLatitude(z) / MapTile.SIZE);
        int longitude = (int) (generator.toLongitude(x) / MapTile.SIZE);
        ChunkPos position = new ChunkPos(latitude, longitude);
        if (MAP_TILES.containsKey(position)) {
            return MAP_TILES.get(position);
        }
        MapTile mapTile = new MapTile(world, latitude, longitude);
        mapTile.load();
        MAP_TILES.put(position, mapTile);
        return mapTile;
    }
}
