package com.github.gamepiaynmo.custommodel.render.feature;

import com.github.gamepiaynmo.custommodel.client.CustomModelClient;
import com.github.gamepiaynmo.custommodel.client.ModelPack;
import com.github.gamepiaynmo.custommodel.render.CustomJsonModel;
import com.github.gamepiaynmo.custommodel.render.PlayerFeature;
import com.github.gamepiaynmo.custommodel.render.model.IBone;
import com.github.gamepiaynmo.custommodel.server.CustomModel;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.ParrotEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.feature.ShoulderParrotFeatureRenderer;
import net.minecraft.client.render.entity.model.ParrotEntityModel;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import org.lwjgl.opengl.GL11;

public class CustomShoulderParrot<T extends AbstractClientPlayerEntity> extends ShoulderParrotFeatureRenderer<T> {
    private final ParrotEntityModel model = new ParrotEntityModel();

    public CustomShoulderParrot(FeatureRendererContext<T, PlayerEntityModel<T>> featureRendererContext_1) {
        super(featureRendererContext_1);
    }

    @Override
    public void render(T playerEntity_1, float float_1, float float_2, float float_3, float float_4, float float_5, float float_6, float float_7) {
        ModelPack pack = CustomModelClient.getModelForPlayer(playerEntity_1);
        CustomJsonModel model = pack == null ? null : pack.getModel();
        if (model == null) {
            super.method_4185(playerEntity_1, float_1, float_2, float_3, float_4, float_5, float_6, float_7);
            return;
        }

        GlStateManager.enableRescaleNormal();
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.renderShoulderParrot(playerEntity_1, float_1, float_2, float_3, float_5, float_6, float_7, true);
        this.renderShoulderParrot(playerEntity_1, float_1, float_2, float_3, float_5, float_6, float_7, false);
        GlStateManager.disableRescaleNormal();
    }

    private void renderShoulderParrot(T playerEntity_1, float float_1, float float_2, float float_3, float float_4, float float_5, float float_6, boolean boolean_1) {
        CompoundTag compoundTag_1 = boolean_1 ? playerEntity_1.getShoulderEntityLeft() : playerEntity_1.getShoulderEntityRight();
        EntityType.get(compoundTag_1.getString("id")).filter((entityType_1) -> {
            return entityType_1 == EntityType.PARROT;
        }).ifPresent((entityType_1) -> {
            ModelPack pack = CustomModelClient.getModelForPlayer(playerEntity_1);
            CustomJsonModel model = pack == null ? null : pack.getModel();

            for (IBone bone : model.getFeatureAttached(boolean_1 ? PlayerFeature.SHOULDER_PARROT_LEFT : PlayerFeature.SHOULDER_PARROT_RIGHT)) {
                if (!bone.isVisible()) continue;

                GlStateManager.pushMatrix();
                if (bone.getPlayerBone() != null) {
                    GlStateManager.translatef(boolean_1 ? 0.4F : -0.4F, playerEntity_1.isInSneakingPose() ? -1.3F : -1.5F, 0.0F);
                } else {
                    GL11.glMultMatrixd(CustomModelClient.currentInvTransform.val);
                    GL11.glMultMatrixd(model.getTransform(bone).val);
                    GlStateManager.translatef(0, -1.5F, 0);
                }
                this.bindTexture(ParrotEntityRenderer.SKINS[compoundTag_1.getInt("Variant")]);
                this.model.method_17106(float_1, float_2, float_4, float_5, float_6, playerEntity_1.age);
                GlStateManager.popMatrix();
            }
        });
    }
}