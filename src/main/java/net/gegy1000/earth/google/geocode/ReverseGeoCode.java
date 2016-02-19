package net.gegy1000.earth.google.geocode;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

public class ReverseGeoCode
{
    private String formattedAddress;

    private ReverseGeoCode(String formattedAddress)
    {
        this.formattedAddress = formattedAddress;
    }

    public static ReverseGeoCode get(double lat, double lon) throws IOException
    {
        String req = "latlng=" + lat + "," + lon + "&sensor=false";
        URL url = new URL("http://maps.googleapis.com/maps/api/geocode/json?" + req);

        Gson gson = new Gson();
        GeoCodeContainer container = gson.fromJson(new InputStreamReader(url.openStream()), GeoCodeContainer.class);

        return new ReverseGeoCode(container.results[0].formatted_address);
    }

    public String getFormattedAddress()
    {
        return formattedAddress;
    }
}
