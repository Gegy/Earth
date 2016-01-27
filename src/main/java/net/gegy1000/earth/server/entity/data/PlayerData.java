package net.gegy1000.earth.server.entity.data;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.IExtendedEntityProperties;

public class PlayerData implements IExtendedEntityProperties
{
    private static final String IDENTIFIER = "EARTHMODPLAYERDATA";

    private boolean spawned;

    public static PlayerData get(EntityPlayer player)
    {
        return (PlayerData) player.getExtendedProperties(IDENTIFIER);
    }

    public static void register(Entity entity)
    {
        if (!entity.worldObj.isRemote)
        {
            if (entity instanceof EntityPlayer)
            {
                entity.registerExtendedProperties(IDENTIFIER, new PlayerData());
            }
        }
    }

    @Override
    public void saveNBTData(NBTTagCompound compound)
    {
        compound.setBoolean("Spawned", spawned);
    }

    @Override
    public void loadNBTData(NBTTagCompound compound)
    {
        this.spawned = compound.getBoolean("Spawned");
    }

    @Override
    public void init(Entity entity, World world)
    {
    }

    public void setSpawned(boolean spawned)
    {
        this.spawned = spawned;
    }

    public boolean hasSpawned()
    {
        return spawned;
    }
}
