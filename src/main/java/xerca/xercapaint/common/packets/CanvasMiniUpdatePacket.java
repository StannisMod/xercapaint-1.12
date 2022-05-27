package xerca.xercapaint.common.packets;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import xerca.xercapaint.common.CanvasType;
import xerca.xercapaint.common.XercaPaint;
import xerca.xercapaint.common.entity.EntityEasel;
import xerca.xercapaint.common.item.ItemCanvas;
import xerca.xercapaint.common.item.ItemPalette;

import java.util.Arrays;

public class CanvasMiniUpdatePacket implements IMessage {
    private int[] pixels;
    private CanvasType canvasType;
    private String name; //name must be unique
    private int version;
    private int easelId;

    public CanvasMiniUpdatePacket() {}

    public CanvasMiniUpdatePacket(int[] pixels, String name, int version, EntityEasel easel, CanvasType canvasType) {
        this.name = name;
        this.version = version;
        this.canvasType = canvasType;
        int area = CanvasType.getHeight(canvasType) * CanvasType.getWidth(canvasType);
        this.pixels = Arrays.copyOfRange(pixels, 0, area);
        if (easel == null) {
            easelId = -1;
        } else {
            easelId = easel.getEntityId();
        }
    }

    @Override
    public void fromBytes(final ByteBuf buf) {
        easelId = buf.readInt();
        canvasType = CanvasType.values()[buf.readInt()];
        version = buf.readInt();

        pixels = new int[buf.readInt()];
        for (int i = 0; i < pixels.length; i++) {
            pixels[i] = buf.readInt();
        }
    }

    @Override
    public void toBytes(final ByteBuf buf) {
        buf.writeInt(easelId);
        buf.writeByte(canvasType.ordinal());
        buf.writeInt(version);
        ByteBufUtils.writeUTF8String(buf, name);

        buf.writeInt(pixels.length);
        for (int i = 0; i < pixels.length; i++) {
            buf.writeInt(pixels[i]);
        }
    }

    public int[] getPixels() {
        return pixels;
    }

    public String getName() {
        return name;
    }

    public int getVersion() {
        return version;
    }

    public int getEaselId() {
        return easelId;
    }

    public CanvasType getCanvasType() {
        return canvasType;
    }

    public static final class Handler implements IMessageHandler<CanvasMiniUpdatePacket, IMessage> {

        @Override
        public IMessage onMessage(final CanvasMiniUpdatePacket msg, final MessageContext ctx) {
            FMLCommonHandler.instance().getMinecraftServerInstance().addScheduledTask(() -> {
                ItemStack canvas;
                ItemStack palette;
                Entity entityEasel = null;

                EntityPlayer pl = ctx.getServerHandler().player;

                if (msg.getEaselId() > -1) {
                    entityEasel = pl.world.getEntityByID(msg.getEaselId());
                    if (entityEasel == null) {
                        XercaPaint.LOGGER.error("CanvasMiniUpdatePacket: Easel entity not found! easelId: " + msg.getEaselId());
                        return;
                    }
                    if (!(entityEasel instanceof EntityEasel)) {
                        XercaPaint.LOGGER.error("CanvasMiniUpdatePacket: Entity found is not an easel! easelId: " + msg.getEaselId());
                        return;
                    }
                    EntityEasel easel = (EntityEasel) entityEasel;
                    canvas = easel.getItem();
                    if (!(canvas.getItem() instanceof ItemCanvas)) {
                        XercaPaint.LOGGER.error("CanvasMiniUpdatePacket: Canvas not found inside easel!");
                        return;
                    }
                } else {
                    canvas = pl.getHeldItemMainhand();
                    palette = pl.getHeldItemOffhand();
                    if (canvas.getItem() instanceof ItemPalette) {
                        ItemStack temp = canvas;
                        canvas = palette;
                        palette = temp;
                    }
                }

                if (!canvas.isEmpty() && canvas.getItem() instanceof ItemCanvas) {
                    NBTTagCompound comp = canvas.getTagCompound();
                    if (comp == null) {
                        comp = new NBTTagCompound();
                        canvas.setTagCompound(comp);
                    }

                    comp.setIntArray("pixels", msg.getPixels());
                    comp.setString("name", msg.getName());
                    comp.setInteger("v", msg.getVersion());
                    comp.setInteger("generation", 0);

                    if (entityEasel instanceof EntityEasel) {
                        EntityEasel easel = (EntityEasel) entityEasel;
                        easel.setItem(canvas, false);
                    }

                    XercaPaint.LOGGER.debug("Handling canvas update: Name: " + msg.getName() + " V: " + msg.getVersion());
                }
            });
            return null;
        }
    }
}
