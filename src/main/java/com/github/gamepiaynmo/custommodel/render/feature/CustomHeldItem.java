package com.github.gamepiaynmo.custommodel.render.feature;

import com.github.gamepiaynmo.custommodel.client.CustomModelClient;
import com.github.gamepiaynmo.custommodel.client.ModelPack;
import com.github.gamepiaynmo.custommodel.render.CustomJsonModel;
import com.github.gamepiaynmo.custommodel.render.PlayerFeature;
import com.github.gamepiaynmo.custommodel.render.RenderContext;
import com.github.gamepiaynmo.custommodel.render.model.IBone;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.layers.LayerHeldItem;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHandSide;
import org.lwjgl.opengl.GL11;

public class CustomHeldItem extends LayerHeldItem {
    private final RenderContext context;

    public CustomHeldItem(RenderLivingBase<?> livingEntityRendererIn, RenderContext context) {
        super(livingEntityRendererIn);
        this.context = context;
    }

    @Override
    public void doRenderLayer(EntityLivingBase entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        ModelPack pack = CustomModelClient.getModelForPlayer(entitylivingbaseIn);
        CustomJsonModel model = pack == null ? null : pack.getModel();
        if (model == null) {
            super.doRenderLayer(entitylivingbaseIn, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, scale);
            return;
        }

        boolean flag = entitylivingbaseIn.getPrimaryHand() == EnumHandSide.RIGHT;
        ItemStack itemstack = flag ? entitylivingbaseIn.getHeldItemOffhand() : entitylivingbaseIn.getHeldItemMainhand();
        ItemStack itemstack1 = flag ? entitylivingbaseIn.getHeldItemMainhand() : entitylivingbaseIn.getHeldItemOffhand();

        if (!itemstack.isEmpty() || !itemstack1.isEmpty()) {
            GlStateManager.pushMatrix();

            if (this.livingEntityRenderer.getMainModel().isChild) {
                float f = 0.5F;
                GlStateManager.translate(0.0F, 0.75F, 0.0F);
                GlStateManager.scale(0.5F, 0.5F, 0.5F);
            }

            this.renderHeldItem(entitylivingbaseIn, itemstack1, ItemCameraTransforms.TransformType.THIRD_PERSON_RIGHT_HAND, EnumHandSide.RIGHT);
            this.renderHeldItem(entitylivingbaseIn, itemstack, ItemCameraTransforms.TransformType.THIRD_PERSON_LEFT_HAND, EnumHandSide.LEFT);
            GlStateManager.popMatrix();
        }
    }

    private void renderHeldItem(EntityLivingBase p_188358_1_, ItemStack p_188358_2_, ItemCameraTransforms.TransformType p_188358_3_, EnumHandSide handSide) {
        if (!p_188358_2_.isEmpty()) {
            boolean flag = handSide == EnumHandSide.LEFT;

            ModelPack pack = CustomModelClient.getModelForPlayer(p_188358_1_);
            CustomJsonModel model = pack == null ? null : pack.getModel();
            if (model != null) {
                PlayerFeature feature = flag ? PlayerFeature.HELD_ITEM_LEFT : PlayerFeature.HELD_ITEM_RIGHT;
                for (IBone bone : model.getFeatureAttached(feature)) {
                    if (bone.isVisible(context)) {
                        GlStateManager.pushMatrix();
                        if (bone.getPlayerBone() != null) {
                            this.translateToHand(handSide);
                            if (p_188358_1_.isSneaking())
                                GlStateManager.translate(0.0F, 0.2F, 0.0F);
                            GlStateManager.translate(-(float)(flag ? -1 : 1) / 16.0F, 0.625F, -0.125F);
                        } else {
                            GL11.glMultMatrix(context.currentInvTransform.toBuffer());
                            GL11.glMultMatrix(model.getTransform(bone).toBuffer());
                        }

                        GlStateManager.rotate(-90.0F, 1.0F, 0.0F, 0.0F);
                        GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
                        Minecraft.getMinecraft().getItemRenderer().renderItemSide(p_188358_1_, p_188358_2_, p_188358_3_, flag);
                        GlStateManager.popMatrix();
                    }
                }
            }
        }
    }
}
