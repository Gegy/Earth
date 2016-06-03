package net.gegy1000.earth.google;

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
        return image;
    }

    public static StreetView get(double lat, double lon, float yaw, float pitch) throws IOException {
        String req = "size=640x320&location=" + lat + "," + lon + "&heading=" + yaw + "&pitch=" + pitch;
        URL url = new URL("https://maps.googleapis.com/maps/api/streetview?" + req);

        return new StreetView(ImageIO.read(url.openStream()));
    }
}
