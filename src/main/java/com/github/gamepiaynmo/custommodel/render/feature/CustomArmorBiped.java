package com.github.gamepiaynmo.custommodel.render.feature;

import com.github.gamepiaynmo.custommodel.client.CustomModelClient;
import com.github.gamepiaynmo.custommodel.client.ModelPack;
import com.github.gamepiaynmo.custommodel.render.CustomJsonModel;
import com.github.gamepiaynmo.custommodel.render.model.IBone;
import com.github.gamepiaynmo.custommodel.util.Matrix4;
import com.github.gamepiaynmo.custommodel.util.Quaternion;
import com.github.gamepiaynmo.custommodel.util.Vector3;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.model.Cuboid;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.feature.ArmorBipedFeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.DyeableArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.lwjgl.opengl.GL11;

import java.util.Map;

public class CustomArmorBiped<T extends AbstractClientPlayerEntity, M extends BipedEntityModel<T>, A extends BipedEntityModel<T>> extends ArmorBipedFeatureRenderer<T, M, A> {
    private static final Map<String, Identifier> ARMOR_TEXTURE_CACHE = Maps.newHashMap();

    public CustomArmorBiped(FeatureRendererContext<T, M> featureRendererContext_1, A bipedEntityModel_1, A bipedEntityModel_2) {
        super(featureRendererContext_1, bipedEntityModel_1, bipedEntityModel_2);
    }

    private Matrix4 invTransform;

    @Override
    public void render(T livingEntity_1, float float_1, float float_2, float float_3, float float_4, float float_5, float float_6, float float_7) {
        invTransform = CustomModelClient.currentTransform.cpy().inv();
        this.renderArmor(livingEntity_1, float_1, float_2, float_3, float_4, float_5, float_6, float_7, EquipmentSlot.CHEST);
        this.renderArmor(livingEntity_1, float_1, float_2, float_3, float_4, float_5, float_6, float_7, EquipmentSlot.LEGS);
        this.renderArmor(livingEntity_1, float_1, float_2, float_3, float_4, float_5, float_6, float_7, EquipmentSlot.FEET);
        this.renderArmor(livingEntity_1, float_1, float_2, float_3, float_4, float_5, float_6, float_7, EquipmentSlot.HEAD);
    }

    private void renderArmor(T livingEntity_1, float float_1, float float_2, float float_3, float float_4, float float_5, float float_6, float float_7, EquipmentSlot equipmentSlot_1) {
        ModelPack pack = CustomModelClient.getModelForPlayer(livingEntity_1);
        CustomJsonModel model = pack == null ? null : pack.getModel();

        ItemStack itemStack_1 = livingEntity_1.getEquippedStack(equipmentSlot_1);
        if (itemStack_1.getItem() instanceof ArmorItem) {
            ArmorItem armorItem_1 = (ArmorItem)itemStack_1.getItem();
            if (armorItem_1.getSlotType() == equipmentSlot_1) {
                A bipedEntityModel_1 = this.getArmor(equipmentSlot_1);
                ((BipedEntityModel)this.getModel()).setAttributes(bipedEntityModel_1);

                // ==============================================

                if (model == null) {
                    bipedEntityModel_1.method_17086(livingEntity_1, float_1, float_2, float_3);
                }

                // ==============================================

                this.method_4170(bipedEntityModel_1, equipmentSlot_1);
                boolean boolean_1 = this.isLegs(equipmentSlot_1);
                this.bindTexture(this.getArmorTexture(armorItem_1, boolean_1));
                if (armorItem_1 instanceof DyeableArmorItem) {
                    int int_1 = ((DyeableArmorItem)armorItem_1).getColor(itemStack_1);
                    float float_8 = (float)(int_1 >> 16 & 255) / 255.0F;
                    float float_9 = (float)(int_1 >> 8 & 255) / 255.0F;
                    float float_10 = (float)(int_1 & 255) / 255.0F;
                    GlStateManager.color4f(float_8, float_9, float_10, 1.0f);

                    if (model == null)
                        bipedEntityModel_1.method_17088(livingEntity_1, float_1, float_2, float_4, float_5, float_6, float_7);
                    else renderModel(livingEntity_1, bipedEntityModel_1, equipmentSlot_1, model, float_7);

                    this.bindTexture(this.method_4174(armorItem_1, boolean_1, "overlay"));
                }

                GlStateManager.color4f(1.0f, 1.0f, 1.0f, 1.0f);

                if (model == null)
                    bipedEntityModel_1.method_17088(livingEntity_1, float_1, float_2, float_4, float_5, float_6, float_7);
                else renderModel(livingEntity_1, bipedEntityModel_1, equipmentSlot_1, model, float_7);

                if (itemStack_1.hasEnchantments()) {
                    renderEnchantedGlint(this::bindTexture, livingEntity_1, bipedEntityModel_1, float_1, float_2, float_3, float_4, float_5, float_6, float_7);
                }

            }
        }
    }

