package net.gegy1000.earth.google;

import net.gegy1000.earth.client.map.MapOverlayHandler;
import net.gegy1000.earth.server.world.gen.WorldTypeEarth;
import net.minecraft.client.Minecraft;
import net.minecraft.world.WorldType;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

public class MapOverlayTile {
    private final BufferedImage image;

    private MapOverlayTile(BufferedImage image) {
        this.image = image;
    }

    public BufferedImage getImage() {
        return this.image;
    }

    public static MapOverlayTile get(double lat, double lon) throws IOException {
        int size = MapOverlayHandler.BASE_RES * MapOverlayHandler.DOWNLOAD_SCALE;
        WorldType worldType = Minecraft.getMinecraft().theWorld.getWorldType();
        int zoom = 16;
        if (worldType instanceof WorldTypeEarth) {
            zoom = ((WorldTypeEarth) worldType).getMapZoom();
        }
        String req = "size=" + size + "x" + size + "&center=" + lat + "," + lon + "&zoom=" + zoom;
        URL url = new URL("https://maps.googleapis.com/maps/api/staticmap?" + req);
        return new MapOverlayTile(ImageIO.read(url.openStream()));
    }
}
