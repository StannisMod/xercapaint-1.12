package xerca.xercapaint.common.packets;

import net.minecraft.client.Minecraft;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import xerca.xercapaint.common.command.CommandExport;

public class ExportPaintingPacketHandler implements IMessageHandler<ExportPaintingPacket, IMessage> {

    @Override
    public IMessage onMessage(final ExportPaintingPacket msg, final MessageContext ctx) {
        Minecraft.getMinecraft().addScheduledTask(() -> {
            if (CommandExport.doExport(Minecraft.getMinecraft().player, msg.getName())){
                Minecraft.getMinecraft().player.sendMessage(new TextComponentTranslation("export.success", msg.getName()).setStyle(new Style().setColor(TextFormatting.GREEN)));
            } else {
                Minecraft.getMinecraft().player.sendMessage(new TextComponentTranslation("export.fail", msg.getName()).setStyle(new Style().setColor(TextFormatting.RED)));
            }
        });
        return null;
    }
}
