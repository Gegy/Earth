package net.gegy1000.earth.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class OverlayGUI extends Gui {
    private static final Minecraft MC = Minecraft.getMinecraft();

    @SubscribeEvent
    public void onGUIRender(RenderGameOverlayEvent event) {
    }
}
