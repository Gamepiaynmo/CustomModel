package com.github.gamepiaynmo.custommodel.mixin;

import com.github.gamepiaynmo.custommodel.client.CustomModelClient;
import com.github.gamepiaynmo.custommodel.client.ModelPack;
import com.github.gamepiaynmo.custommodel.render.*;
import com.github.gamepiaynmo.custommodel.util.Matrix4;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.PlayerModelPart;
import net.minecraft.client.render.entity.feature.*;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.*;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Function;
import java.util.function.Supplier;

@Mixin(PlayerEntityRenderer.class)
public abstract class MixinPlayerEntityRenderer extends LivingEntityRenderer<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> implements ICustomPlayerRenderer {
    public MixinPlayerEntityRenderer(EntityRenderDispatcher entityRenderDispatcher_1, PlayerEntityModel<AbstractClientPlayerEntity> entityModel_1, float float_1) {
        super(entityRenderDispatcher_1, entityModel_1, float_1);
    }

    private boolean slim;

    @Inject(method = "<init>(Lnet/minecraft/client/render/entity/EntityRenderDispatcher;Z)V", at = @At(value = "RETURN"))
    public void addFeatures(EntityRenderDispatcher dispatcher, boolean slim, CallbackInfo info) {
        this.slim = slim;
        for (int i = 0; i < this.features.size(); i++) {
            FeatureRenderer feature = this.features.get(i);
//            if (feature instanceof ArmorBipedFeatureRenderer)
//                feature = new CustomArmorBiped<>(this, new BipedEntityModel(0.5F), new BipedEntityModel(1.0F));
//            if (feature instanceof HeldItemFeatureRenderer)
//                feature = new CustomHeldItem<>(this);
//            if (feature instanceof StuckArrowsFeatureRenderer)
//                feature = new CustomStuckArrows<>(this);
//            if (feature instanceof CapeFeatureRenderer)
//                feature = new CustomCape(this);
//            if (feature instanceof HeadFeatureRenderer)
//                feature = new CustomHead<>(this);
//            if (feature instanceof ElytraFeatureRenderer)
//                feature = new CustomElytra<>(this);
//            if (feature instanceof ShoulderParrotFeatureRenderer)
//                feature = new CustomShoulderParrot<>(this);
            this.features.set(i, feature);
        }
    }

    public boolean disableSetModelPose;

    @Inject(method = "setModelPose(Lnet/minecraft/client/network/AbstractClientPlayerEntity;)V", at = @At("HEAD"), cancellable = true)
    private void setModelPose(AbstractClientPlayerEntity playerEntity, CallbackInfo info) {
        if (disableSetModelPose)
            info.cancel();
    }

    private Function<Identifier, VertexConsumer> getVertexConsumer(AbstractClientPlayerEntity playerEntity, VertexConsumerProvider vertexConsumerProvider) {
        boolean bl2 = this.method_4056(playerEntity, false);
        boolean bl3 = !bl2 && !playerEntity.canSeePlayer(MinecraftClient.getInstance().player);

        return (identifier) -> {
            if (bl3) {
                return vertexConsumerProvider.getBuffer(RenderLayer.getEntityTranslucent(identifier));
            } else if (bl2) {
                return vertexConsumerProvider.getBuffer(this.model.getLayer(identifier));
            } else {
                return vertexConsumerProvider.getBuffer(RenderLayer.getOutline(identifier));
            }
        };
    }

    @Override
    public void renderCustom(AbstractClientPlayerEntity playerEntity, float yaw, float partial, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int light) {
        boolean bl2 = this.method_4056(playerEntity, false);
        boolean bl3 = !bl2 && !playerEntity.canSeePlayer(MinecraftClient.getInstance().player);
        Function<Identifier, VertexConsumer> vertexConsumer = getVertexConsumer(playerEntity, vertexConsumerProvider);

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
            CustomModelClient.currentInvTransform = transform.cpy().inv();

            model.getModel().updateSkeleton();
            int r = getOverlay(playerEntity, this.getWhiteOverlayProgress(playerEntity, partial));
            CustomModelClient.currentJsonModel.render(transform, matrixStack, vertexConsumer, vertexConsumerProvider, light, r);
            resetSkeleton();
        }

