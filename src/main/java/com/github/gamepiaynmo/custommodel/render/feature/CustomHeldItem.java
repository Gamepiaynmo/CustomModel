package com.github.gamepiaynmo.custommodel.render.feature;

import com.github.gamepiaynmo.custommodel.client.CustomModelClient;
import com.github.gamepiaynmo.custommodel.client.ModelPack;
import com.github.gamepiaynmo.custommodel.render.CustomJsonModel;
import com.github.gamepiaynmo.custommodel.render.model.IBone;
import com.github.gamepiaynmo.custommodel.util.Matrix4;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.feature.HeldItemFeatureRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.ModelWithArms;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import org.lwjgl.opengl.GL11;

public class CustomHeldItem<T extends AbstractClientPlayerEntity, M extends PlayerEntityModel<T>> extends HeldItemFeatureRenderer<T, M> {
    public CustomHeldItem(FeatureRendererContext featureRendererContext_1) {
        super(featureRendererContext_1);
    }

    private AbstractClientPlayerEntity playerEntity;
    private Matrix4 transLeft, transRight;
    private IBone left, right;

    @Override
    public void render(T livingEntity_1, float float_1, float float_2, float float_3, float float_4, float float_5, float float_6, float float_7) {
        playerEntity = livingEntity_1;
        boolean boolean_1 = livingEntity_1.getMainArm() == Arm.RIGHT;
        ItemStack itemStack_1 = boolean_1 ? livingEntity_1.getOffHandStack() : livingEntity_1.getMainHandStack();
        ItemStack itemStack_2 = boolean_1 ? livingEntity_1.getMainHandStack() : livingEntity_1.getOffHandStack();
        if (!itemStack_1.isEmpty() || !itemStack_2.isEmpty()) {
            GlStateManager.pushMatrix();
            if (this.getModel().isChild) {
                float float_8 = 0.5F;
                GlStateManager.translatef(0.0F, 0.75F, 0.0F);
                GlStateManager.scalef(0.5F, 0.5F, 0.5F);
            }

            ModelPack pack = CustomModelClient.getModelForPlayer(playerEntity);
            transLeft = transRight = null;
            left = right = null;
            if (pack != null) {
                CustomJsonModel model = pack.getModel();
                left = model.getBone("left_item");
                right = model.getBone("right_item");
                if (left != null && left.isVisible()) {
                    transLeft = model.getTransform(left).cpy();
                    transLeft.mulLeft(CustomModelClient.currentInvTransform);
                }

                if (right != null && right.isVisible()) {
                    transRight = model.getTransform(right).cpy();
                    transRight.mulLeft(CustomModelClient.currentInvTransform);
                }
            }

            if (left == null || transLeft != null)
                this.method_4192(livingEntity_1, itemStack_1, ModelTransformation.Type.THIRD_PERSON_LEFT_HAND, Arm.LEFT);
            if (right == null || transRight != null)
                this.method_4192(livingEntity_1, itemStack_2, ModelTransformation.Type.THIRD_PERSON_RIGHT_HAND, Arm.RIGHT);
            GlStateManager.popMatrix();
        }
    }

    private void method_4192(LivingEntity livingEntity_1, ItemStack itemStack_1, ModelTransformation.Type modelTransformation$Type_1, Arm arm_1) {
        if (!itemStack_1.isEmpty()) {
            GlStateManager.pushMatrix();

            boolean boolean_1 = arm_1 == Arm.LEFT;
            IBone bone = boolean_1 ? left : right;
            Matrix4 trans = boolean_1 ? transLeft : transRight;
            if (bone == null) {
                this.method_4193(arm_1);
                GlStateManager.translatef(-(float)(boolean_1 ? -1 : 1) / 16.0F, 0.625F, -0.125F);
            } else {
                GL11.glMultMatrixd(trans.val);
            }

            GlStateManager.rotatef(-90.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.rotatef(180.0F, 0.0F, 1.0F, 0.0F);
            MinecraftClient.getInstance().getFirstPersonRenderer().renderItemFromSide(livingEntity_1, itemStack_1, modelTransformation$Type_1, boolean_1);
            GlStateManager.popMatrix();
        }
    }
}
