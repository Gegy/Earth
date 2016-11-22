package net.gegy1000.earth.server.util.google.geocode;

import com.google.gson.Gson;
import net.gegy1000.earth.server.util.MapPoint;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

public class ReverseGeoCode {
    private final String formattedAddress;

    private ReverseGeoCode(String formattedAddress) {
        this.formattedAddress = formattedAddress;
    }

    public static ReverseGeoCode get(MapPoint point) throws IOException {
        String req = "latlng=" + point.getLatitude() + "," + point.getLongitude() + "&sensor=false";
        URL url = new URL("http://maps.googleapis.com/maps/api/geocode/json?" + req);

        Gson gson = new Gson();
        GeoCodeContainer container = gson.fromJson(new InputStreamReader(url.openStream()), GeoCodeContainer.class);

        return new ReverseGeoCode(container.results.length > 0 ? container.results[0].formatted_address : "Unknown");
    }

    public String getFormattedAddress() {
        return this.formattedAddress;
    }
}
