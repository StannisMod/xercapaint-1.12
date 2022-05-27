package xerca.xercapaint.common.packets;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import xerca.xercapaint.common.XercaPaint;
import xerca.xercapaint.common.entity.EntityEasel;

public class EaselLeftPacket implements IMessage {

    private int easelId;

    public EaselLeftPacket() {
    }

    public EaselLeftPacket(EntityEasel easel) {
        easelId = easel.getEntityId();
    }

    @Override
    public void fromBytes(final ByteBuf buf) {
        easelId = buf.readInt();
    }

    @Override
    public void toBytes(final ByteBuf buf) {
        buf.writeInt(easelId);
    }

    public int getEaselId() {
        return easelId;
    }

    public static final class Handler implements IMessageHandler<EaselLeftPacket, IMessage> {

        @Override
        public IMessage onMessage(final EaselLeftPacket msg, final MessageContext ctx) {
            FMLCommonHandler.instance().getMinecraftServerInstance().addScheduledTask(() -> {
                if (msg.getEaselId() > -1) {
                    Entity entityEasel = ctx.getServerHandler().player.world.getEntityByID(msg.getEaselId());
                    if (entityEasel == null) {
                        XercaPaint.LOGGER.error("EaselLeftPacket: Easel entity not found! easelId: " + msg.getEaselId());
                        return;
                    }
                    if (!(entityEasel instanceof EntityEasel)) {
                        XercaPaint.LOGGER.error("EaselLeftPacket: Entity found is not an easel! easelId: " + msg.getEaselId());
                        return;
                    }
                    EntityEasel easel = (EntityEasel) entityEasel;
                    easel.setPainter(null);
                }
            });
            return null;
        }
    }
}
