package net.gegy1000.earth.server.util.google;

import net.gegy1000.earth.server.util.MapPoint;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

public class StreetView {
    private final BufferedImage image;

    private StreetView(BufferedImage image) {
        this.image = image;
    }

    public BufferedImage getImage() {
        return this.image;
    }

    public static StreetView get(MapPoint point, float yaw, float pitch) throws IOException {
        String req = "size=640x640&location=" + point.getLatitude() + "," + point.getLongitude() + "&heading=" + yaw + "&pitch=" + pitch;
        URL url = new URL("https://maps.googleapis.com/maps/api/streetview?" + req);

        return new StreetView(ImageIO.read(url.openStream()));
    }
}
