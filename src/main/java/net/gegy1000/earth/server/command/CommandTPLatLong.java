package net.gegy1000.earth.server.command;

import net.gegy1000.earth.Earth;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ChatComponentText;

public class CommandTPLatLong extends CommandBase
{
    @Override
    public String getCommandName()
    {
        return "tplatlong";
    }

    @Override
    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    @Override
    public String getCommandUsage(ICommandSender sender)
    {
        return "tplatlong <latitude> <longitude>";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException
    {
        if (args.length >= 2)
        {
            try
            {
                double latitude = Double.parseDouble(args[0]);
                double longitude = Double.parseDouble(args[1]);

                double x = Earth.generator.fromLong(longitude);
                double z = Earth.generator.fromLat(latitude);
                int y = Earth.generator.getHeightForCoords((int) x, (int) z) + 1;

                if (sender instanceof EntityPlayerMP)
                {
                    ((EntityPlayerMP) sender).playerNetServerHandler.setPlayerLocation(x, y, z, 0, 0);

                    sender.addChatMessage(new ChatComponentText("Teleporting to " + x + " " + y + " " + z + ", or " + latitude + " " + longitude + "."));
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
                throw new WrongUsageException("Invalid Longitude and/or Latitude!");
            }
        }
        else
        {
            throw new WrongUsageException("Please specify a latitude and longitude!");
        }
    }
}