    private void renderCuboid(CustomJsonModel model, String boneName, Cuboid cuboid, float scale) {
        IBone bone = model.getBone(boneName);
        cuboid.visible = bone.isVisible();
        if (cuboid.visible) {
            Matrix4 transform = model.getTransform(bone);
            transform.mulLeft(invTransform);
            cuboid.rotationPointX = 0;
            cuboid.rotationPointY = 0;
            cuboid.rotationPointZ = 0;
            cuboid.yaw = 0;
            cuboid.pitch = 0;
            cuboid.roll = 0;
            GlStateManager.pushMatrix();
            GL11.glMultMatrixd(transform.val);
            cuboid.render(scale);
            GlStateManager.popMatrix();
        }
    }

    private void renderModel(T livingEntity_1, BipedEntityModel model, EquipmentSlot slot, CustomJsonModel cmodel, float scale) {
        GlStateManager.pushMatrix();
        if (livingEntity_1.isInSneakingPose())
            GlStateManager.translatef(0.0F, 0.2F, 0.0F);

        if (model != null) {
            switch (slot) {
                case HEAD:
                    renderCuboid(cmodel, "head", model.head, scale);
                    renderCuboid(cmodel, "head_overlay", model.headwear, scale);
                    break;
                case CHEST:
                    renderCuboid(cmodel, "body", model.body, scale);
                    renderCuboid(cmodel, "left_arm", model.leftArm, scale);
                    renderCuboid(cmodel, "right_arm", model.rightArm, scale);
                    break;
                case LEGS:
                    renderCuboid(cmodel, "body", model.body, scale);
                    renderCuboid(cmodel, "left_leg", model.leftLeg, scale);
                    renderCuboid(cmodel, "right_leg", model.rightLeg, scale);
                    break;
                case FEET:
                    renderCuboid(cmodel, "left_leg", model.leftLeg, scale);
                    renderCuboid(cmodel, "right_leg", model.rightLeg, scale);
                    break;
            }
        }

        GlStateManager.popMatrix();
    }

    // copied
    private Identifier getArmorTexture(ArmorItem armorItem_1, boolean boolean_1) {
        return this.method_4174(armorItem_1, boolean_1, (String)null);
    }

    // copied
    private Identifier method_4174(ArmorItem armorItem_1, boolean boolean_1, String string_1) {
        String string_2 = "textures/models/armor/" + armorItem_1.getMaterial().getName() + "_layer_" + (boolean_1 ? 2 : 1) + (string_1 == null ? "" : "_" + string_1) + ".png";
        return (Identifier)ARMOR_TEXTURE_CACHE.computeIfAbsent(string_2, Identifier::new);
    }

    // copied
    private boolean isLegs(EquipmentSlot equipmentSlot_1) {
        return equipmentSlot_1 == EquipmentSlot.LEGS;
    }
}
