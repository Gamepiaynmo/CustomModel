package com.github.gamepiaynmo.custommodel.render.feature;

import com.github.gamepiaynmo.custommodel.client.CustomModelClient;
import com.github.gamepiaynmo.custommodel.client.ModelPack;
import com.github.gamepiaynmo.custommodel.render.CustomJsonModel;
import com.github.gamepiaynmo.custommodel.render.PlayerFeature;
import com.github.gamepiaynmo.custommodel.render.model.IBone;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.PlayerModelPart;
import net.minecraft.client.render.entity.feature.CapeFeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL11;

import java.util.function.Supplier;

public class CustomCape extends CapeFeatureRenderer {
    public CustomCape(FeatureRendererContext<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> featureRendererContext_1) {
        super(featureRendererContext_1);
    }

    @Override
    public void render(AbstractClientPlayerEntity abstractClientPlayerEntity_1, float float_1, float float_2, float float_3, float float_4, float float_5, float float_6, float float_7) {
        ModelPack pack = CustomModelClient.getModelForPlayer(abstractClientPlayerEntity_1);
        CustomJsonModel model = pack == null ? null : pack.getModel();
        if (model == null) {
            super.method_4177(abstractClientPlayerEntity_1, float_1, float_2, float_3, float_4, float_5, float_6, float_7);
            return;
        }

        if (!abstractClientPlayerEntity_1.isInvisible() && abstractClientPlayerEntity_1.isSkinOverlayVisible(PlayerModelPart.CAPE)) {
            ItemStack itemStack_1 = abstractClientPlayerEntity_1.getEquippedStack(EquipmentSlot.CHEST);
            if (itemStack_1.getItem() != Items.ELYTRA) {
                GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
                for (IBone bone : model.getFeatureAttached(PlayerFeature.CAPE)) {
                    Identifier customCapeTex = bone.getTexture().get();
                    boolean customCape = !customCapeTex.equals(pack.getBaseTexture().get());

                    if ((abstractClientPlayerEntity_1.getCapeTexture() != null && abstractClientPlayerEntity_1.canRenderCapeTexture()) || customCape) {
                        GlStateManager.pushMatrix();
                        GlStateManager.translatef(0.0F, 0.0F, 0.125F);
                        double double_1 = MathHelper.lerp((double) float_3, abstractClientPlayerEntity_1.field_7524, abstractClientPlayerEntity_1.field_7500) - MathHelper.lerp((double) float_3, abstractClientPlayerEntity_1.prevX, abstractClientPlayerEntity_1.x);
                        double double_2 = MathHelper.lerp((double) float_3, abstractClientPlayerEntity_1.field_7502, abstractClientPlayerEntity_1.field_7521) - MathHelper.lerp((double) float_3, abstractClientPlayerEntity_1.prevY, abstractClientPlayerEntity_1.y);
                        double double_3 = MathHelper.lerp((double) float_3, abstractClientPlayerEntity_1.field_7522, abstractClientPlayerEntity_1.field_7499) - MathHelper.lerp((double) float_3, abstractClientPlayerEntity_1.prevZ, abstractClientPlayerEntity_1.z);
                        float float_8 = abstractClientPlayerEntity_1.field_6220 + (abstractClientPlayerEntity_1.field_6283 - abstractClientPlayerEntity_1.field_6220);
                        double double_4 = (double) MathHelper.sin(float_8 * 0.017453292F);
                        double double_5 = (double) (-MathHelper.cos(float_8 * 0.017453292F));
                        float float_9 = (float) double_2 * 10.0F;
                        float_9 = MathHelper.clamp(float_9, -6.0F, 32.0F);
                        float float_10 = (float) (double_1 * double_4 + double_3 * double_5) * 100.0F;
                        float_10 = MathHelper.clamp(float_10, 0.0F, 150.0F);
                        float float_11 = (float) (double_1 * double_5 - double_3 * double_4) * 100.0F;
                        float_11 = MathHelper.clamp(float_11, -20.0F, 20.0F);
                        if (float_10 < 0.0F) {
                            float_10 = 0.0F;
                        }

                        float float_12 = MathHelper.lerp(float_3, abstractClientPlayerEntity_1.field_7505, abstractClientPlayerEntity_1.field_7483);
                        float_9 += MathHelper.sin(MathHelper.lerp(float_3, abstractClientPlayerEntity_1.prevHorizontalSpeed, abstractClientPlayerEntity_1.horizontalSpeed) * 6.0F) * 32.0F * float_12;
                        if (abstractClientPlayerEntity_1.isInSneakingPose()) {
                            float_9 += 25.0F;
                        }

                        GL11.glMultMatrixd(CustomModelClient.currentInvTransform.val);
                        GL11.glMultMatrixd(model.getTransform(bone).val);
                        this.bindTexture(customCape ? customCapeTex : abstractClientPlayerEntity_1.getCapeTexture());

                        GlStateManager.rotatef(6.0F + float_10 / 2.0F + float_9, 1.0F, 0.0F, 0.0F);
                        GlStateManager.rotatef(float_11 / 2.0F, 0.0F, 0.0F, 1.0F);
                        GlStateManager.rotatef(-float_11 / 2.0F, 0.0F, 1.0F, 0.0F);
                        GlStateManager.rotatef(180.0F, 0.0F, 1.0F, 0.0F);
                        ((PlayerEntityModel) this.getModel()).renderCape(0.0625F);
                        GlStateManager.popMatrix();
                    }
                }
            }
        }
    }
}
