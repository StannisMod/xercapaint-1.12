package xerca.xercapaint.common.packets;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import xerca.xercapaint.common.XercaPaint;

import java.io.File;
import java.io.IOException;

public class ImportPaintingPacketHandler implements IMessageHandler<ImportPaintingPacket, IMessage> {

    @Override
    public IMessage onMessage(final ImportPaintingPacket msg, final MessageContext ctx) {
        Minecraft.getMinecraft().addScheduledTask(() -> {
            String filename = msg.getName() + ".paint";
            String filepath = "paintings/" + filename;
            try {
                NBTTagCompound tag = CompressedStreamTools.read(new File(filepath));

                ImportPaintingSendPacket pack = new ImportPaintingSendPacket(tag);
                XercaPaint.network.sendToServer(pack);
            } catch (IOException e) {
                e.printStackTrace();
                Minecraft.getMinecraft().player.sendMessage(new TextComponentTranslation("import.fail.4", filepath).setStyle(new Style().setColor(TextFormatting.RED)));
            }
        });
        return null;
    }
}
