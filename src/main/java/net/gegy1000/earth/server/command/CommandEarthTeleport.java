package net.gegy1000.earth.server.command;

import net.gegy1000.earth.Earth;
import net.gegy1000.earth.server.world.gen.EarthGenerator;
import net.gegy1000.earth.server.world.gen.WorldTypeEarth;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;

public class CommandEarthTeleport extends CommandBase {
    @Override
    public String getCommandName() {
        return "tplatlong";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "tplatlong <latitude> <longitude>";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length >= 2) {
            try {
                double latitude = Double.parseDouble(args[0]);
                double longitude = Double.parseDouble(args[1]);
                EarthGenerator generator = WorldTypeEarth.getGenerator(server.getEntityWorld());
                double x = generator.fromLong(longitude);
                double z = generator.fromLat(latitude);
                int y = generator.getHeightForCoords((int) x, (int) z) + 1;
                if (sender instanceof EntityPlayerMP) {
                    ((EntityPlayerMP) sender).connection.setPlayerLocation(x, y, z, 0, 0);
                    sender.addChatMessage(new TextComponentString("Teleporting to " + x + " " + y + " " + z + ", or " + latitude + " " + longitude + "."));
                }
            } catch (Exception e) {
                e.printStackTrace();
                throw new WrongUsageException("Invalid Longitude and/or Latitude!");
            }
        } else {
            throw new WrongUsageException("Please specify a latitude and longitude!");
        }
    }
}