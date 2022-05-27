package xerca.xercapaint.client;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import xerca.xercapaint.common.entity.EntityCanvas;
import xerca.xercapaint.common.entity.EntityEasel;
import xerca.xercapaint.common.item.ItemCanvas;
import xerca.xercapaint.common.item.ItemPalette;

public class ClientStuff {
//    public static ModelLayerLocation EASEL_MAIN_LAYER = new ModelLayerLocation(new ResourceLocation(XercaPaint.MODID, "easel"), "main");
//    public static ModelLayerLocation EASEL_CANVAS_LAYER = new ModelLayerLocation(new ResourceLocation(XercaPaint.MODID, "easel"), "canvas");

    public static void init() {
        RenderingRegistry.registerEntityRenderingHandler(EntityCanvas.class, new RenderEntityCanvas.RenderEntityCanvasFactory());
        RenderingRegistry.registerEntityRenderingHandler(EntityEasel.class, new RenderEntityEasel.RenderEntityCanvasFactory());
    }

    public static void showCanvasGui(EntityEasel easel, ItemStack palette) {
        showCanvasGui(easel, palette, Minecraft.getMinecraft());
    }

    public static void showCanvasGui(EntityEasel easel, ItemStack palette, Minecraft minecraft) {
        ItemStack canvas = easel.getItem();
        NBTTagCompound tag = canvas.getTagCompound();
        if ((tag != null && tag.getInteger("generation") > 0) || palette.isEmpty()) {
            minecraft.displayGuiScreen(new GuiCanvasView(canvas.getTagCompound(),
                    new TextComponentTranslation("item.xercapaint.item_canvas"),
                    ((ItemCanvas) canvas.getItem()).getCanvasType(), easel));
        } else {
            minecraft.displayGuiScreen(new GuiCanvasEdit(minecraft.player, canvas.getTagCompound(), palette.getTagCompound(),
                    new TextComponentTranslation("item.xercapaint.item_canvas"),
                    ((ItemCanvas) canvas.getItem()).getCanvasType(), easel));
        }
    }

    public static void showCanvasGui(EntityPlayer player) {
        final ItemStack heldItem = player.getHeldItemMainhand();
        final ItemStack offhandItem = player.getHeldItemOffhand();
        final Minecraft minecraft = Minecraft.getMinecraft();

        if (heldItem.isEmpty() || (minecraft.player != null && !minecraft.player.getGameProfile().getId().equals(player.getGameProfile().getId()))) {
            return;
        }

        if (heldItem.getItem() instanceof ItemCanvas) {
            NBTTagCompound tag = heldItem.getTagCompound();
            if (offhandItem.isEmpty() || !(offhandItem.getItem() instanceof ItemPalette) || (tag != null && tag.getInteger("generation") > 0)) {
                minecraft.displayGuiScreen(new GuiCanvasView(heldItem.getTagCompound(), new TextComponentTranslation("item.xercapaint.item_canvas"), ((ItemCanvas) heldItem.getItem()).getCanvasType(), null));
            } else {
                minecraft.displayGuiScreen(new GuiCanvasEdit(minecraft.player,
                        tag, offhandItem.getTagCompound(), new TextComponentTranslation("item.xercapaint.item_canvas"), ((ItemCanvas) heldItem.getItem()).getCanvasType(), null));
            }
        } else if (heldItem.getItem() instanceof ItemPalette) {
            if (offhandItem.isEmpty() || !(offhandItem.getItem() instanceof ItemCanvas)) {
                minecraft.displayGuiScreen(new GuiPalette(heldItem.getTagCompound(), new TextComponentTranslation("item.xercapaint.item_palette")));
            } else {
                NBTTagCompound tag = offhandItem.getTagCompound();
                if (tag != null && tag.getInteger("generation") > 0) {
                    minecraft.displayGuiScreen(new GuiCanvasView(offhandItem.getTagCompound(), new TextComponentTranslation("item.xercapaint.item_canvas"), ((ItemCanvas) offhandItem.getItem()).getCanvasType(), null));
                } else {
                    minecraft.displayGuiScreen(new GuiCanvasEdit(minecraft.player,
                            tag, heldItem.getTagCompound(), new TextComponentTranslation("item.xercapaint.item_canvas"), ((ItemCanvas) offhandItem.getItem()).getCanvasType(), null));
                }
            }
        }
    }
}
