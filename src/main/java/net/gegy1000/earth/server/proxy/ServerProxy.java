package net.gegy1000.earth.server.proxy;

import net.gegy1000.earth.server.command.CommandEarthTeleport;
import net.gegy1000.earth.server.event.ServerEventHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;

public class ServerProxy {
    public void preInit() {
        MinecraftForge.EVENT_BUS.register(new ServerEventHandler());
    }

    public void serverStart(FMLServerStartingEvent event) {
        event.registerServerCommand(new CommandEarthTeleport());
    }
}
