package net.gegy1000.earth.server.network;

import io.netty.buffer.ByteBuf;
import net.gegy1000.earth.Earth;
import net.gegy1000.earth.client.gui.GuiSelectSpawn;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * If received on server, spawn player at location, if received on client, open selection gui
 */
public class MessageSpawnpoint implements IMessage
{
    private int x, z;

    public MessageSpawnpoint()
    {
    }

    public MessageSpawnpoint(int x, int z)
    {
        this.x = x;
        this.z = z;
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        this.x = buf.readInt();
        this.z = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        buf.writeInt(x);
        buf.writeInt(z);
    }

    public static class Handler implements IMessageHandler<MessageSpawnpoint, IMessage>
    {
        @Override
        public IMessage onMessage(final MessageSpawnpoint message, final MessageContext ctx)
        {
            Earth.proxy.scheduleTask(ctx, new Runnable()
            {
                @Override
                public void run()
                {
                    if (ctx.side.isClient())
                    {
                        openGUI();
                    }
                    else
                    {
                        EntityPlayerMP player = ctx.getServerHandler().playerEntity;

                        BlockPos pos = player.worldObj.getTopSolidOrLiquidBlock(new BlockPos(message.x, 0, message.z));
                        player.setPosition(pos.getX(), pos.getY(), pos.getZ());
                    }
                }
            });

            return null;
        }
    }

    public static void openGUI()
    {
        Minecraft.getMinecraft().displayGuiScreen(new GuiSelectSpawn());
    }
}
