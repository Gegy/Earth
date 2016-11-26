package net.gegy1000.earth;

import net.gegy1000.earth.server.proxy.ServerProxy;
import net.gegy1000.earth.server.world.gen.EarthGenerator;
import net.gegy1000.earth.server.world.gen.WorldTypeEarth;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = Earth.MODID, name = "Earth", version = Earth.VERSION, dependencies = "required-after:llibrary@[" + Earth.LLIBRARY_VERSION + ",)")
public class Earth {
    public static final EarthGenerator GENERATOR = new EarthGenerator();

    @SidedProxy(clientSide = "net.gegy1000.earth.client.proxy.ClientProxy", serverSide = "net.gegy1000.earth.server.proxy.ServerProxy")
    public static ServerProxy proxy;

    public static final String MODID = "earth";
    public static final String VERSION = "1.1.0";
    public static final String LLIBRARY_VERSION = "1.7.1";

    public static final Logger LOGGER = LogManager.getLogger("Earth Mod");

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        GENERATOR.load();

        new WorldTypeEarth("earth", GENERATOR);

        proxy.preInit();
    }

    @Mod.EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        proxy.serverStart(event);
    }
}
