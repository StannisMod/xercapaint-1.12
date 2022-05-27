package xerca.xercapaint.common.packets;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class CloseGuiPacket implements IMessage {
    @Override
    public void fromBytes(final ByteBuf buf) {

    }

    @Override
    public void toBytes(final ByteBuf buf) {

    }

    public static final class Handler implements IMessageHandler<CloseGuiPacket, IMessage> {

        @Override
        public IMessage onMessage(final CloseGuiPacket message, final MessageContext ctx) {
            Minecraft.getMinecraft().addScheduledTask(() -> Minecraft.getMinecraft().displayGuiScreen(null));
            return null;
        }
    }
}
