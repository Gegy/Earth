package net.gegy1000.earth.server.event;

import net.gegy1000.earth.server.world.gen.WorldTypeEarth;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ServerEventHandler
{
    //TODO select spawn

//    private List<EntityPlayer> selectingSpawn = new ArrayList<EntityPlayer>();

//    @SubscribeEvent
//    public void entityConstruct(EntityEvent.EntityConstructing event)
//    {
//        PlayerData.register(event.entity);
//    }

    @SubscribeEvent
    public void worldLoad(WorldEvent.Load event)
    {
        World world = event.world;

        if (world.getWorldType() instanceof WorldTypeEarth)
        {
            world.setSpawnPoint(new BlockPos(30000, 0, 0));
        }
    }

//    @SubscribeEvent
//    public void joinWorld(EntityJoinWorldEvent event)
//    {
//        Entity entity = event.entity;
//
//        if (entity instanceof EntityPlayer)
//        {
//            EntityPlayer player = (EntityPlayer) entity;
//
//            if (!player.worldObj.isRemote && !PlayerData.get(player).hasSpawned())
//            {
//                selectingSpawn.add(player);
//            }
//        }
//    }
//
//    @SubscribeEvent
//    public void playerTickEvent(TickEvent.PlayerTickEvent event)
//    {
//        EntityPlayer player = event.player;
//
//        if (!player.worldObj.isRemote && selectingSpawn.contains(player))
//        {
//            EarthNetworkManager.networkWrapper.sendTo(new MessageSpawnpoint(), (EntityPlayerMP) player);
//
//            selectingSpawn.remove(player);
//        }
//    }

//    @SubscribeEvent
//    public void onDeath(LivingDeathEvent event)
//    {
//        Entity entity = event.entity;
//
//        if (!entity.worldObj.isRemote)
//        {
//            if (entity instanceof EntityPlayer)
//            {
//                PlayerData.get((EntityPlayer) entity).setSpawned(false);
//            }
//        }
//    }
}
