package net.gegy1000.earth.server.util.google.geocode;

import com.google.gson.Gson;
import net.gegy1000.earth.server.util.MapPoint;
import net.minecraft.world.World;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

public class GeoCode {
    private final MapPoint point;

    private GeoCode(MapPoint point) {
        this.point = point;
    }

    public static GeoCode get(World world, String place) throws IOException {
        String req = "address=" + place.replaceAll(" ", "+");
        URL url = new URL("https://maps.googleapis.com/maps/api/geocode/json?" + req);

        GeoCodeContainer container = new Gson().fromJson(new InputStreamReader(url.openStream()), GeoCodeContainer.class);

        if (container.results != null && container.results.length > 0) {
            GeoCodeContainer.Location location = container.results[0].geometry.location;
            return new GeoCode(new MapPoint(world, location.lat, location.lng));
        }
        return null;
    }

    public MapPoint getPoint() {
        return this.point;
    }
}
