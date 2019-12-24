package com.github.gamepiaynmo.custommodel.render.feature;

import com.github.gamepiaynmo.custommodel.client.CustomModelClient;
import com.github.gamepiaynmo.custommodel.client.ModelPack;
import com.github.gamepiaynmo.custommodel.render.CustomJsonModel;
import com.github.gamepiaynmo.custommodel.render.PlayerFeature;
import com.github.gamepiaynmo.custommodel.render.RenderContext;
import com.github.gamepiaynmo.custommodel.render.model.IBone;
import com.github.gamepiaynmo.custommodel.util.Matrix4;
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
    private final RenderContext context;

    public CustomArmorBiped(FeatureRendererContext<T, M> featureRendererContext_1, A bipedEntityModel_1, A bipedEntityModel_2, RenderContext context) {
        super(featureRendererContext_1, bipedEntityModel_1, bipedEntityModel_2);
        this.context = context;
    }

    @Override
    public void render(T livingEntity_1, float float_1, float float_2, float float_3, float float_4, float float_5, float float_6, float float_7) {
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

    private void renderCuboid(CustomJsonModel model, PlayerFeature feature, Cuboid cuboid, float scale) {
        for (IBone bone : model.getFeatureAttached(feature)) {
            boolean visible = cuboid.visible;
            cuboid.visible = bone.isVisible(context);
            if (cuboid.visible) {
                float x = cuboid.rotationPointX;
                float y = cuboid.rotationPointY;
                float z = cuboid.rotationPointZ;
                float yaw = cuboid.yaw;
                float pitch = cuboid.pitch;
                float roll = cuboid.roll;
                cuboid.rotationPointX = 0;
                cuboid.rotationPointY = 0;
                cuboid.rotationPointZ = 0;
                cuboid.yaw = 0;
                cuboid.pitch = 0;
                cuboid.roll = 0;

                GlStateManager.pushMatrix();
                GL11.glMultMatrixd(context.currentInvTransform.val);
                GL11.glMultMatrixd(model.getTransform(bone).val);
                cuboid.render(scale);
                GlStateManager.popMatrix();

                cuboid.rotationPointX = x;
                cuboid.rotationPointY = y;
                cuboid.rotationPointZ = z;
                cuboid.yaw = yaw;
                cuboid.pitch = pitch;
                cuboid.roll = roll;
            }

            cuboid.visible = visible;
        }
    }

    private void renderModel(T livingEntity_1, BipedEntityModel model, EquipmentSlot slot, CustomJsonModel cmodel, float scale) {
        switch (slot) {
            case HEAD:
                renderCuboid(cmodel, PlayerFeature.HELMET_HEAD, model.head, scale);
                renderCuboid(cmodel, PlayerFeature.HELMET_HEAD_OVERLAY, model.headwear, scale);
                break;
            case CHEST:
                renderCuboid(cmodel, PlayerFeature.CHESTPLATE_BODY, model.body, scale);
                renderCuboid(cmodel, PlayerFeature.CHESTPLATE_LEFT_ARM, model.leftArm, scale);
                renderCuboid(cmodel, PlayerFeature.CHESTPLATE_RIGHT_ARM, model.rightArm, scale);
                break;
            case LEGS:
                renderCuboid(cmodel, PlayerFeature.LEGGINGS_BODY, model.body, scale);
                renderCuboid(cmodel, PlayerFeature.LEGGINGS_LEFT_LEG, model.leftLeg, scale);
                renderCuboid(cmodel, PlayerFeature.LEGGINGS_RIGHT_LEG, model.rightLeg, scale);
                break;
            case FEET:
                renderCuboid(cmodel, PlayerFeature.BOOTS_LEFT_LEG, model.leftLeg, scale);
                renderCuboid(cmodel, PlayerFeature.BOOTS_RIGHT_LEG, model.rightLeg, scale);
                break;
        }
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
