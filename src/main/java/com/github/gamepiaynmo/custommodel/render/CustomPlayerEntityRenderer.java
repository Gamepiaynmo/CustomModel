package com.github.gamepiaynmo.custommodel.render;

import com.github.gamepiaynmo.custommodel.client.CustomModelClient;
import com.github.gamepiaynmo.custommodel.client.ModelPack;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.PlayerModelPart;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.UseAction;
import net.minecraft.util.math.MathHelper;

public class CustomPlayerEntityRenderer extends PlayerEntityRenderer {
    public CustomPlayerEntityRenderer(EntityRenderDispatcher entityRenderDispatcher_1) {
        super(entityRenderDispatcher_1);
    }

    public CustomPlayerEntityRenderer(EntityRenderDispatcher entityRenderDispatcher_1, boolean boolean_1) {
        super(entityRenderDispatcher_1, boolean_1);
    }

    private float partial;

    public void tick(AbstractClientPlayerEntity playerEntity) {
        ModelPack model = CustomModelClient.getModelForPlayer(playerEntity);
        if (model != null) {
            this.model.handSwingProgress = this.getHandSwingProgress(playerEntity, 1);
            this.model.isRiding = playerEntity.hasVehicle();
            this.model.isChild = playerEntity.isBaby();

            try {
                float float_3 = playerEntity.field_6283;
                float float_4 = playerEntity.headYaw;
                float float_5 = float_4 - float_3;
                float float_8;
                if (playerEntity.hasVehicle() && playerEntity.getVehicle() instanceof LivingEntity) {
                    LivingEntity livingEntity_2 = (LivingEntity)playerEntity.getVehicle();
                    float_3 = livingEntity_2.field_6283;
                    float_5 = float_4 - float_3;
                    float_8 = MathHelper.wrapDegrees(float_5);
                    if (float_8 < -85.0F) {
                        float_8 = -85.0F;
                    }

                    if (float_8 >= 85.0F) {
                        float_8 = 85.0F;
                    }

                    float_3 = float_4 - float_8;
                    if (float_8 * float_8 > 2500.0F) {
                        float_3 += float_8 * 0.2F;
                    }

                    float_5 = float_4 - float_3;
                }

                float float_7 = playerEntity.pitch;
                float_8 = this.getAge(playerEntity, 1);
                float float_10 = 0.0F;
                float float_11 = 0.0F;
                if (!playerEntity.hasVehicle() && playerEntity.isAlive()) {
                    float_10 = playerEntity.limbDistance;
                    float_11 = playerEntity.limbAngle;
                    if (playerEntity.isBaby()) {
                        float_11 *= 3.0F;
                    }

                    if (float_10 > 1.0F) {
                        float_10 = 1.0F;
                    }
                }

                GlStateManager.enableAlphaTest();
                this.model.animateModel(playerEntity, float_11, float_10, 1);
                this.model.setAngles(playerEntity, float_11, float_10, float_8, float_5, float_7, 0.0625f);
            } catch (Exception var19) {
            }

            model.getModel().tick(playerEntity, getModel());
        }
    }

    @Override
    public void render(AbstractClientPlayerEntity playerEntity, double offX, double offY, double offZ, float rotYaw, float partial) {
        if (!playerEntity.isMainPlayer() || this.renderManager.camera != null && this.renderManager.camera.getFocusedEntity() == playerEntity) {
            double newOffY = offY;
            if (playerEntity.isInSneakingPose()) {
                newOffY = offY - 0.125D;
            }

            ModelPack model = CustomModelClient.getModelForPlayer(playerEntity);

            if (model != null)
                this.setModelPose(playerEntity, model.getModel());
            else this.setModelPose(playerEntity);
            this.partial = partial;

            GlStateManager.setProfile(GlStateManager.RenderMode.PLAYER_SKIN);
            super.render(playerEntity, offX, newOffY, offZ, rotYaw, partial);
            GlStateManager.unsetProfile(GlStateManager.RenderMode.PLAYER_SKIN);
        }
    }

    @Override
    protected void render(AbstractClientPlayerEntity playerEntity, float float_1, float float_2, float float_3, float float_4, float float_5, float float_6) {
        boolean boolean_1 = this.method_4056(playerEntity);
        boolean boolean_2 = !boolean_1 && !playerEntity.canSeePlayer(MinecraftClient.getInstance().player);
        if (boolean_1 || boolean_2) {
            if (!this.bindEntityTexture(playerEntity)) {
                return;
            }

            if (boolean_2) {
                GlStateManager.setProfile(GlStateManager.RenderMode.TRANSPARENT_MODEL);
            }

            this.model.render(playerEntity, float_1, float_2, float_3, float_4, float_5, float_6);

            ModelPack model = CustomModelClient.getModelForPlayer(playerEntity);
            if (model != null)
                model.getModel().render(playerEntity, this, getModel(), float_6, partial);

            if (boolean_2) {
                GlStateManager.unsetProfile(GlStateManager.RenderMode.TRANSPARENT_MODEL);
            }
        }

    }

