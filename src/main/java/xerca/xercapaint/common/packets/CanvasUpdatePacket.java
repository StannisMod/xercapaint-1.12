package xerca.xercapaint.common.packets;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import xerca.xercapaint.common.CanvasType;
import xerca.xercapaint.common.PaletteUtil;
import xerca.xercapaint.common.entity.EntityEasel;

import java.util.Arrays;

public class CanvasUpdatePacket implements IMessage {
    private PaletteUtil.CustomColor[] paletteColors;
    private int[] pixels;
    private boolean signed;
    private String title;
    private CanvasType canvasType;
    private String name; //name must be unique
    private int version;
    private int easelId;
    private boolean messageIsValid;

    public CanvasUpdatePacket(int[] pixels, boolean signed, String title, String name, int version, EntityEasel easel, PaletteUtil.CustomColor[] paletteColors, CanvasType canvasType) {
        this.paletteColors = Arrays.copyOfRange(paletteColors, 0, 12);
        this.signed = signed;
        this.title = title;
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

    public CanvasUpdatePacket() {
        this.messageIsValid = false;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        for (PaletteUtil.CustomColor color : paletteColors) {
            color.writeToBuffer(buf);
        }
        buf.writeInt(easelId);
        buf.writeByte(canvasType.ordinal());
        buf.writeInt(version);
        ByteBufUtils.writeUTF8String(buf, name);
        ByteBufUtils.writeUTF8String(buf, title);
        buf.writeBoolean(signed);
//        buf.writeVarIntArray(pixels);
        for (int p : pixels) {
            buf.writeInt(p);
        }
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        try {
            paletteColors = new PaletteUtil.CustomColor[12];
            for (int i = 0; i < paletteColors.length; i++) {
                paletteColors[i] = new PaletteUtil.CustomColor(buf);
            }
            easelId = buf.readInt();
            canvasType = CanvasType.fromByte(buf.readByte());
            version = buf.readInt();
            name = ByteBufUtils.readUTF8String(buf);
            title = ByteBufUtils.readUTF8String(buf);
            signed = buf.readBoolean();
            int area = CanvasType.getHeight(canvasType) * CanvasType.getWidth(canvasType);
//            pixels = buf.readVarIntArray(area);
            pixels = new int[area];
            for (int i = 0; i < area; i++) {
                pixels[i] = buf.readInt();
            }
        } catch (IndexOutOfBoundsException ioe) {
            System.err.println("Exception while reading CanvasUpdatePacket: " + ioe);
            return;
        }
        messageIsValid = true;
    }

    public int[] getPixels() {
        return pixels;
    }

    public PaletteUtil.CustomColor[] getPaletteColors() {
        return paletteColors;
    }

    public boolean getSigned() {
        return signed;
    }

    public String getTitle() {
        return title;
    }

    public String getName() {
        return name;
    }

    public int getEaselId() {
        return easelId;
    }

    public boolean isMessageValid() {
        return messageIsValid;
    }

    public int getVersion() {
        return version;
    }

    public CanvasType getCanvasType() {
        return canvasType;
    }
}
