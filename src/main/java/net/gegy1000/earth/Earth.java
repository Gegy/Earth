package net.gegy1000.earth;

import net.gegy1000.earth.server.proxy.ServerProxy;
import net.gegy1000.earth.server.util.osm.MapMaterial;
import net.gegy1000.earth.server.world.gen.EarthGenerator;
import net.gegy1000.earth.server.world.gen.OSMGenerator;
import net.gegy1000.earth.server.world.gen.WorldTypeEarth;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = Earth.MODID, name = "Earth", version = Earth.VERSION)
public class Earth {
    public static final EarthGenerator GENERATOR_145 = new EarthGenerator();
    public static final EarthGenerator OSM_11 = new OSMGenerator(1);
    public static final EarthGenerator OSM_12 = new OSMGenerator(2);

    @SidedProxy(clientSide = "net.gegy1000.earth.client.proxy.ClientProxy", serverSide = "net.gegy1000.earth.server.proxy.ServerProxy")
    public static ServerProxy proxy;

    public static final String MODID = "earth";
    public static final String VERSION = "1.1.0";

    public static final Logger LOGGER = LogManager.getLogger("Earth Mod");

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        new WorldTypeEarth("earth_145", GENERATOR_145, true, false, 170.0F);
        new WorldTypeEarth("osm_11", OSM_11, false, true, 260.0F);
        new WorldTypeEarth("osm_12", OSM_12, false, true, 130.0F);

        proxy.preInit();

        MapMaterial.init();
    }

    @Mod.EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        proxy.serverStart(event);
    }
}
