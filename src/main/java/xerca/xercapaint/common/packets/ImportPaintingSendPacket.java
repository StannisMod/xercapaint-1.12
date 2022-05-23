package xerca.xercapaint.common.packets;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class ImportPaintingSendPacket implements IMessage {

    private NBTTagCompound tag;

    public ImportPaintingSendPacket() {}
    public ImportPaintingSendPacket(NBTTagCompound tag) {
        this.tag = tag;
    }

    public NBTTagCompound getTag() {
        return tag;
    }

    @Override
    public void fromBytes(final ByteBuf buf) {
        try {
            tag = ByteBufUtils.readTag(buf);
        } catch (IndexOutOfBoundsException ioe) {
            System.err.println("Exception while reading ImportPaintingSendPacket: " + ioe);
        }
    }

    @Override
    public void toBytes(final ByteBuf buf) {
        ByteBufUtils.writeTag(buf, tag);
    }
}
