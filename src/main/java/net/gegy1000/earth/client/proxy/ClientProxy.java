package net.gegy1000.earth.client.proxy;

import net.gegy1000.earth.client.event.ClientEventHandler;
import net.gegy1000.earth.client.key.EarthKeyBinds;
import net.gegy1000.earth.server.proxy.ServerProxy;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class ClientProxy extends ServerProxy
{
    @Override
    public void preInit()
    {
        super.preInit();

        EarthKeyBinds.init();

        MinecraftForge.EVENT_BUS.register(new ClientEventHandler());
    }

    @Override
    public void scheduleTask(MessageContext ctx, Runnable runnable)
    {
        Minecraft.getMinecraft().addScheduledTask(runnable);
    }
}
