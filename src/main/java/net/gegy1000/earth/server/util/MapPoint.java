package net.gegy1000.earth.server.util;

import net.gegy1000.earth.server.world.gen.EarthGenerator;
import net.gegy1000.earth.server.world.gen.WorldTypeEarth;
import net.minecraft.world.World;

public class MapPoint {
    private final double x;
    private final double z;
    private final double latitude;
    private final double longitude;
    private double y;

    public MapPoint(World world, double latitude, double longitude) {
        EarthGenerator generator = WorldTypeEarth.getGenerator(world);
        this.x = generator.fromLongitude(longitude);
        this.z = generator.fromLatitude(latitude);
        this.y = generator.getGenerationHeight((int) this.x, (int) this.z) + 1.1;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public MapPoint(World world, double x, double y, double z) {
        EarthGenerator generator = WorldTypeEarth.getGenerator(world);
        this.x = x;
        this.z = z;
        if (y < 0) {
            this.y = generator.getGenerationHeight((int) this.x, (int) this.z) + 1.1;
        } else {
            this.y = y;
        }
        this.latitude = generator.toLatitude(z);
        this.longitude = generator.toLongitude(x);
    }

    public double getX() {
        return this.x;
    }

    public double getZ() {
        return this.z;
    }

    public double getY() {
        return this.y;
    }

    public double getLatitude() {
        return this.latitude;
    }

    public double getLongitude() {
        return this.longitude;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(this.x);
        builder.append(',');
        builder.append(this.z);
        return builder.toString();
    }
}
