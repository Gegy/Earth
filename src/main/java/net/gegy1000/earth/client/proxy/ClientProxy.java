package net.gegy1000.earth.client.proxy;

import net.gegy1000.earth.client.event.ClientEventHandler;
import net.gegy1000.earth.client.gui.OverlayGUI;
import net.gegy1000.earth.client.key.EarthKeyBinds;
import net.gegy1000.earth.server.proxy.ServerProxy;
import net.minecraftforge.common.MinecraftForge;

public class ClientProxy extends ServerProxy {
    @Override
    public void preInit() {
        super.preInit();

        EarthKeyBinds.init();

        MinecraftForge.EVENT_BUS.register(new ClientEventHandler());
        MinecraftForge.EVENT_BUS.register(new OverlayGUI());
    }
}
