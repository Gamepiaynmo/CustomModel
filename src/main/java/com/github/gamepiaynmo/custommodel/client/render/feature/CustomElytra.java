package com.github.gamepiaynmo.custommodel.client.render.feature;

import com.github.gamepiaynmo.custommodel.client.CustomModelClient;
import com.github.gamepiaynmo.custommodel.client.ModelPack;
import com.github.gamepiaynmo.custommodel.client.render.CustomJsonModel;
import com.github.gamepiaynmo.custommodel.client.render.PlayerFeature;
import com.github.gamepiaynmo.custommodel.client.render.RenderContext;
import com.github.gamepiaynmo.custommodel.client.render.model.IBone;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelElytra;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.layers.LayerArmorBase;
import net.minecraft.client.renderer.entity.layers.LayerElytra;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EnumPlayerModelParts;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class CustomElytra extends LayerElytra {
    private static final ResourceLocation TEXTURE_ELYTRA = new ResourceLocation("textures/entity/elytra.png");
    private final ModelElytra modelElytra = new ModelElytra();
    private final RenderContext context;

    public CustomElytra(RenderLivingBase<?> p_i47185_1_, RenderContext context) {
        super(p_i47185_1_);
        this.context = context;
    }

    @Override
    public void doRenderLayer(EntityLivingBase entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        ModelPack pack = CustomModelClient.manager.getModelForEntity(entitylivingbaseIn);
        CustomJsonModel model = pack == null ? null : pack.getModel();
        if (model == null) {
            super.doRenderLayer(entitylivingbaseIn, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, scale);
            return;
        }

        ItemStack itemstack = entitylivingbaseIn.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
        if (itemstack.getItem() == Items.ELYTRA) {
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);

            for (IBone bone : model.getFeatureAttached(PlayerFeature.ELYTRA)) {
                if (!bone.isVisible(context)) continue;
                ResourceLocation customElytraTex = bone.getTexture(context).apply(context);
                boolean customElytra = !customElytraTex.equals(pack.getBaseTexture().apply(context));

                if (entitylivingbaseIn instanceof AbstractClientPlayer) {
                    AbstractClientPlayer abstractclientplayer = (AbstractClientPlayer) entitylivingbaseIn;
                    if (customElytra) {
                        this.renderPlayer.bindTexture(customElytraTex);
                    } else if (abstractclientplayer.isPlayerInfoSet() && abstractclientplayer.getLocationElytra() != null) {
                        this.renderPlayer.bindTexture(abstractclientplayer.getLocationElytra());
                    } else if (abstractclientplayer.hasPlayerInfo() && abstractclientplayer.getLocationCape() != null && abstractclientplayer.isWearing(EnumPlayerModelParts.CAPE)) {
                        this.renderPlayer.bindTexture(abstractclientplayer.getLocationCape());
                    } else {
                        this.renderPlayer.bindTexture(TEXTURE_ELYTRA);
                    }
                } else {
                    this.renderPlayer.bindTexture(TEXTURE_ELYTRA);
                }

                GlStateManager.pushMatrix();
                GlStateManager.translate(0.0F, 0.0F, 0.125F);

                GL11.glMultMatrix(context.currentInvTransform.toBuffer());
                GL11.glMultMatrix(model.getTransform(bone).toBuffer());

                this.modelElytra.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, entitylivingbaseIn);
                this.modelElytra.render(entitylivingbaseIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);

                if (itemstack.isItemEnchanted()) {
                    LayerArmorBase.renderEnchantedGlint(this.renderPlayer, entitylivingbaseIn, this.modelElytra, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, scale);
                }

                GlStateManager.disableBlend();
                GlStateManager.popMatrix();
            }
        }
    }
}
