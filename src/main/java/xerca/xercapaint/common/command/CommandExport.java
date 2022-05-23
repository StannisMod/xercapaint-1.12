package xerca.xercapaint.common.command;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import xerca.xercapaint.common.XercaPaint;
import xerca.xercapaint.common.item.ItemCanvas;
import xerca.xercapaint.common.packets.ExportPaintingPacket;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.io.File;
import java.io.IOException;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CommandExport extends CommandBase {
//    public static void register(CommandDispatcher<CommandSource> dispatcher) {
//        dispatcher.register(
//                Commands.literal("paintexport")
//                        .requires((p) -> p.hasPermission(1))
//                        .then(Commands.argument("name", StringArgumentType.word())
//                                .executes((p) -> paintExport(p.getSource(), StringArgumentType.getString(p, "name"))))
//        );
//    }

    @Override
    public String getName() {
        return "paintexport";
    }

    @Override
    public String getUsage(final ICommandSender sender) {
        return "/paintexport <name>";
    }

    private static int paintExport(@Nullable ICommandSender sender, String name) throws PlayerNotFoundException {
        XercaPaint.LOGGER.debug("Paint export called. name: " + name);
        if (sender == null){
            XercaPaint.LOGGER.error("Command entity is not found");
            return 0;
        }
        if (!(sender instanceof EntityPlayer)) {
            XercaPaint.LOGGER.error("Command entity is not a player");
            return 0;
        }

        ExportPaintingPacket pack = new ExportPaintingPacket(name);
        XercaPaint.network.sendTo(pack, getCommandSenderAsPlayer(sender));
        return 1;
    }

    public static boolean doExport(EntityPlayer player, String name){
        String dir = "paintings";
        String filename = name + ".paint";
        String filepath = dir + "/" + filename;
        File directory = new File(dir);
        if (!directory.exists()){
            directory.mkdir();
        }

        for (ItemStack s : new ItemStack[] { player.getHeldItemMainhand(), player.getHeldItemOffhand() }) {
            if (s.getItem() instanceof ItemCanvas) {
                if (s.hasTagCompound()) {
                    try {
                        NBTTagCompound tag = s.getTagCompound().copy();
                        tag.setByte("ct", (byte) ((ItemCanvas) s.getItem()).getCanvasType().ordinal());
                        CompressedStreamTools.write(tag, new File(filepath));
                        return true;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return false;
    }

    @Override
    public void execute(final MinecraftServer server, final ICommandSender sender, final String[] args) throws CommandException {
        if (args.length < 1) {
            throw new WrongUsageException(getUsage(sender));
        }
        paintExport(sender, args[0]);
    }
}
