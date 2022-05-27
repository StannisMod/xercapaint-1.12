package xerca.xercapaint.client;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class EaselModel extends ModelBase {

    private final ModelRenderer bb_main;
    private final ModelRenderer cube_r1;
    private final ModelRenderer cube_r2;
    private final ModelRenderer cube_r3;
    private final ModelRenderer cube_r4;
    private final ModelRenderer cube_r5;

    public EaselModel() {
        textureWidth = 16;
        textureHeight = 32;

        bb_main = new ModelRenderer(this);
        bb_main.setRotationPoint(0.0F, 24.0F, 0.0F);


        cube_r1 = new ModelRenderer(this);
        cube_r1.setRotationPoint(5.0F, 0.0F, -5.0F);
        bb_main.addChild(cube_r1);
        setRotationAngle(cube_r1, -0.2618F, 0.0F, 0.0F);
        cube_r1.setTextureOffset(12, 0).addBox(-5.5F, -33.5F, -0.8F, 1, 26, 1, false);

        cube_r2 = new ModelRenderer(this);
        cube_r2.setRotationPoint(0.0F, -7.5F, -3.5F);
        bb_main.addChild(cube_r2);
        setRotationAngle(cube_r2, -0.2618F, 0.0F, 0.0F);
        cube_r2.setTextureOffset(2, 2).addBox(-3.0F, -14.0F, -0.5F, 6, 1, 1, false);
        cube_r2.setTextureOffset(0, 0).addBox(-4.5F, -0.5F, -0.5F, 9, 1, 1, false);

        cube_r3 = new ModelRenderer(this);
        cube_r3.setRotationPoint(0.0F, 0.0F, 6.0F);
        bb_main.addChild(cube_r3);
        setRotationAngle(cube_r3, 0.2618F, 0.0F, 0.0F);
        cube_r3.setTextureOffset(8, 0).addBox(-0.5F, -21.0F, -1.0F, 1, 21, 1, false);

        cube_r4 = new ModelRenderer(this);
        cube_r4.setRotationPoint(5.0F, 0.0F, -5.0F);
        bb_main.addChild(cube_r4);
        setRotationAngle(cube_r4, -0.2618F, 0.0F, -0.1309F);
        cube_r4.setTextureOffset(4, 0).addBox(0.0F, -29.0F, -1.0F, 1, 29, 1, false);

        cube_r5 = new ModelRenderer(this);
        cube_r5.setRotationPoint(-5.0F, 0.0F, -5.0F);
        bb_main.addChild(cube_r5);
        setRotationAngle(cube_r5, -0.2618F, 0.0F, 0.1309F);
        cube_r5.setTextureOffset(0, 0).addBox(-1.0F, -29.0F, -1.0F, 1, 29, 1, false);
    }

    @Override
    public void render(final Entity entityIn, final float limbSwing, final float limbSwingAmount, final float ageInTicks, final float netHeadYaw, final float headPitch, final float scale) {
        super.render(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
        bb_main.render(scale);
    }

    public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
        modelRenderer.rotateAngleX = x;
        modelRenderer.rotateAngleY = y;
        modelRenderer.rotateAngleZ = z;
    }
}
