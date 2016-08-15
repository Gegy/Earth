package net.gegy1000.earth.client.gui;

import net.gegy1000.earth.client.texture.AdvancedDynamicTexture;
import net.gegy1000.earth.google.SatelliteMap;
import net.gegy1000.earth.google.geocode.ReverseGeoCode;
import net.gegy1000.earth.server.world.gen.EarthGenerator;
import net.gegy1000.earth.server.world.gen.WorldTypeEarth;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import org.lwjgl.opengl.GL11;

import java.awt.image.BufferedImage;
import java.io.IOException;

public class MapGUI extends GuiScreen {
    private AdvancedDynamicTexture dynamicTexture;
    private BufferedImage image;
    private final double latitude;
    private final double longitude;
    private String address;

    public MapGUI(final EntityPlayer player) {
        EarthGenerator generator = WorldTypeEarth.getGenerator(player.worldObj);
        this.latitude = generator.toLat(player.posZ);
        this.longitude = generator.toLong(player.posX);

        Thread downloadThread = new Thread(() -> {
            try {
                SatelliteMap satelliteMap = SatelliteMap.get(this.latitude, this.longitude);
                this.image = satelliteMap.getImage();
                this.address = ReverseGeoCode.get(this.latitude, this.longitude).getFormattedAddress();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        downloadThread.start();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);

        this.drawDefaultBackground();

        ScaledResolution scaledResolution = new ScaledResolution(this.mc);

        int scaledWidth = scaledResolution.getScaledWidth();
        int scaledHeight = scaledResolution.getScaledHeight();

        if (this.dynamicTexture == null) {
            if (this.image != null) {
                this.dynamicTexture = new AdvancedDynamicTexture("sattelite_map", this.image);
            }

            this.fontRendererObj.drawStringWithShadow("Downloading Image...", 10, 10, 0xFF0000);
        } else {
            this.fontRendererObj.drawStringWithShadow("Location: Lat: " + this.latitude + ", Long: " + this.longitude, 5, 5, 0xFF0000);
            this.fontRendererObj.drawStringWithShadow(this.address, 5, 15, 0x00FFFF);

            this.dynamicTexture.bind();

            double scaleX = (scaledWidth / 640.0) * 0.8;
            double scaleY = (scaledHeight / 320.0) * 0.8;

            this.drawTexturedModalRect((int) ((scaledWidth / 2) - ((640.0 * scaleX) / 2)), (int) ((scaledHeight / 2) - ((320.0 * scaleY) / 2)), 0, 0, 640, 320, 640, 320, scaleX, scaleY);
        }
    }

    public void drawTexturedModalRect(int x, int y, int textureX, int textureY, int width, int height, int texWidth, int texHeight, double scaleX, double scaleY) {
        GlStateManager.pushMatrix();
        GlStateManager.color(1.0F, 1.0F, 1.0F);
        GlStateManager.scale(scaleX, scaleY, 0.0);

        x /= scaleX;
        y /= scaleY;

        float f = 1.0F / texWidth;
        float f1 = 1.0F / texHeight;
        Tessellator tessellator = Tessellator.getInstance();
        VertexBuffer buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        buffer.pos(x, y + height, this.zLevel).tex(textureX * f, (textureY + height) * f1).endVertex();
        buffer.pos(x + width, y + height, this.zLevel).tex((textureX + width) * f, (textureY + height) * f1).endVertex();
        buffer.pos(x + width, y, this.zLevel).tex((textureX + width) * f, (textureY * f1)).endVertex();
        buffer.pos(x, y, this.zLevel).tex(textureX * f, textureY * f1).endVertex();
        tessellator.draw();
        GlStateManager.popMatrix();
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        if (this.dynamicTexture != null) {
            this.dynamicTexture.delete();
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
