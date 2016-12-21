package net.gegy1000.earth.client.event;

import net.gegy1000.earth.client.gui.EarthMainMenuGUI;
import net.gegy1000.earth.client.gui.MapGUI;
import net.gegy1000.earth.client.gui.StreetViewGUI;
import net.gegy1000.earth.client.gui.TeleportPlaceGUI;
import net.gegy1000.earth.client.key.EarthKeyBinds;
import net.gegy1000.earth.server.world.gen.WorldTypeEarth;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;

public class ClientEventHandler {
    private static final Minecraft MC = Minecraft.getMinecraft();

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onGUIOpen(GuiOpenEvent event) {
        if (event.getGui() instanceof GuiMainMenu && !(event.getGui() instanceof EarthMainMenuGUI)) {
            event.setGui(new EarthMainMenuGUI());
        }
    }

    @SubscribeEvent
    public void onKeyPress(InputEvent.KeyInputEvent event) {
        if (MC.world.getWorldType() instanceof WorldTypeEarth) {
            if (EarthKeyBinds.KEY_STREET_VIEW.isPressed()) {
                MC.displayGuiScreen(new StreetViewGUI(MC.player));
            } else if (EarthKeyBinds.KEY_TELEPORT_PLACE.isPressed()) {
                MC.displayGuiScreen(new TeleportPlaceGUI());
            } else if (EarthKeyBinds.KEY_SHOW_MAP.isPressed()) {
                MC.displayGuiScreen(new MapGUI(MC.player));
            }
        }
    }
}
