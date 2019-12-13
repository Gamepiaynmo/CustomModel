package com.github.gamepiaynmo.custommodel.render.feature;

import com.github.gamepiaynmo.custommodel.client.CustomModelClient;
import com.github.gamepiaynmo.custommodel.client.ModelPack;
import com.github.gamepiaynmo.custommodel.render.CustomJsonModel;
import com.github.gamepiaynmo.custommodel.render.PlayerFeature;
import com.github.gamepiaynmo.custommodel.render.model.IBone;
import com.github.gamepiaynmo.custommodel.server.CustomModel;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.PlayerModelPart;
import net.minecraft.client.render.entity.feature.ArmorFeatureRenderer;
import net.minecraft.client.render.entity.feature.ElytraFeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.ElytraEntityModel;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import org.lwjgl.opengl.GL11;

import java.util.function.Supplier;

public class CustomElytra<T extends AbstractClientPlayerEntity, M extends PlayerEntityModel<T>> extends ElytraFeatureRenderer<T, M> {
    private static final Identifier SKIN = new Identifier("textures/entity/elytra.png");
    private final ElytraEntityModel<T> elytra = new ElytraEntityModel();

    public CustomElytra(FeatureRendererContext<T, M> featureRendererContext_1) {
        super(featureRendererContext_1);
    }

    @Override
    public void method_17161(T livingEntity_1, float float_1, float float_2, float float_3, float float_4, float float_5, float float_6, float float_7) {
        ModelPack pack = CustomModelClient.getModelForPlayer(livingEntity_1);
        CustomJsonModel model = pack == null ? null : pack.getModel();
        if (model == null) {
            super.method_17161(livingEntity_1, float_1, float_2, float_3, float_4, float_5, float_6, float_7);
            return;
        }

        ItemStack itemStack_1 = livingEntity_1.getEquippedStack(EquipmentSlot.CHEST);
        if (itemStack_1.getItem() == Items.ELYTRA) {
            GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);

            for (IBone bone : model.getFeatureAttached(PlayerFeature.ELYTRA)) {
                Identifier customElytraTex = bone.getTexture().get();
                boolean customElytra = !customElytraTex.equals(pack.getBaseTexture().get());
                if (customElytra) {
                    this.bindTexture(customElytraTex);
                } else if (livingEntity_1.canRenderElytraTexture() && livingEntity_1.getElytraTexture() != null) {
                    this.bindTexture(livingEntity_1.getElytraTexture());
                } else if (livingEntity_1.canRenderCapeTexture() && livingEntity_1.getCapeTexture() != null && livingEntity_1.isSkinOverlayVisible(PlayerModelPart.CAPE)) {
                    this.bindTexture(livingEntity_1.getCapeTexture());
                } else {
                    this.bindTexture(SKIN);
                }

                GlStateManager.pushMatrix();
                GlStateManager.translatef(0.0F, 0.0F, 0.125F);

                GL11.glMultMatrixd(CustomModelClient.currentInvTransform.val);
                GL11.glMultMatrixd(model.getTransform(bone).val);

                this.elytra.method_17079(livingEntity_1, float_1, float_2, float_4, float_5, float_6, float_7);
                this.elytra.method_17078(livingEntity_1, float_1, float_2, float_4, float_5, float_6, float_7);
                if (itemStack_1.hasEnchantments()) {
                    ArmorFeatureRenderer.renderEnchantedGlint(this::bindTexture, livingEntity_1, this.elytra, float_1, float_2, float_3, float_4, float_5, float_6, float_7);
                }

                GlStateManager.disableBlend();
                GlStateManager.popMatrix();
            }
        }
    }
}