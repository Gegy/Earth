package net.gegy1000.earth.server.event;

import net.gegy1000.earth.server.world.gen.EarthGenerator;
import net.gegy1000.earth.server.world.gen.WorldTypeEarth;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ServerEventHandler {
    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event) {
        World world = event.getWorld();
        if (world.getWorldType() instanceof WorldTypeEarth) {
            EarthGenerator generator = WorldTypeEarth.getGenerator(world);
            double x = generator.fromLongitude(-0.127758);
            double z = generator.fromLatitude(51.507348);
            world.setSpawnPoint(new BlockPos(x, 0, z));
        }
    }
}
