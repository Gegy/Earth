package net.gegy1000.earth.client.gui;

import net.gegy1000.earth.Earth;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;

public class GuiSelectSpawn extends GuiScreen
{
    private static final ResourceLocation EARTH_MAP_IMAGE = new ResourceLocation(Earth.MODID, "textures/gui/earth_map.png");
    private static final ResourceLocation LOCATION_MARKER = new ResourceLocation(Earth.MODID, "textures/gui/location_marker.png");

    private int selectX, selectY;

    public void initGui()
    {
        this.buttonList.clear();

        ScaledResolution resolution = new ScaledResolution(mc);

        int scaledWidth = resolution.getScaledWidth();
        int scaledHeight = resolution.getScaledHeight();

        this.buttonList.add(new GuiButton(0, (scaledWidth / 2) - 100, scaledHeight - 30, "Spawn"));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        this.drawDefaultBackground();

        super.drawScreen(mouseX, mouseY, partialTicks);

        ScaledResolution resolution = new ScaledResolution(mc);

        int scaledWidth = resolution.getScaledWidth();
        int scaledHeight = resolution.getScaledHeight();

        drawCenteredString(fontRendererObj, "Select Spawn Location", scaledWidth / 2, 15, 0xFFFFFF);

        mc.renderEngine.bindTexture(EARTH_MAP_IMAGE);

        double scaleX = scaledWidth / 21600.0;
        double scaleY = scaledHeight / 10800.0;

        int mapX = (int) ((scaledWidth / 2) - ((14100.0 * scaleX) / 2));
        int mapY = (int) ((scaledHeight / 2) - ((7050.0 * scaleY) / 2));

        drawTexturedModalRect(mapX, mapY, 0, 0, 14100, 7050, 14100, 7050, scaleX, scaleY);

        mc.renderEngine.bindTexture(LOCATION_MARKER);
        drawTexturedModalRect(selectX + mapX, selectY + mapY, 0, 0, 16, 16, 16, 16, 0.85, 0.85); //TODO
    }

    @Override
    public void actionPerformed(GuiButton button)
    {
        if (button.id == 0)
        {
            //TODO
        }
    }

    /**
     * Draws a textured rectangle at the stored z-value. Args: x, y, u, v, width, height
     */
    public void drawTexturedModalRect(int x, int y, int textureX, int textureY, int width, int height, int texWidth, int texHeight, double scaleX, double scaleY)
    {
        GlStateManager.pushMatrix();
        GlStateManager.scale(scaleX, scaleY, 0.0);

        x /= scaleX;
        y /= scaleY;

        float f = 1.0F / texWidth;
        float f1 = 1.0F / texHeight;
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
        worldrenderer.pos((double) (x + 0), (double) (y + height), (double) this.zLevel).tex((double) ((float) (textureX + 0) * f), (double) ((float) (textureY + height) * f1)).endVertex();
        worldrenderer.pos((double) (x + width), (double) (y + height), (double) this.zLevel).tex((double) ((float) (textureX + width) * f), (double) ((float) (textureY + height) * f1)).endVertex();
        worldrenderer.pos((double) (x + width), (double) (y + 0), (double) this.zLevel).tex((double) ((float) (textureX + width) * f), (double) ((float) (textureY + 0) * f1)).endVertex();
        worldrenderer.pos((double) (x + 0), (double) (y + 0), (double) this.zLevel).tex((double) ((float) (textureX + 0) * f), (double) ((float) (textureY + 0) * f1)).endVertex();
        tessellator.draw();

        GlStateManager.popMatrix();
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
    {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        ScaledResolution resolution = new ScaledResolution(mc);

        int scaledWidth = resolution.getScaledWidth();
        int scaledHeight = resolution.getScaledHeight();

        double scaleX = scaledWidth / 21600.0;
        double scaleY = scaledHeight / 10800.0;

        int mapX = (int) ((scaledWidth / 2) - ((14100.0 * scaleX) / 2));
        int mapY = (int) ((scaledHeight / 2) - ((7050.0 * scaleY) / 2));

        int offset = (int) ((16.0 * 0.85) / 2);
        selectX = (int) Math.max(Math.min((mouseX - mapX) - offset, (14100.0 * scaleX) - (offset * 2)), 0);
        selectY = (int) Math.max(Math.min((mouseY - mapY) - offset, (7050.0 * scaleY) - (offset * 2)), 0);
    }
}
