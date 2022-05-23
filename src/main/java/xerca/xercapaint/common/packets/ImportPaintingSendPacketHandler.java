package xerca.xercapaint.common.packets;

import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import xerca.xercapaint.common.command.CommandImport;

public class ImportPaintingSendPacketHandler implements IMessageHandler<ImportPaintingSendPacket, IMessage> {

    @Override
    public IMessage onMessage(final ImportPaintingSendPacket msg, final MessageContext ctx) {
        FMLCommonHandler.instance().getMinecraftServerInstance().addScheduledTask(() -> {
            CommandImport.doImport(msg.getTag(), ctx.getServerHandler().player);
        });
        return null;
    }
}
