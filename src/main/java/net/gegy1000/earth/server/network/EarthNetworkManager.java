package net.gegy1000.earth.server.network;

import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class EarthNetworkManager
{
    public static SimpleNetworkWrapper networkWrapper;

    public static void init()
    {
        networkWrapper = NetworkRegistry.INSTANCE.newSimpleChannel("earthmod");

        networkWrapper.registerMessage(MessageSpawnpoint.Handler.class, MessageSpawnpoint.class, 0, Side.CLIENT);
        networkWrapper.registerMessage(MessageSpawnpoint.Handler.class, MessageSpawnpoint.class, 1, Side.SERVER);
    }
}
