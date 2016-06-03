package net.gegy1000.earth.client.event;

import net.gegy1000.earth.client.gui.GuiMap;
import net.gegy1000.earth.client.gui.GuiStreetView;
import net.gegy1000.earth.client.gui.GuiTeleportPlace;
import net.gegy1000.earth.client.key.EarthKeyBinds;
import net.gegy1000.earth.server.world.gen.WorldTypeEarth;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;

public class ClientEventHandler {
    private static final Minecraft mc = Minecraft.getMinecraft();

    @SubscribeEvent
    public void keyPress(InputEvent.KeyInputEvent event) {
        if (mc.theWorld.getWorldType() instanceof WorldTypeEarth) {
            if (EarthKeyBinds.KEY_STREET_VIEW.isPressed()) {
                mc.displayGuiScreen(new GuiStreetView(mc.thePlayer));
            } else if (EarthKeyBinds.KEY_TELEPORT_PLACE.isPressed()) {
                mc.displayGuiScreen(new GuiTeleportPlace());
            } else if (EarthKeyBinds.KEY_SHOW_MAP.isPressed()) {
                mc.displayGuiScreen(new GuiMap(mc.thePlayer));
            }
        }
    }
}
