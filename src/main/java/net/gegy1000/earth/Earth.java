package net.gegy1000.earth;

import net.gegy1000.earth.server.proxy.ServerProxy;
import net.gegy1000.earth.server.world.gen.EarthGen;
import net.gegy1000.earth.server.world.gen.WorldTypeEarth;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ProgressManager;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

@Mod(modid = Earth.MODID, name = "Earth", version = Earth.VERSION)
public class Earth {
    public static final EarthGen GENERATOR = new EarthGen();

    @SidedProxy(clientSide = "net.gegy1000.earth.client.proxy.ClientProxy", serverSide = "net.gegy1000.earth.server.proxy.ServerProxy")
    public static ServerProxy proxy;

    public static final String MODID = "earth";
    public static final String VERSION = "1.1.0";

    public static final Logger LOGGER = LogManager.getLogger("Earth Mod");

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        ProgressManager.ProgressBar bar = ProgressManager.push("Loading Earth Maps", 2);
        try {
            GENERATOR.load(bar);
        } catch (IOException e) {
        }
        ProgressManager.pop(bar);

        new WorldTypeEarth(GENERATOR);

        proxy.preInit();
    }

    @Mod.EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        proxy.serverStart(event);
    }
}
