package xerca.xercapaint.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import xerca.xercapaint.common.XercaPaint;
import xerca.xercapaint.common.entity.EntityEasel;
import xerca.xercapaint.common.item.ItemCanvas;

import javax.annotation.Nullable;

public class RenderEntityEasel extends Render<EntityEasel> {

    protected EaselModel model;
    static public RenderEntityEasel theInstance;
    static private final ResourceLocation woodTexture = new ResourceLocation(XercaPaint.MODID, "textures/block/birch_long.png");

    RenderEntityEasel(RenderManager manager) {
        super(manager);
        this.model = new EaselModel();
    }

    @Nullable
    @Override
    public ResourceLocation getEntityTexture(EntityEasel entity) {
        return woodTexture;
    }

    @Override
    public void doRender(final EntityEasel entity, final double x, final double y, final double z, final float entityYaw, final float partialTicks) {
        super.doRender(entity, x, y, z, entityYaw, partialTicks);
        GlStateManager.pushMatrix();

        GlStateManager.translate(x, y, z);
        GlStateManager.rotate(-entityYaw, 0.0F, 1.0F, 0.0F);

        //this.model.setupAnim(entity, 0, 0, 0, 0, 0);

        GlStateManager.rotate(180, 0.0F, 1.0F, 0.0F);
        GlStateManager.translate(0, -1.5, 0);

        //RenderType rendertype = this.model.renderType(this.getTextureLocation(entity));

        //int i = OverlayTexture.pack(OverlayTexture.u(0), OverlayTexture.v(false));
        //this.model.renderToBuffer(matrixStackIn, vertexconsumer, packedLightIn, i, 1.0F, 1.0F, 1.0F, 1.0F);
        //Minecraft.getMinecraft().getTextureManager().bindTexture(Minecraft.getMinecraft().getBlockRendererDispatcher());
        this.model.render(null, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, partialTicks);

        ItemStack itemstack = entity.getItem();
        if (itemstack.getItem() instanceof ItemCanvas) {
            ItemCanvas itemCanvas = (ItemCanvas) itemstack.getItem();
            GlStateManager.pushMatrix();

            switch (itemCanvas.getCanvasType()) {
                case SMALL:
                    GlStateManager.scale(1.5F, 1.5f, 1.5f);
                    GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
                    GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);
                    GlStateManager.rotate(-15.0F, 1.0F, 0.0F, 0.0F);
                    GlStateManager.translate(-0.5, -1.17, -0.5);
                    break;
                case LARGE:
//                    poseStack.scale(1.5F, 1.5f, 1.5f);
                    GlStateManager.scale(2F, 2f, 2f);
                    GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
                    GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);
                    GlStateManager.rotate(-15.0F, 1.0F, 0.0F, 0.0F);
                    GlStateManager.translate(-0.45, -1.015, -0.5);
                    break;
                case LONG:
                    GlStateManager.scale(2F, 2f, 2f);
                    GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
                    GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);
                    GlStateManager.rotate(-15.0F, 1.0F, 0.0F, 0.0F);
                    GlStateManager.translate(-0.45, -0.915, -0.5);
                    break;
                case TALL:
//                    poseStack.scale(1.75F, 1.75f, 1.75f);
                    GlStateManager.scale(2F, 2f, 2f);
                    GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
                    GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);
                    GlStateManager.rotate(-15.0F, 1.0F, 0.0F, 0.0F);
                    GlStateManager.translate(-0.595, -1.015, -0.5);
                    break;
            }

            itemCanvas.getTileEntityItemStackRenderer().renderByItem(itemstack, partialTicks);

            GlStateManager.popMatrix();
        }

        GlStateManager.popMatrix();
        //super.render(entity, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
    }

    @Override
    protected boolean canRenderName(EntityEasel easel) {
        RayTraceResult result = Minecraft.getMinecraft().objectMouseOver;
        if (result.typeOfHit == RayTraceResult.Type.ENTITY) {
            if (/*Minecraft.getMinecraft().world.getGameRules(). &&*/ result.entityHit == easel && !easel.getItem().isEmpty()) { //&& ItemCanvas.hasTitle(easel.getItem())) {
                double d0 = Minecraft.getMinecraft().player.getDistanceSq(easel);
                float f = 64;//easel.isDiscrete() ? 32.0F : 64.0F;
                return d0 < (double) (f * f);
            }
        }
        return false;
    }

    @Override
    protected void renderEntityName(final EntityEasel entityIn, final double x, final double y, final double z, final String name, final double distanceSq) {
        super.renderEntityName(entityIn, x, y, z, name, distanceSq);
    }

    @Override
    protected void renderLivingLabel(final EntityEasel entityIn, final String str, final double x, final double y, final double z, final int maxDistance) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(0.0F, -0.5F, 0.0F);
        super.renderLivingLabel(entityIn, str, x, y, z, maxDistance);
        GlStateManager.popMatrix();
    }

//    @Override
//    protected void renderName(EntityEasel easel, ITextComponent component, int p_115087_) {
//        poseStack.pushPose();
//        poseStack.translate(0, -0.5, 0);
//        super.renderName(easel, ItemCanvas.getFullLabel(easel.getItem()), poseStack, bufferSource, p_115087_);
//        poseStack.popPose();
//    }

    public static class RenderEntityCanvasFactory implements IRenderFactory<EntityEasel> {
        @Override
        public Render<? super EntityEasel> createRenderFor(RenderManager manager) {
            theInstance = new RenderEntityEasel(manager);
            return theInstance;
        }
    }

//    public static Quaternion fromXYZDegrees(Vector3f p_175226_) {
//        return fromXYZ((float) Math.toRadians((double) p_175226_.x()), (float) Math.toRadians((double) p_175226_.y()), (float) Math.toRadians((double) p_175226_.z()));
//    }
//
//    private static Quaternion fromXYZ(float p_175219_, float p_175220_, float p_175221_) {
//        Quaternion quaternion = new Quaternion();
//        quaternion.mul(new Quaternion(0.0F, (float) Math.sin((double) (p_175219_ / 2.0F)), 0.0F, (float) Math.cos((double) (p_175219_ / 2.0F))));
//        quaternion.mul(new Quaternion((float) Math.sin((double) (p_175220_ / 2.0F)), 0.0F, 0.0F, (float) Math.cos((double) (p_175220_ / 2.0F))));
//        quaternion.mul(new Quaternion(0.0F, 0.0F, (float) Math.sin((double) (p_175221_ / 2.0F)), (float) Math.cos((double) (p_175221_ / 2.0F))));
//        return quaternion;
//    }
}
