package com.github.gamepiaynmo.custommodel.client.render.feature;

import com.github.gamepiaynmo.custommodel.client.CustomModelClient;
import com.github.gamepiaynmo.custommodel.client.ModelPack;
import com.github.gamepiaynmo.custommodel.client.render.CustomJsonModel;
import com.github.gamepiaynmo.custommodel.client.render.PlayerFeature;
import com.github.gamepiaynmo.custommodel.client.render.RenderContext;
import com.github.gamepiaynmo.custommodel.client.render.model.IBone;
import com.github.gamepiaynmo.custommodel.server.ModConfig;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.layers.LayerArmorBase;
import net.minecraft.client.renderer.entity.layers.LayerBipedArmor;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import org.lwjgl.opengl.GL11;

public class CustomBipedArmor extends LayerBipedArmor {
    private final RenderContext context;
    private final RenderLivingBase<?> renderer;

    public CustomBipedArmor(RenderLivingBase<?> rendererIn, RenderContext context) {
        super(rendererIn);
        this.context = context;
        this.renderer = rendererIn;
    }

    @Override
    public void doRenderLayer(EntityLivingBase entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        ModelPack pack = CustomModelClient.manager.getModelForEntity(entitylivingbaseIn);
        CustomJsonModel model = pack == null ? null : pack.getModel();
        if (model == null) {
            super.doRenderLayer(entitylivingbaseIn, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, scale);
            return;
        }

        if (!ModConfig.isHideArmors()) {
            renderArmorLayer(entitylivingbaseIn, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, scale, EntityEquipmentSlot.CHEST);
            renderArmorLayer(entitylivingbaseIn, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, scale, EntityEquipmentSlot.LEGS);
            renderArmorLayer(entitylivingbaseIn, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, scale, EntityEquipmentSlot.FEET);
            renderArmorLayer(entitylivingbaseIn, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, scale, EntityEquipmentSlot.HEAD);
        }
    }

