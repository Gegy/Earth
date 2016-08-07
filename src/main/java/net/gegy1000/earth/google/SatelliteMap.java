package net.gegy1000.earth.google;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

public class SatelliteMap {
    private final BufferedImage image;

    private SatelliteMap(BufferedImage image) {
        this.image = image;
    }

    public BufferedImage getImage() {
        return this.image;
    }

    public static SatelliteMap get(double lat, double lon) throws IOException {
        String req = "size=640x320&center=" + lat + "," + lon + "&zoom=15&maptype=hybrid&markers=" + lat + "," + lon;
        URL url = new URL("https://maps.googleapis.com/maps/api/staticmap?" + req);

        return new SatelliteMap(ImageIO.read(url.openStream()));
    }
}