    private void setModelPose(AbstractClientPlayerEntity abstractClientPlayerEntity_1) {
        PlayerEntityModel<AbstractClientPlayerEntity> playerEntityModel_1 = (PlayerEntityModel)this.getModel();
        if (abstractClientPlayerEntity_1.isSpectator()) {
            playerEntityModel_1.setVisible(false);
            playerEntityModel_1.head.visible = true;
            playerEntityModel_1.headwear.visible = true;
        } else {
            ItemStack itemStack_1 = abstractClientPlayerEntity_1.getMainHandStack();
            ItemStack itemStack_2 = abstractClientPlayerEntity_1.getOffHandStack();
            playerEntityModel_1.setVisible(true);
            playerEntityModel_1.headwear.visible = abstractClientPlayerEntity_1.isSkinOverlayVisible(PlayerModelPart.HAT);
            playerEntityModel_1.bodyOverlay.visible = abstractClientPlayerEntity_1.isSkinOverlayVisible(PlayerModelPart.JACKET);
            playerEntityModel_1.leftLegOverlay.visible = abstractClientPlayerEntity_1.isSkinOverlayVisible(PlayerModelPart.LEFT_PANTS_LEG);
            playerEntityModel_1.rightLegOverlay.visible = abstractClientPlayerEntity_1.isSkinOverlayVisible(PlayerModelPart.RIGHT_PANTS_LEG);
            playerEntityModel_1.leftArmOverlay.visible = abstractClientPlayerEntity_1.isSkinOverlayVisible(PlayerModelPart.LEFT_SLEEVE);
            playerEntityModel_1.rightArmOverlay.visible = abstractClientPlayerEntity_1.isSkinOverlayVisible(PlayerModelPart.RIGHT_SLEEVE);
            playerEntityModel_1.isSneaking = abstractClientPlayerEntity_1.isInSneakingPose();
            BipedEntityModel.ArmPose bipedEntityModel$ArmPose_1 = this.method_4210(abstractClientPlayerEntity_1, itemStack_1, itemStack_2, Hand.MAIN_HAND);
            BipedEntityModel.ArmPose bipedEntityModel$ArmPose_2 = this.method_4210(abstractClientPlayerEntity_1, itemStack_1, itemStack_2, Hand.OFF_HAND);
            if (abstractClientPlayerEntity_1.getMainArm() == Arm.RIGHT) {
                playerEntityModel_1.rightArmPose = bipedEntityModel$ArmPose_1;
                playerEntityModel_1.leftArmPose = bipedEntityModel$ArmPose_2;
            } else {
                playerEntityModel_1.rightArmPose = bipedEntityModel$ArmPose_2;
                playerEntityModel_1.leftArmPose = bipedEntityModel$ArmPose_1;
            }
        }

    }