    private void renderArmorLayer(EntityLivingBase entityLivingBaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale, EntityEquipmentSlot slotIn) {
        ModelPack pack = CustomModelClient.manager.getModelForEntity(entityLivingBaseIn);
        CustomJsonModel model = pack == null ? null : pack.getModel();

        ItemStack itemstack = entityLivingBaseIn.getItemStackFromSlot(slotIn);
        if (itemstack.getItem() instanceof ItemArmor) {
            ItemArmor itemarmor = (ItemArmor)itemstack.getItem();
            if (itemarmor.getEquipmentSlot() == slotIn) {
                ModelBiped t = this.getModelFromSlot(slotIn);
                t = getArmorModelHook(entityLivingBaseIn, itemstack, slotIn, t);
                t.setModelAttributes(this.renderer.getMainModel());

                // ==============================================

                if (model == null)
                    t.setLivingAnimations(entityLivingBaseIn, limbSwing, limbSwingAmount, partialTicks);

                // ==============================================

                this.setModelSlotVisible(t, slotIn);
                boolean flag = this.isLegSlot(slotIn);
                this.renderer.bindTexture(this.getArmorResource(entityLivingBaseIn, itemstack, slotIn, null));

                {
                    float colorR = ObfuscationReflectionHelper.getPrivateValue(LayerArmorBase.class, this, 5);
                    float colorG = ObfuscationReflectionHelper.getPrivateValue(LayerArmorBase.class, this, 6);
                    float colorB = ObfuscationReflectionHelper.getPrivateValue(LayerArmorBase.class, this, 7);
                    float alpha = ObfuscationReflectionHelper.getPrivateValue(LayerArmorBase.class, this, 4);
                    boolean skipRenderGlint = ObfuscationReflectionHelper.getPrivateValue(LayerArmorBase.class, this, 8);

                    if (itemarmor.hasOverlay(itemstack)) // Allow this for anything, not only cloth
                    {
                        int i = itemarmor.getColor(itemstack);
                        float f = (float)(i >> 16 & 255) / 255.0F;
                        float f1 = (float)(i >> 8 & 255) / 255.0F;
                        float f2 = (float)(i & 255) / 255.0F;
                        GlStateManager.color(colorR * f, colorG * f1, colorB * f2, alpha);

                        if (model == null)
                            t.render(entityLivingBaseIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
                        else renderModel(entityLivingBaseIn, t, slotIn, model, scale);

                        this.renderer.bindTexture(this.getArmorResource(entityLivingBaseIn, itemstack, slotIn, "overlay"));
                    }
                    { // Non-colored
                        GlStateManager.color(colorR, colorG, colorB, alpha);
                        if (model == null)
                            t.render(entityLivingBaseIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
                        else renderModel(entityLivingBaseIn, t, slotIn, model, scale);
                    } // Default
                    if (!skipRenderGlint && itemstack.hasEffect())
                    {
                        renderEnchantedGlint(this.renderer, entityLivingBaseIn, t, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, scale);
                    }
                }
            }
        }
    }

    private boolean isLegSlot(EntityEquipmentSlot slotIn)
    {
        return slotIn == EntityEquipmentSlot.LEGS;
    }

    private void renderModel(EntityLivingBase entityLivingBaseIn, ModelBiped model, EntityEquipmentSlot slot, CustomJsonModel cmodel, float scale) {
        switch (slot) {
            case HEAD:
                renderCuboid(cmodel, PlayerFeature.HELMET_HEAD, model.bipedHead, scale);
                renderCuboid(cmodel, PlayerFeature.HELMET_HEAD_OVERLAY, model.bipedHeadwear, scale);
                break;
            case CHEST:
                renderCuboid(cmodel, PlayerFeature.CHESTPLATE_BODY, model.bipedBody, scale);
                renderCuboid(cmodel, PlayerFeature.CHESTPLATE_LEFT_ARM, model.bipedLeftArm, scale);
                renderCuboid(cmodel, PlayerFeature.CHESTPLATE_RIGHT_ARM, model.bipedRightArm, scale);
                break;
            case LEGS:
                renderCuboid(cmodel, PlayerFeature.LEGGINGS_BODY, model.bipedBody, scale);
                renderCuboid(cmodel, PlayerFeature.LEGGINGS_LEFT_LEG, model.bipedLeftLeg, scale);
                renderCuboid(cmodel, PlayerFeature.LEGGINGS_RIGHT_LEG, model.bipedRightLeg, scale);
                break;
            case FEET:
                renderCuboid(cmodel, PlayerFeature.BOOTS_LEFT_LEG, model.bipedLeftLeg, scale);
                renderCuboid(cmodel, PlayerFeature.BOOTS_RIGHT_LEG, model.bipedRightLeg, scale);
                break;
        }
    }

    private void renderCuboid(CustomJsonModel model, PlayerFeature feature, ModelRenderer cuboid, float scale) {
        for (IBone bone : model.getFeatureAttached(feature)) {
            boolean visible = cuboid.showModel;
            cuboid.showModel = bone.isVisible(context);
            if (cuboid.showModel) {
                float x = cuboid.rotationPointX;
                float y = cuboid.rotationPointY;
                float z = cuboid.rotationPointZ;
                float yaw = cuboid.rotateAngleY;
                float pitch = cuboid.rotateAngleX;
                float roll = cuboid.rotateAngleZ;
                cuboid.rotationPointX = 0;
                cuboid.rotationPointY = 0;
                cuboid.rotationPointZ = 0;
                cuboid.rotateAngleY = 0;
                cuboid.rotateAngleX = 0;
                cuboid.rotateAngleZ = 0;

                GlStateManager.pushMatrix();
                GL11.glMultMatrix(context.currentInvTransform.toBuffer());
                GL11.glMultMatrix(model.getTransform(bone).toBuffer());
                cuboid.render(scale);
                GlStateManager.popMatrix();

                cuboid.rotationPointX = x;
                cuboid.rotationPointY = y;
                cuboid.rotationPointZ = z;
                cuboid.rotateAngleY = yaw;
                cuboid.rotateAngleX = pitch;
                cuboid.rotateAngleZ = roll;
            }

            cuboid.showModel = visible;
        }
    }
}
