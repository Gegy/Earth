package net.gegy1000.earth.client.texture;

import net.gegy1000.earth.Earth;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;

import java.awt.image.BufferedImage;

public class AdvancedDynamicTexture extends DynamicTexture {
    private static final Minecraft MC = Minecraft.getMinecraft();

    private ResourceLocation resource;

    private final int width;
    private final int height;

    public AdvancedDynamicTexture(String name, BufferedImage image) {
        super(image);
        this.resource = MC.getTextureManager().getDynamicTextureLocation(Earth.MODID + ":" + name, this);
        this.width = image.getWidth();
        this.height = image.getHeight();
    }

    public void bind() {
        MC.getTextureManager().bindTexture(this.resource);
    }

    public void delete() {
        MC.getTextureManager().deleteTexture(this.resource);
    }

    @Override
    protected void finalize() throws Throwable {
        this.delete();
        super.finalize();
    }

    public BufferedImage toBufferedImage() {
        BufferedImage image = new BufferedImage(this.width, this.height, BufferedImage.TYPE_INT_ARGB);
        image.setRGB(0, 0, this.width, this.height, this.getTextureData(), 0, this.width);
        return image;
    }
}
