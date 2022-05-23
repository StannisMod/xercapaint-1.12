package xerca.xercapaint.common.command;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.*;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import xerca.xercapaint.common.CanvasType;
import xerca.xercapaint.common.XercaPaint;
import xerca.xercapaint.common.item.ItemCanvas;
import xerca.xercapaint.common.item.ItemPalette;
import xerca.xercapaint.common.item.Items;
import xerca.xercapaint.common.packets.ImportPaintingPacket;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CommandImport extends CommandBase {
//    public static void register(CommandDispatcher<CommandSource> dispatcher) {
//        dispatcher.register(
//                Commands.literal("paintimport")
//                        .requires((p) -> p.hasPermission(1))
//                        .then(Commands.argument("name", StringArgumentType.word())
//                                .executes((p) -> paintImport(p.getSource(), StringArgumentType.getString(p, "name"))))
//        );
//    }

    private static int paintImport(ICommandSender sender, String name) {
        XercaPaint.LOGGER.debug("Paint import called. name: " + name);
        ImportPaintingPacket pack = new ImportPaintingPacket(name);
        try {
            EntityPlayerMP player = getCommandSenderAsPlayer(sender);
            XercaPaint.network.sendTo(pack, player);
        } catch (PlayerNotFoundException e) {
            XercaPaint.LOGGER.debug("Command executor is not a player");
            e.printStackTrace();
            return 0;
        }

        return 1;
    }

    @SuppressWarnings("ConstantConditions")
    public static void doImport(NBTTagCompound tag, EntityPlayerMP player) {
        byte canvasType = tag.getByte("ct");
        tag.removeTag("ct");
        if (tag.getInteger("generation") > 0) {
            tag.setInteger("generation", tag.getInteger("generation") + 1);
        }

        if (player.isCreative()) {
            ItemStack itemStack;
            switch (CanvasType.fromByte(canvasType)) {
                case SMALL:
                    itemStack = new ItemStack(Items.ITEM_CANVAS);
                    break;
                case LONG:
                    itemStack = new ItemStack(Items.ITEM_CANVAS_LONG);
                    break;
                case TALL:
                    itemStack = new ItemStack(Items.ITEM_CANVAS_TALL);
                    break;
                case LARGE:
                    itemStack = new ItemStack(Items.ITEM_CANVAS_LARGE);
                    break;
                default:
                    XercaPaint.LOGGER.error("Invalid canvas type");
                    return;
            }
            itemStack.setTagCompound(tag);
            player.addItemStackToInventory(itemStack);
        } else {
            ItemStack mainhand = player.getHeldItemMainhand();
            ItemStack offhand = player.getHeldItemOffhand();

            if (!(mainhand.getItem() instanceof ItemCanvas) || (mainhand.hasTagCompound() && !mainhand.getTagCompound().hasNoTags())) {
                player.sendMessage(new TextComponentTranslation("import.fail.1").setStyle(new Style().setColor(TextFormatting.RED)));
                return;
            }
            if (((ItemCanvas)mainhand.getItem()).getCanvasType() != CanvasType.fromByte(canvasType)) {
                String type = Items.ITEM_CANVAS.getItemStackDisplayName(ItemStack.EMPTY);
                switch (CanvasType.fromByte(canvasType)){
                    case LONG:
                        type = Items.ITEM_CANVAS_LONG.getItemStackDisplayName(ItemStack.EMPTY);
                        break;
                    case TALL:
                        type = Items.ITEM_CANVAS_TALL.getItemStackDisplayName(ItemStack.EMPTY);
                        break;
                    case LARGE:
                        type = Items.ITEM_CANVAS_LARGE.getItemStackDisplayName(ItemStack.EMPTY);
                        break;
                }
                player.sendMessage(new TextComponentTranslation("import.fail.2", new TextComponentString(type))
                        .setStyle(new Style().setColor(TextFormatting.RED)));
                return;
            }
            if (!ItemPalette.isFull(offhand)) {
                player.sendMessage(new TextComponentTranslation("import.fail.3").setStyle(new Style().setColor(TextFormatting.RED)));
                return;
            }
            mainhand.setTagCompound(tag);
        }
        player.sendMessage(new TextComponentTranslation("import.success").setStyle(new Style().setColor(TextFormatting.GREEN)));
    }

    @Override
    public String getName() {
        return "paintimport";
    }

    @Override
    public String getUsage(final ICommandSender sender) {
        return "/paintimport <name>";
    }

    @Override
    public void execute(final MinecraftServer server, final ICommandSender sender, final String[] args) throws CommandException {
        if (args.length < 1) {
            throw new WrongUsageException(getUsage(sender));
        }
        paintImport(sender, args[0]);
    }
}
