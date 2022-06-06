package xerca.xercapaint.common.packets;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IThreadListener;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import xerca.xercapaint.common.PaletteUtil;
import xerca.xercapaint.common.item.Items;


public class PaletteUpdatePacketHandler implements IMessageHandler<PaletteUpdatePacket, IMessage> {

    @SideOnly(Side.CLIENT)
    @Override
    public IMessage onMessage(PaletteUpdatePacket message, MessageContext ctx) {

        if (!message.isMessageValid()) {
            System.err.println("Packet was invalid");
            return null;
        }
        EntityPlayerMP sendingPlayer = ctx.getServerHandler().player;
        if (sendingPlayer == null) {
            System.err.println("EntityPlayerMP was null when PaletteUpdatePacket was received");
            return null;
        }

        final IThreadListener mainThread = (WorldServer) ctx.getServerHandler().player.world;
        mainThread.addScheduledTask(() -> processMessage(message, sendingPlayer));

        return null;
    }

    private static void processMessage(PaletteUpdatePacket msg, EntityPlayerMP pl) {
        ItemStack palette = pl.getHeldItemMainhand();

        if (!palette.isEmpty() && palette.getItem() == Items.ITEM_PALETTE) {
            NBTTagCompound paletteComp = palette.getTagCompound();
            if(paletteComp == null){
                paletteComp = new NBTTagCompound();
                palette.setTagCompound(paletteComp);
            }
            PaletteUtil.writeCustomColorArrayToNBT(paletteComp, msg.getPaletteColors());
        }
    }
}
