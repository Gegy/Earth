package net.gegy1000.earth.server.proxy;

import net.gegy1000.earth.server.command.CommandTPLatLong;
import net.gegy1000.earth.server.event.ServerEventHandler;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class ServerProxy
{
    public void preInit()
    {
        MinecraftForge.EVENT_BUS.register(new ServerEventHandler());
    }

    public void serverStart(FMLServerStartingEvent event)
    {
        event.registerServerCommand(new CommandTPLatLong());
    }

    public void scheduleTask(MessageContext ctx, Runnable runnable)
    {
        WorldServer worldObj = (WorldServer) ctx.getServerHandler().playerEntity.worldObj;
        worldObj.addScheduledTask(runnable);
    }
}
