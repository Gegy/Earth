package net.gegy1000.earth.client.gui;

import net.gegy1000.earth.client.event.ClientEventHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class OverlayGUI extends Gui {
    private static final Minecraft MC = Minecraft.getMinecraft();

    @SubscribeEvent
    public void onGUIRender(RenderGameOverlayEvent event) {
        if (event.getType() == RenderGameOverlayEvent.ElementType.TEXT) {
            if (ClientEventHandler.isMapEnabled) {
                FontRenderer fontRenderer = MC.fontRendererObj;
                ScaledResolution resolution = new ScaledResolution(MC);
                int width = resolution.getScaledWidth();
                int height = resolution.getScaledHeight();
                String credit = "© OpenStreetMap contributors";
                int creditWidth = fontRenderer.getStringWidth(credit);
                drawRect(width - creditWidth - 3, height - 14, width, height, Integer.MIN_VALUE);
                fontRenderer.drawStringWithShadow(credit, width - creditWidth - 1, height - 11, 0xFFFFFF);
            }

        }
    }
}
