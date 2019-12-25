package com.github.gamepiaynmo.custommodel.render.feature;

import com.github.gamepiaynmo.custommodel.client.CustomModelClient;
import com.github.gamepiaynmo.custommodel.client.ModelPack;
import com.github.gamepiaynmo.custommodel.render.CustomJsonModel;
import com.github.gamepiaynmo.custommodel.render.PlayerFeature;
import com.github.gamepiaynmo.custommodel.render.RenderContext;
import com.github.gamepiaynmo.custommodel.render.model.IBone;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.entity.layers.LayerCape;
import net.minecraft.entity.player.EnumPlayerModelParts;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL11;

public class CustomCape extends LayerCape {
    private final RenderContext context;
    private final RenderPlayer playerRenderer;

    public CustomCape(RenderPlayer playerRendererIn, RenderContext context) {
        super(playerRendererIn);
        this.context = context;
        this.playerRenderer = playerRendererIn;
    }

    @Override
    public void doRenderLayer(AbstractClientPlayer entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        ModelPack pack = CustomModelClient.getModelForPlayer(entitylivingbaseIn);
        CustomJsonModel model = pack == null ? null : pack.getModel();
        if (model == null) {
            super.doRenderLayer(entitylivingbaseIn, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, scale);
            return;
        }

        if (entitylivingbaseIn.hasPlayerInfo() && !entitylivingbaseIn.isInvisible() && entitylivingbaseIn.isWearing(EnumPlayerModelParts.CAPE)) {
            ItemStack itemstack = entitylivingbaseIn.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
            if (itemstack.getItem() != Items.ELYTRA) {
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

                for (IBone bone : model.getFeatureAttached(PlayerFeature.CAPE)) {
                    if (!bone.isVisible(context)) continue;
                    ResourceLocation customCapeTex = bone.getTexture(context).apply(context);
                    boolean customCape = !customCapeTex.equals(pack.getBaseTexture().apply(context));

                    if (entitylivingbaseIn.getLocationCape() != null || customCape) {
                        this.playerRenderer.bindTexture(customCape ? customCapeTex : entitylivingbaseIn.getLocationCape());
                        GlStateManager.pushMatrix();
                        GlStateManager.translate(0.0F, 0.0F, 0.125F);
                        double d0 = entitylivingbaseIn.prevChasingPosX + (entitylivingbaseIn.chasingPosX - entitylivingbaseIn.prevChasingPosX) * (double) partialTicks - (entitylivingbaseIn.prevPosX + (entitylivingbaseIn.posX - entitylivingbaseIn.prevPosX) * (double) partialTicks);
                        double d1 = entitylivingbaseIn.prevChasingPosY + (entitylivingbaseIn.chasingPosY - entitylivingbaseIn.prevChasingPosY) * (double) partialTicks - (entitylivingbaseIn.prevPosY + (entitylivingbaseIn.posY - entitylivingbaseIn.prevPosY) * (double) partialTicks);
                        double d2 = entitylivingbaseIn.prevChasingPosZ + (entitylivingbaseIn.chasingPosZ - entitylivingbaseIn.prevChasingPosZ) * (double) partialTicks - (entitylivingbaseIn.prevPosZ + (entitylivingbaseIn.posZ - entitylivingbaseIn.prevPosZ) * (double) partialTicks);
                        float f = entitylivingbaseIn.prevRenderYawOffset + (entitylivingbaseIn.renderYawOffset - entitylivingbaseIn.prevRenderYawOffset) * partialTicks;
                        double d3 = (double) MathHelper.sin(f * 0.017453292F);
                        double d4 = (double) (-MathHelper.cos(f * 0.017453292F));
                        float f1 = (float) d1 * 10.0F;
                        f1 = MathHelper.clamp(f1, -6.0F, 32.0F);
                        float f2 = (float) (d0 * d3 + d2 * d4) * 100.0F;
                        float f3 = (float) (d0 * d4 - d2 * d3) * 100.0F;

                        if (f2 < 0.0F) {
                            f2 = 0.0F;
                        }

                        float f4 = entitylivingbaseIn.prevCameraYaw + (entitylivingbaseIn.cameraYaw - entitylivingbaseIn.prevCameraYaw) * partialTicks;
                        f1 = f1 + MathHelper.sin((entitylivingbaseIn.prevDistanceWalkedModified + (entitylivingbaseIn.distanceWalkedModified - entitylivingbaseIn.prevDistanceWalkedModified) * partialTicks) * 6.0F) * 32.0F * f4;

                        if (entitylivingbaseIn.isSneaking()) {
                            f1 += 25.0F;
                        }

                        GL11.glMultMatrix(context.currentInvTransform.toBuffer());
                        GL11.glMultMatrix(model.getTransform(bone).toBuffer());

                        GlStateManager.rotate(6.0F + f2 / 2.0F + f1, 1.0F, 0.0F, 0.0F);
                        GlStateManager.rotate(f3 / 2.0F, 0.0F, 0.0F, 1.0F);
                        GlStateManager.rotate(-f3 / 2.0F, 0.0F, 1.0F, 0.0F);
                        GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
                        this.playerRenderer.getMainModel().renderCape(0.0625F);
                        GlStateManager.popMatrix();
                    }
                }
            }
        }
    }

}
