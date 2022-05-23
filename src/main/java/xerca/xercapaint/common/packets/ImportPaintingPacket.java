package xerca.xercapaint.common.packets;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class ImportPaintingPacket implements IMessage {

    private String name;

    public ImportPaintingPacket() {}

    public ImportPaintingPacket(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public void fromBytes(final ByteBuf buf) {
        try {
            name = ByteBufUtils.readUTF8String(buf);
        } catch (IndexOutOfBoundsException ioe) {
            System.err.println("Exception while reading ImportPaintingPacket: " + ioe);
        }
    }

    @Override
    public void toBytes(final ByteBuf buf) {
        ByteBufUtils.writeUTF8String(buf, name);
    }
}
