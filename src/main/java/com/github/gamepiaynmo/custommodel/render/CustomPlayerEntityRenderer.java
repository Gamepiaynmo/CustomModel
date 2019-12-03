package com.github.gamepiaynmo.custommodel.render;

import com.github.gamepiaynmo.custommodel.client.CustomModelClient;
import com.github.gamepiaynmo.custommodel.client.ModelPack;
import com.github.gamepiaynmo.custommodel.util.Matrix4;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.PlayerModelPart;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Arm;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.UseAction;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class CustomPlayerEntityRenderer extends PlayerEntityRenderer {
    public CustomPlayerEntityRenderer(EntityRenderDispatcher entityRenderDispatcher) {
        super(entityRenderDispatcher);
    }

    public CustomPlayerEntityRenderer(EntityRenderDispatcher entityRenderDispatcher, boolean slim) {
        super(entityRenderDispatcher, slim);
    }

    private float partial;
    private Matrix4 transform;

    protected void method_4048_c(AbstractClientPlayerEntity abstractClientPlayerEntity_1, double partial) {
        double double_1 = MathHelper.lerp(partial, abstractClientPlayerEntity_1.prevX, abstractClientPlayerEntity_1.x);
        double double_2 = MathHelper.lerp(partial, abstractClientPlayerEntity_1.prevY, abstractClientPlayerEntity_1.y);
        double double_3 = MathHelper.lerp(partial, abstractClientPlayerEntity_1.prevZ, abstractClientPlayerEntity_1.z);

        if (abstractClientPlayerEntity_1.getPose() == EntityPose.SLEEPING) {
            Direction direction_1 = abstractClientPlayerEntity_1.getSleepingDirection();
            if (direction_1 != null) {
                float float_1 = abstractClientPlayerEntity_1.getEyeHeight(EntityPose.STANDING) - 0.1F;
                transform.translate(double_1 - direction_1.getOffsetX() * float_1, double_2, double_3 - direction_1.getOffsetZ() * float_1);
                return;
            }
        }

        transform.translate(double_1, double_2, double_3);
    }

    private static float method_18656_c(Direction direction_1) {
        switch(direction_1) {
            case SOUTH:
                return 90.0F;
            case WEST:
                return 0.0F;
            case NORTH:
                return 270.0F;
            case EAST:
                return 180.0F;
            default:
                return 0.0F;
        }
    }

    protected void setupTransforms_c(AbstractClientPlayerEntity abstractClientPlayerEntity_1, float float_1, float float_2, float float_3) {
        EntityPose entityPose_1 = abstractClientPlayerEntity_1.getPose();
        if (entityPose_1 != EntityPose.SLEEPING) {
            transform.rotate(0.0F, 1.0F, 0.0F, 180.0F - float_2);
        }

        if (abstractClientPlayerEntity_1.deathTime > 0) {
            float float_4 = ((float)abstractClientPlayerEntity_1.deathTime + float_3 - 1.0F) / 20.0F * 1.6F;
            float_4 = MathHelper.sqrt(float_4);
            if (float_4 > 1.0F) {
                float_4 = 1.0F;
            }

            transform.rotate(0.0F, 0.0F, 1.0F, float_4 * this.getLyingAngle(abstractClientPlayerEntity_1));
        } else if (abstractClientPlayerEntity_1.isUsingRiptide()) {
            transform.rotate(1.0F, 0.0F, 0.0F, -90.0F - abstractClientPlayerEntity_1.pitch);
            transform.rotate(0.0F, 1.0F, 0.0F, ((float)abstractClientPlayerEntity_1.age + float_3) * -75.0F);
        } else if (entityPose_1 == EntityPose.SLEEPING) {
            Direction direction_1 = abstractClientPlayerEntity_1.getSleepingDirection();
            transform.rotate(0.0F, 1.0F, 0.0F, direction_1 != null ? method_18656_c(direction_1) : float_2);
            transform.rotate(0.0F, 0.0F, 1.0F, this.getLyingAngle(abstractClientPlayerEntity_1));
            transform.rotate(0.0F, 1.0F, 0.0F, 270.0F);
        } else {
            String string_1 = Formatting.strip(abstractClientPlayerEntity_1.getName().getString());
            if (string_1 != null && ("Dinnerbone".equals(string_1) || "Grumm".equals(string_1)) && abstractClientPlayerEntity_1.isSkinOverlayVisible(PlayerModelPart.CAPE)) {
                transform.translate(0.0F, abstractClientPlayerEntity_1.getHeight() + 0.1F, 0.0F);
                transform.rotate(0.0F, 0.0F, 1.0F, 180.0F);
            }
        }

    }

    protected void method_4212_c(AbstractClientPlayerEntity abstractClientPlayerEntity_1, float float_1, float float_2, float float_3) {
        float float_4 = abstractClientPlayerEntity_1.method_6024(float_3);
        float float_7;
        float float_6;
        if (abstractClientPlayerEntity_1.isFallFlying()) {
            setupTransforms_c(abstractClientPlayerEntity_1, float_1, float_2, float_3);
            float_7 = (float)abstractClientPlayerEntity_1.method_6003() + float_3;
            float_6 = MathHelper.clamp(float_7 * float_7 / 100.0F, 0.0F, 1.0F);
            if (!abstractClientPlayerEntity_1.isUsingRiptide()) {
                transform.rotate(1.0F, 0.0F, 0.0F, float_6 * (-90.0F - abstractClientPlayerEntity_1.pitch));
            }

            Vec3d vec3d_1 = abstractClientPlayerEntity_1.getRotationVec(float_3);
            Vec3d vec3d_2 = abstractClientPlayerEntity_1.getVelocity();
            double double_1 = Entity.squaredHorizontalLength(vec3d_2);
            double double_2 = Entity.squaredHorizontalLength(vec3d_1);
            if (double_1 > 0.0D && double_2 > 0.0D) {
                double double_3 = (vec3d_2.x * vec3d_1.x + vec3d_2.z * vec3d_1.z) / (Math.sqrt(double_1) * Math.sqrt(double_2));
                double double_4 = vec3d_2.x * vec3d_1.z - vec3d_2.z * vec3d_1.x;
                transform.rotate(0.0F, 1.0F, 0.0F, (float)(Math.signum(double_4) * Math.acos(double_3)) * 180.0F / 3.1415927F);
            }
        } else if (float_4 > 0.0F) {
            setupTransforms_c(abstractClientPlayerEntity_1, float_1, float_2, float_3);
            float_7 = abstractClientPlayerEntity_1.isInsideWater() ? -90.0F - abstractClientPlayerEntity_1.pitch : -90.0F;
            float_6 = MathHelper.lerp(float_4, 0.0F, float_7);
            transform.rotate(1.0F, 0.0F, 0.0F, float_6);
            if (abstractClientPlayerEntity_1.isInSwimmingPose()) {
                transform.translate(0.0F, -1.0F, 0.3F);
            }
        } else {
            setupTransforms_c(abstractClientPlayerEntity_1, float_1, float_2, float_3);
        }

    }

    public float scaleAndTranslate_c(AbstractClientPlayerEntity playerEntity, float float_1) {
        transform.scale(-1.0F, -1.0F, 1.0F);
        transform.scale(0.9375F, 0.9375F, 0.9375F);
        transform.translate(0.0F, -1.501F, 0.0F);
        return 0.0625F;
    }

    public void tick(AbstractClientPlayerEntity playerEntity) {
        ModelPack model = CustomModelClient.getModelForPlayer(playerEntity);
        if (model != null) {
            this.model.handSwingProgress = this.getHandSwingProgress(playerEntity, 1);
            this.model.isRiding = playerEntity.hasVehicle();
            this.model.isChild = playerEntity.isBaby();

            this.partial = 1;
            CustomModelClient.currentParameter = calculateTransform(playerEntity);

            CustomModelClient.currentPlayer = playerEntity;
            CustomModelClient.currentRenderer = this;
            CustomModelClient.currentModel = getModel();
            CustomModelClient.currentJsonModel = model.getModel();

            model.getModel().tick(transform);
        }
    }

    public boolean disableSetModelPose;

    @Override
    public void render(AbstractClientPlayerEntity playerEntity, double offX, double offY, double offZ, float rotYaw, float partial) {
        if (!playerEntity.isMainPlayer() || this.renderManager.camera != null && this.renderManager.camera.getFocusedEntity() == playerEntity) {
            ModelPack model = CustomModelClient.getModelForPlayer(playerEntity);
            CustomModelClient.currentPlayer = playerEntity;
            CustomModelClient.currentRenderer = this;
            CustomModelClient.currentModel = getModel();

            disableSetModelPose = model != null;
            if (disableSetModelPose)
                this.setModelPose(playerEntity, model.getModel());
            this.partial = partial;

            GlStateManager.setProfile(GlStateManager.RenderMode.PLAYER_SKIN);
            super.render(playerEntity, offX, offY, offZ, rotYaw, partial);
            GlStateManager.unsetProfile(GlStateManager.RenderMode.PLAYER_SKIN);
        }
    }

    private RenderParameter calculateTransform(AbstractClientPlayerEntity playerEntity) {
        try {
            float yaw = MathHelper.lerpAngleDegrees(partial, playerEntity.field_6220, playerEntity.field_6283);
            float headYaw = MathHelper.lerpAngleDegrees(partial, playerEntity.prevHeadYaw, playerEntity.headYaw);
            float delta = headYaw - yaw;
            float float_8;
            if (playerEntity.hasVehicle() && playerEntity.getVehicle() instanceof LivingEntity) {
                LivingEntity livingEntity_2 = (LivingEntity) playerEntity.getVehicle();
                yaw = MathHelper.lerpAngleDegrees(partial, livingEntity_2.field_6220, livingEntity_2.field_6283);
                delta = headYaw - yaw;
                float_8 = MathHelper.wrapDegrees(delta);
                if (float_8 < -85.0F) {
                    float_8 = -85.0F;
                }

                if (float_8 >= 85.0F) {
                    float_8 = 85.0F;
                }

                yaw = headYaw - float_8;
                if (float_8 * float_8 > 2500.0F) {
                    yaw += float_8 * 0.2F;
                }

                delta = headYaw - yaw;
            }

            transform = new Matrix4();
            float float_7 = MathHelper.lerp(partial, playerEntity.prevPitch, playerEntity.pitch);
            this.method_4048_c(playerEntity, partial);
            float_8 = this.getAge(playerEntity, partial);
            this.method_4212_c(playerEntity, float_8, yaw, partial);
            float float_9 = this.scaleAndTranslate_c(playerEntity, partial);
            float float_10 = 0.0F;
            float float_11 = 0.0F;
            if (!playerEntity.hasVehicle() && playerEntity.isAlive()) {
                float_10 = MathHelper.lerp(partial, playerEntity.lastLimbDistance, playerEntity.limbDistance);
                float_11 = playerEntity.limbAngle - playerEntity.limbDistance * (1.0F - partial);
                if (playerEntity.isBaby()) {
                    float_11 *= 3.0F;
                }

                if (float_10 > 1.0F) {
                    float_10 = 1.0F;
                }
            }

            GlStateManager.enableAlphaTest();
            this.model.animateModel(playerEntity, float_11, float_10, partial);
            this.model.setAngles(playerEntity, float_11, float_10, float_8, delta, float_7, float_9);
            return new RenderParameter(float_11, float_10, float_8, delta, float_7, 0.0625f, partial);
        } catch (Exception ignored) {
            return null;
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

            if (model != null) {
                CustomModelClient.currentJsonModel = model.getModel();
                CustomModelClient.currentJsonModel.clearTransform();
                if (CustomModelClient.isRenderingInventory) {
                    EntityParameter currentParameter = new EntityParameter(playerEntity);
                    CustomModelClient.inventoryEntityParameter.apply(playerEntity);
                    CustomModelClient.currentParameter = calculateTransform(playerEntity);
                    CustomModelClient.currentJsonModel.update(this.transform);
                    currentParameter.apply(playerEntity);
                }

                CustomModelClient.currentParameter = calculateTransform(playerEntity);
                CustomModelClient.currentJsonModel.render(transform);
            }

            if (boolean_2) {
                GlStateManager.unsetProfile(GlStateManager.RenderMode.TRANSPARENT_MODEL);
            }
        }
        this.setModelPose(playerEntity);

    }

    private void setModelPose(AbstractClientPlayerEntity abstractClientPlayerEntity_1) {
        PlayerEntityModel<AbstractClientPlayerEntity> playerEntityModel_1 = this.getModel();
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
        PlayerEntityModel<AbstractClientPlayerEntity> playerEntityModel_1 = this.getModel();
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
