package net.gegy1000.earth.server.util.google;

import net.gegy1000.earth.server.util.MapPoint;

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

    public static SatelliteMap get(MapPoint point) throws IOException {
        String request = "size=640x320&center=" + point.getLatitude() + "," + point.getLongitude() + "&zoom=15&maptype=hybrid&markers=" + point.getLatitude() + "," + point.getLongitude();
        URL url = new URL("https://maps.googleapis.com/maps/api/staticmap?" + request);
        return new SatelliteMap(ImageIO.read(url.openStream()));
    }
}
