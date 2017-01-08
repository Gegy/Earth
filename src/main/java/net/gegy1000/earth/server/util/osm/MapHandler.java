package net.gegy1000.earth.server.util.osm;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import net.gegy1000.earth.server.world.gen.EarthGenerator;
import net.gegy1000.earth.server.world.gen.WorldTypeEarth;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import java.util.concurrent.TimeUnit;

public class MapHandler {
    private static final LoadingCache<MapTilePos, MapTile> MAP_TILES = CacheBuilder.newBuilder()
            .expireAfterAccess(1, TimeUnit.SECONDS)
            .maximumSize(6)
            .removalListener((RemovalListener<MapTilePos, MapTile>) notification -> notification.getValue().clear())
            .build(new CacheLoader<MapTilePos, MapTile>() {
                @Override
                public MapTile load(MapTilePos pos) {
                    MapTile mapTile = pos.create();
                    mapTile.load();
                    return mapTile;
                }
            });

    public static MapTile getTile(World world, int x, int z) {
        EarthGenerator generator = WorldTypeEarth.getGenerator(world);
        int latitude = MathHelper.floor(generator.toLatitude(z) / MapTile.SIZE);
        int longitude = MathHelper.floor(generator.toLongitude(x) / MapTile.SIZE);
        MapTilePos position = new MapTilePos(world, latitude, longitude);
        return MAP_TILES.getUnchecked(position);
    }

    public static class MapTilePos {
        private World world;
        private int latitude;
        private int longitude;

        public MapTilePos(World world, int latitude, int longitude) {
            this.world = world;
            this.latitude = latitude;
            this.longitude = longitude;
        }

        public MapTile create() {
            return new MapTile(this.world, this.latitude, this.longitude);
        }

        @Override
        public int hashCode() {
            return this.longitude + 18000 << 16 | this.latitude + 9000;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof MapTilePos) {
                MapTilePos pos = (MapTilePos) obj;
                return pos.latitude == this.latitude && pos.longitude == this.longitude;
            }
            return false;
        }
    }
}