        this.setModelPose_c(playerEntity);
    }

    @Inject(method = "renderRightArm", at = @At("HEAD"), cancellable = true)
    public void renderRightArm(MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, AbstractClientPlayerEntity abstractClientPlayerEntity, CallbackInfo info) {
        ModelPack pack = CustomModelClient.getModelForPlayer(abstractClientPlayerEntity);
        if (pack != null && pack.getModel().getFirstPersonList(Arm.RIGHT) != null) {
            CustomModelClient.isRenderingFirstPerson = true;
            CustomJsonModel model = pack.getModel();
            partial = CustomModelClient.getPartial();
            CustomModelClient.currentJsonModel = model;
            CustomModelClient.currentModel = getModel();
            CustomModelClient.currentParameter = calculateTransform(abstractClientPlayerEntity);
            CustomModelClient.currentPlayer = abstractClientPlayerEntity;
            CustomModelClient.currentRenderer = (PlayerEntityRenderer) (Object) this;

            PlayerEntityModel<AbstractClientPlayerEntity> playerEntityModel = getModel();

            playerEntityModel.isSneaking = false;
            playerEntityModel.handSwingProgress = 0.0F;
            playerEntityModel.field_3396 = 0.0F;
            playerEntityModel.setAngles(abstractClientPlayerEntity, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
            playerEntityModel.rightArm.pitch = 0;
            playerEntityModel.rightSleeve.pitch = 0;

            model.clearTransform();
            model.update(this.transform);

            if (!model.isHidden(PlayerBone.RIGHT_ARM))
                playerEntityModel.rightArm.render(matrixStack, vertexConsumerProvider.getBuffer(RenderLayer.getEntitySolid(abstractClientPlayerEntity.getSkinTexture())), i, OverlayTexture.DEFAULT_UV);
            if (!model.isHidden(PlayerBone.RIGHT_ARM_OVERLAY))
                playerEntityModel.rightSleeve.render(matrixStack, vertexConsumerProvider.getBuffer(RenderLayer.getEntityTranslucent(abstractClientPlayerEntity.getSkinTexture())), i, OverlayTexture.DEFAULT_UV);
            int r = getOverlay(abstractClientPlayerEntity, this.getWhiteOverlayProgress(abstractClientPlayerEntity, partial));
            Function<Identifier, VertexConsumer> vertexConsumer = getVertexConsumer(abstractClientPlayerEntity, vertexConsumerProvider);
            model.renderArm(model.getTransform(PlayerBone.RIGHT_ARM.getBone()), Arm.RIGHT, matrixStack, vertexConsumer, vertexConsumerProvider, i, r);

            CustomModelClient.isRenderingFirstPerson = false;
            info.cancel();
        }
    }

    @Inject(method = "renderLeftArm", at = @At("HEAD"), cancellable = true)
    public void renderLeftArm(MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, AbstractClientPlayerEntity abstractClientPlayerEntity, CallbackInfo info) {
        ModelPack pack = CustomModelClient.getModelForPlayer(abstractClientPlayerEntity);
        if (pack != null && pack.getModel().getFirstPersonList(Arm.LEFT) != null) {
            CustomModelClient.isRenderingFirstPerson = true;
            CustomJsonModel model = pack.getModel();
            partial = CustomModelClient.getPartial();
            CustomModelClient.currentJsonModel = model;
            CustomModelClient.currentModel = getModel();
            CustomModelClient.currentParameter = calculateTransform(abstractClientPlayerEntity);
            CustomModelClient.currentPlayer = abstractClientPlayerEntity;
            CustomModelClient.currentRenderer = (PlayerEntityRenderer) (Object) this;

            PlayerEntityModel<AbstractClientPlayerEntity> playerEntityModel = getModel();

            playerEntityModel.isSneaking = false;
            playerEntityModel.handSwingProgress = 0.0F;
            playerEntityModel.field_3396 = 0.0F;
            playerEntityModel.setAngles(abstractClientPlayerEntity, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
            playerEntityModel.leftArm.pitch = 0.0F;
            playerEntityModel.leftSleeve.pitch = 0.0F;

            model.clearTransform();
            model.update(this.transform);

            if (!model.isHidden(PlayerBone.LEFT_ARM))
                playerEntityModel.leftArm.render(matrixStack, vertexConsumerProvider.getBuffer(RenderLayer.getEntityTranslucent(abstractClientPlayerEntity.getSkinTexture())), i, OverlayTexture.DEFAULT_UV);
            if (!model.isHidden(PlayerBone.LEFT_ARM_OVERLAY))
                playerEntityModel.leftSleeve.render(matrixStack, vertexConsumerProvider.getBuffer(RenderLayer.getEntityTranslucent(abstractClientPlayerEntity.getSkinTexture())), i, OverlayTexture.DEFAULT_UV);
            int r = getOverlay(abstractClientPlayerEntity, this.getWhiteOverlayProgress(abstractClientPlayerEntity, partial));
            Function<Identifier, VertexConsumer> vertexConsumer = getVertexConsumer(abstractClientPlayerEntity, vertexConsumerProvider);
            model.renderArm(model.getTransform(PlayerBone.LEFT_ARM.getBone()), Arm.LEFT, matrixStack, vertexConsumer, vertexConsumerProvider, i, r);

            CustomModelClient.isRenderingFirstPerson = false;
            info.cancel();
        }
    }

    @Override
    public void tick(AbstractClientPlayerEntity playerEntity) {
        ModelPack model = CustomModelClient.getModelForPlayer(playerEntity);
        if (model != null) {
            this.model.handSwingProgress = this.getHandSwingProgress(playerEntity, 1);
            this.model.riding = playerEntity.hasVehicle();
            this.model.child = playerEntity.isBaby();

            this.partial = 1;
            CustomModelClient.currentModel = getModel();
            CustomModelClient.currentParameter = calculateTransform(playerEntity);

            CustomModelClient.currentPlayer = playerEntity;
            CustomModelClient.currentRenderer = (PlayerEntityRenderer) (Object) this;
            CustomModelClient.currentJsonModel = model.getModel();
            CustomModelClient.currentInvTransform = transform.cpy().inv();

            model.getModel().updateSkeleton();
            model.getModel().tick(transform);
            resetSkeleton();
        }
    }

    @Inject(method = "render", at = @At(value = "HEAD"))
    public void render(AbstractClientPlayerEntity abstractClientPlayerEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo info) {
        ModelPack model = CustomModelClient.getModelForPlayer(abstractClientPlayerEntity);
        CustomModelClient.currentPlayer = abstractClientPlayerEntity;
        CustomModelClient.currentRenderer = (PlayerEntityRenderer) (Object) this;
        CustomModelClient.currentModel = getModel();

        disableSetModelPose = model != null;
        if (disableSetModelPose) {
            this.setModelPose(abstractClientPlayerEntity, model.getModel());
        }
        this.partial = g;
    }

    public void resetSkeleton() {
        PlayerEntityModel model = CustomModelClient.currentModel;
        model.head.setPivot(0.0F, 0.0F, 0.0F);
        model.helmet.setPivot(0.0F, 0.0F, 0.0F);
        model.torso.setPivot(0.0F, 0.0F, 0.0F);
        model.rightArm.setPivot(-5.0F, 2.0F, 0.0F);
        model.leftArm.setPivot(5.0F, 2.0F, 0.0F);
        model.rightLeg.setPivot(-1.9F, 12.0F, 0.0F);
        model.leftLeg.setPivot(1.9F, 12.0F, 0.0F);

        if (slim) {
            model.leftArm.setPivot(5.0F, 2.5F, 0.0F);
            model.rightArm.setPivot(-5.0F, 2.5F, 0.0F);
            model.leftSleeve.setPivot(5.0F, 2.5F, 0.0F);
            model.rightSleeve.setPivot(-5.0F, 2.5F, 10.0F);
        } else {
            model.leftArm.setPivot(5.0F, 2.0F, 0.0F);
            model.leftSleeve.setPivot(5.0F, 2.0F, 0.0F);
            model.rightSleeve.setPivot(-5.0F, 2.0F, 10.0F);
        }

        model.leftPantLeg.setPivot(1.9F, 12.0F, 0.0F);
        model.rightPantLeg.setPivot(-1.9F, 12.0F, 0.0F);
        model.jacket.setPivot(0.0F, 0.0F, 0.0F);
    }

    private float partial;
    private Matrix4 transform;

    private void method_4048_c(AbstractClientPlayerEntity abstractClientPlayerEntity_1, double partial) {
        double double_1 = MathHelper.lerp(partial, abstractClientPlayerEntity_1.prevX, abstractClientPlayerEntity_1.getX());
        double double_2 = MathHelper.lerp(partial, abstractClientPlayerEntity_1.prevY, abstractClientPlayerEntity_1.getY());
        double double_3 = MathHelper.lerp(partial, abstractClientPlayerEntity_1.prevZ, abstractClientPlayerEntity_1.getZ());

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

    // copied
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

    private void setupTransforms_c(AbstractClientPlayerEntity abstractClientPlayerEntity_1, float float_1, float float_2, float float_3) {
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
            if (string_1 != null && ("Dinnerbone".equals(string_1) || "Grumm".equals(string_1)) && abstractClientPlayerEntity_1.isPartVisible(PlayerModelPart.CAPE)) {
                transform.translate(0.0F, abstractClientPlayerEntity_1.getHeight() + 0.1F, 0.0F);
                transform.rotate(0.0F, 0.0F, 1.0F, 180.0F);
            }
        }

    }

    private void method_4212_c(AbstractClientPlayerEntity abstractClientPlayerEntity_1, float float_1, float float_2, float float_3) {
        float float_4 = abstractClientPlayerEntity_1.getLeaningPitch(float_3);
        float float_7;
        float float_6;
        if (abstractClientPlayerEntity_1.isFallFlying()) {
            setupTransforms_c(abstractClientPlayerEntity_1, float_1, float_2, float_3);
            float_7 = (float)abstractClientPlayerEntity_1.getRoll() + float_3;
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
                float degrees = (float)(Math.signum(double_4) * Math.acos(double_3)) * 180.0F / 3.1415927F;
                if (!Float.isNaN(degrees))
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

    private float scaleAndTranslate_c(AbstractClientPlayerEntity playerEntity, float float_1) {
        transform.scale(-1.0F, -1.0F, 1.0F);
        transform.scale(0.9375F, 0.9375F, 0.9375F);
        transform.translate(0.0F, -1.501F, 0.0F);
        return 0.0625F;
    }

    private RenderParameter calculateTransform(AbstractClientPlayerEntity livingEntity) {
        try {
            float g = this.partial;
            transform = new Matrix4();
            this.model.handSwingProgress = this.getHandSwingProgress(livingEntity, g);
            this.model.riding = livingEntity.hasVehicle();
            this.model.child = livingEntity.isBaby();
            float h = MathHelper.lerpAngleDegrees(g, livingEntity.prevBodyYaw, livingEntity.bodyYaw);
            float j = MathHelper.lerpAngleDegrees(g, livingEntity.prevHeadYaw, livingEntity.headYaw);
            float k = j - h;
            float o;
            if (livingEntity.hasVehicle() && livingEntity.getVehicle() instanceof LivingEntity) {
                LivingEntity livingEntity2 = (LivingEntity)livingEntity.getVehicle();
                h = MathHelper.lerpAngleDegrees(g, livingEntity2.prevBodyYaw, livingEntity2.bodyYaw);
                k = j - h;
                o = MathHelper.wrapDegrees(k);
                if (o < -85.0F) {
                    o = -85.0F;
                }

                if (o >= 85.0F) {
                    o = 85.0F;
                }

                h = j - o;
                if (o * o > 2500.0F) {
                    h += o * 0.2F;
                }

                k = j - h;
            }

            float m = MathHelper.lerp(g, livingEntity.prevPitch, livingEntity.pitch);
            float p;
            if (livingEntity.getPose() == EntityPose.SLEEPING) {
                Direction direction = livingEntity.getSleepingDirection();
                if (direction != null) {
                    p = livingEntity.getEyeHeight(EntityPose.STANDING) - 0.1F;
                    transform.translate((double)((float)(-direction.getOffsetX()) * p), 0.0D, (double)((float)(-direction.getOffsetZ()) * p));
                }
            }

            o = this.getCustomAngle(livingEntity, g);
            this.setupTransforms_c(livingEntity, o, h, g);
            transform.scale(-1.0F, -1.0F, 1.0F);
            transform.scale(0.9375F, 0.9375F, 0.9375F);
            transform.translate(0.0D, -1.5010000467300415D, 0.0D);
            p = 0.0F;
            float q = 0.0F;
            if (!livingEntity.hasVehicle() && livingEntity.isAlive()) {
                p = MathHelper.lerp(g, livingEntity.lastLimbDistance, livingEntity.limbDistance);
                q = livingEntity.limbAngle - livingEntity.limbDistance * (1.0F - g);
                if (livingEntity.isBaby()) {
                    q *= 3.0F;
                }

                if (p > 1.0F) {
                    p = 1.0F;
                }
            }

            this.model.animateModel(livingEntity, q, p, g);
            boolean bl = livingEntity.isGlowing();
            boolean bl2 = this.method_4056(livingEntity, false);
            boolean bl3 = !bl2 && !livingEntity.canSeePlayer(MinecraftClient.getInstance().player);
            this.model.setAngles(livingEntity, q, p, o, k, m);
            return new RenderParameter(q, p, o, k, m, 0.0625f, partial);
        } catch (Exception ignored) {
            return null;
        }
    }

    // copied
    private void setModelPose_c(AbstractClientPlayerEntity abstractClientPlayerEntity_1) {
        PlayerEntityModel<AbstractClientPlayerEntity> playerEntityModel_1 = this.getModel();
        if (abstractClientPlayerEntity_1.isSpectator()) {
            playerEntityModel_1.setVisible(false);
            playerEntityModel_1.head.visible = true;
            playerEntityModel_1.helmet.visible = true;
        } else {
            ItemStack itemStack_1 = abstractClientPlayerEntity_1.getMainHandStack();
            ItemStack itemStack_2 = abstractClientPlayerEntity_1.getOffHandStack();
            playerEntityModel_1.setVisible(true);
            playerEntityModel_1.helmet.visible = abstractClientPlayerEntity_1.isPartVisible(PlayerModelPart.HAT);
            playerEntityModel_1.jacket.visible = abstractClientPlayerEntity_1.isPartVisible(PlayerModelPart.JACKET);
            playerEntityModel_1.leftPantLeg.visible = abstractClientPlayerEntity_1.isPartVisible(PlayerModelPart.LEFT_PANTS_LEG);
            playerEntityModel_1.rightPantLeg.visible = abstractClientPlayerEntity_1.isPartVisible(PlayerModelPart.RIGHT_PANTS_LEG);
            playerEntityModel_1.leftSleeve.visible = abstractClientPlayerEntity_1.isPartVisible(PlayerModelPart.LEFT_SLEEVE);
            playerEntityModel_1.rightSleeve.visible = abstractClientPlayerEntity_1.isPartVisible(PlayerModelPart.RIGHT_SLEEVE);
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
            model.setVisible(PlayerBone.HEAD, true);
            playerEntityModel_1.helmet.visible = true;
            model.setVisible(PlayerBone.HEAD_OVERLAY, true);
        } else {
            ItemStack itemStack_1 = abstractClientPlayerEntity_1.getMainHandStack();
            ItemStack itemStack_2 = abstractClientPlayerEntity_1.getOffHandStack();
            playerEntityModel_1.setVisible(true);
            model.setVisible(true);
            playerEntityModel_1.helmet.visible = abstractClientPlayerEntity_1.isPartVisible(PlayerModelPart.HAT);
            model.setVisible(PlayerBone.HEAD_OVERLAY, playerEntityModel_1.helmet.visible);
            playerEntityModel_1.jacket.visible = abstractClientPlayerEntity_1.isPartVisible(PlayerModelPart.JACKET);
            model.setVisible(PlayerBone.BODY_OVERLAY, playerEntityModel_1.jacket.visible);
            playerEntityModel_1.leftPantLeg.visible = abstractClientPlayerEntity_1.isPartVisible(PlayerModelPart.LEFT_PANTS_LEG);
            model.setVisible(PlayerBone.LEFT_LEG_OVERLAY, playerEntityModel_1.leftPantLeg.visible);
            playerEntityModel_1.rightPantLeg.visible = abstractClientPlayerEntity_1.isPartVisible(PlayerModelPart.RIGHT_PANTS_LEG);
            model.setVisible(PlayerBone.RIGHT_LEG_OVERLAY, playerEntityModel_1.rightPantLeg.visible);
            playerEntityModel_1.leftSleeve.visible = abstractClientPlayerEntity_1.isPartVisible(PlayerModelPart.LEFT_SLEEVE);
            model.setVisible(PlayerBone.LEFT_ARM_OVERLAY, playerEntityModel_1.leftSleeve.visible);
            playerEntityModel_1.rightSleeve.visible = abstractClientPlayerEntity_1.isPartVisible(PlayerModelPart.RIGHT_SLEEVE);
            model.setVisible(PlayerBone.RIGHT_ARM_OVERLAY, playerEntityModel_1.rightSleeve.visible);

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

        for (PlayerBone bone : model.getHiddenBones()) {
            if (bone != PlayerBone.NONE)
                bone.getCuboid(playerEntityModel_1).visible = false;
        }
    }

    // copied
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