    private void setModelPose(AbstractClientPlayerEntity abstractClientPlayerEntity_1, CustomJsonModel model) {
        PlayerEntityModel<AbstractClientPlayerEntity> playerEntityModel_1 = (PlayerEntityModel)this.getModel();
        if (abstractClientPlayerEntity_1.isSpectator()) {
            playerEntityModel_1.setVisible(false);
            model.setVisible(false);
            playerEntityModel_1.head.visible = true;
            model.setVisible(PlayerBones.HEAD, true);
            playerEntityModel_1.headwear.visible = true;
            model.setVisible(PlayerBones.HEAD_OVERLAY, true);
        } else {
            ItemStack itemStack_1 = abstractClientPlayerEntity_1.getMainHandStack();
            ItemStack itemStack_2 = abstractClientPlayerEntity_1.getOffHandStack();
            playerEntityModel_1.setVisible(true);
            model.setVisible(true);
            playerEntityModel_1.headwear.visible = abstractClientPlayerEntity_1.isSkinOverlayVisible(PlayerModelPart.HAT);
            model.setVisible(PlayerBones.HEAD_OVERLAY, playerEntityModel_1.headwear.visible);
            playerEntityModel_1.bodyOverlay.visible = abstractClientPlayerEntity_1.isSkinOverlayVisible(PlayerModelPart.JACKET);
            model.setVisible(PlayerBones.BODY_OVERLAY, playerEntityModel_1.bodyOverlay.visible);
            playerEntityModel_1.leftLegOverlay.visible = abstractClientPlayerEntity_1.isSkinOverlayVisible(PlayerModelPart.LEFT_PANTS_LEG);
            model.setVisible(PlayerBones.LEFT_LEG_OVERLAY, playerEntityModel_1.leftLegOverlay.visible);
            playerEntityModel_1.rightLegOverlay.visible = abstractClientPlayerEntity_1.isSkinOverlayVisible(PlayerModelPart.RIGHT_PANTS_LEG);
            model.setVisible(PlayerBones.RIGHT_LEG_OVERLAY, playerEntityModel_1.rightLegOverlay.visible);
            playerEntityModel_1.leftArmOverlay.visible = abstractClientPlayerEntity_1.isSkinOverlayVisible(PlayerModelPart.LEFT_SLEEVE);
            model.setVisible(PlayerBones.LEFT_ARM_OVERLAY, playerEntityModel_1.leftArmOverlay.visible);
            playerEntityModel_1.rightArmOverlay.visible = abstractClientPlayerEntity_1.isSkinOverlayVisible(PlayerModelPart.RIGHT_SLEEVE);
            model.setVisible(PlayerBones.RIGHT_ARM_OVERLAY, playerEntityModel_1.rightArmOverlay.visible);

            playerEntityModel_1.isSneaking = abstractClientPlayerEntity_1.isInSneakingPose();
            BipedEntityModel.ArmPose bipedEntityModel$ArmPose_1 = this.method_4210(abstractClientPlayerEntity_1, itemStack_1, itemStack_2, Hand.MAIN_HAND);
            BipedEntityModel.ArmPose bipedEntityModel$ArmPose_2 = this.method_4210(abstractClientPlayerEntity_1, itemStack_1, itemStack_2, Hand.OFF_HAND);
            if (abstractClientPlayerEntity_1.getMainArm() == Arm.RIGHT) {
                playerEntityModel_1.rightArmPose = bipedEntityModel$ArmPose_1;
                playerEntityModel_1.leftArmPose = bipedEntityModel$ArmPose_2;
            } else {
                playerEntityModel_1.rightArmPose = bipedEntityModel$ArmPose_2;
                playerEntityModel_1.leftArmPose = bipedEntityModel$ArmPose_1;
            }
        }

        for (PlayerBones bone : model.getHiddenBones()) {
            bone.getCuboid(playerEntityModel_1).visible = false;
        }
    }

    private BipedEntityModel.ArmPose method_4210(AbstractClientPlayerEntity abstractClientPlayerEntity_1, ItemStack itemStack_1, ItemStack itemStack_2, Hand hand_1) {
        BipedEntityModel.ArmPose bipedEntityModel$ArmPose_1 = BipedEntityModel.ArmPose.EMPTY;
        ItemStack itemStack_3 = hand_1 == Hand.MAIN_HAND ? itemStack_1 : itemStack_2;
        if (!itemStack_3.isEmpty()) {
            bipedEntityModel$ArmPose_1 = BipedEntityModel.ArmPose.ITEM;
            if (abstractClientPlayerEntity_1.getItemUseTimeLeft() > 0) {
                UseAction useAction_1 = itemStack_3.getUseAction();
                if (useAction_1 == UseAction.BLOCK) {
                    bipedEntityModel$ArmPose_1 = BipedEntityModel.ArmPose.BLOCK;
                } else if (useAction_1 == UseAction.BOW) {
                    bipedEntityModel$ArmPose_1 = BipedEntityModel.ArmPose.BOW_AND_ARROW;
                } else if (useAction_1 == UseAction.SPEAR) {
                    bipedEntityModel$ArmPose_1 = BipedEntityModel.ArmPose.THROW_SPEAR;
                } else if (useAction_1 == UseAction.CROSSBOW && hand_1 == abstractClientPlayerEntity_1.getActiveHand()) {
                    bipedEntityModel$ArmPose_1 = BipedEntityModel.ArmPose.CROSSBOW_CHARGE;
                }
            } else {
                boolean boolean_1 = itemStack_1.getItem() == Items.CROSSBOW;
                boolean boolean_2 = CrossbowItem.isCharged(itemStack_1);
                boolean boolean_3 = itemStack_2.getItem() == Items.CROSSBOW;
                boolean boolean_4 = CrossbowItem.isCharged(itemStack_2);
                if (boolean_1 && boolean_2) {
                    bipedEntityModel$ArmPose_1 = BipedEntityModel.ArmPose.CROSSBOW_HOLD;
                }

                if (boolean_3 && boolean_4 && itemStack_1.getItem().getUseAction(itemStack_1) == UseAction.NONE) {
                    bipedEntityModel$ArmPose_1 = BipedEntityModel.ArmPose.CROSSBOW_HOLD;
                }
            }
        }

        return bipedEntityModel$ArmPose_1;
    }
}
