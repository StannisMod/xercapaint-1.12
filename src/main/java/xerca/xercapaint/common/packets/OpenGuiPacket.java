package xerca.xercapaint.common.packets;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import xerca.xercapaint.client.ClientStuff;
import xerca.xercapaint.common.XercaPaint;
import xerca.xercapaint.common.entity.EntityEasel;
import xerca.xercapaint.common.item.ItemPalette;

public class OpenGuiPacket implements IMessage {

    private int easelId;
    private boolean allowed;
    private boolean edit;
    private EnumHand hand;

    public OpenGuiPacket(final int easelId, final boolean allowed, final boolean edit, final EnumHand hand) {
        this.easelId = easelId;
        this.allowed = allowed;
        this.edit = edit;
        this.hand = hand;
    }

    @Override
    public void fromBytes(final ByteBuf buf) {
        easelId = buf.readInt();
        allowed = buf.readBoolean();
        edit = buf.readBoolean();
        hand = EnumHand.values()[buf.readInt()];
    }

    @Override
    public void toBytes(final ByteBuf buf) {
        buf.writeInt(easelId);
        buf.writeBoolean(allowed);
        buf.writeBoolean(edit);
        buf.writeInt(hand.ordinal());
    }

    public int getEaselId() {
        return easelId;
    }

    public boolean isAllowed() {
        return allowed;
    }

    public boolean isEdit() {
        return edit;
    }

    public EnumHand getHand() {
        return hand;
    }

    public static final class Handler implements IMessageHandler<OpenGuiPacket, IMessage> {

        @SideOnly(Side.CLIENT)
        @Override
        public IMessage onMessage(final OpenGuiPacket msg, final MessageContext ctx) {
            Minecraft.getMinecraft().addScheduledTask(() -> {
                EntityPlayer player = Minecraft.getMinecraft().player;
                if (msg.isAllowed()) {
                    Entity entity = player.world.getEntityByID(msg.getEaselId());
                    if (entity instanceof EntityEasel) {
                        EntityEasel easel = (EntityEasel) entity;
                        ItemStack itemInHand = player.getHeldItem(msg.getHand());
                        boolean handHoldsPalette = itemInHand.getItem() instanceof ItemPalette;
                        if (msg.isEdit()) {
                            if (handHoldsPalette) {
                                ClientStuff.showCanvasGui(easel, itemInHand);
                            } else {
                                XercaPaint.LOGGER.error("Could not find palette in hand for editing painting");
                            }
                        } else {
                            ClientStuff.showCanvasGui(easel, ItemStack.EMPTY);
                        }
                    } else {
                        XercaPaint.LOGGER.error("Could not find easel");
                    }
                } else {
                    player.sendMessage(new TextComponentTranslation("easel.deny").setStyle(new Style().setColor(TextFormatting.RED)));
                }
            });
            return null;
        }
    }
}
