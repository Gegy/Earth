package net.gegy1000.earth.client.event;

import net.gegy1000.earth.client.gui.GuiStreetView;
import net.gegy1000.earth.client.key.EarthKeyBinds;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;

public class ClientEventHandler
{
    private static final Minecraft mc = Minecraft.getMinecraft();

    @SubscribeEvent
    public void keyPress(InputEvent.KeyInputEvent event)
    {
        if (EarthKeyBinds.key_streetview.isPressed())
        {
            mc.displayGuiScreen(new GuiStreetView(mc.thePlayer));
        }
    }
}
