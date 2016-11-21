package net.gegy1000.earth.client.map;

import net.gegy1000.earth.Earth;

public class MapPoint {
    private final double x;
    private final double y;
    private final double z;
    private final double latitude;
    private final double longitude;

    public MapPoint(double latitude, double longitude) {
        this.x = Earth.GENERATOR.fromLong(longitude);
        this.z = Earth.GENERATOR.fromLat(latitude);
        this.y = Earth.GENERATOR.getHeightForCoords((int) this.x, (int) this.z) + 1.1;
        this.latitude = latitude;
        this.longitude = longitude;
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
}
