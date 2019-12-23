package com.github.gamepiaynmo.custommodel.mixin;

import com.github.gamepiaynmo.custommodel.client.CustomModelClient;
import com.github.gamepiaynmo.custommodel.client.ModelPack;
import com.github.gamepiaynmo.custommodel.render.*;
import com.github.gamepiaynmo.custommodel.render.feature.*;
import com.github.gamepiaynmo.custommodel.util.Matrix4;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EnumPlayerModelParts;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import scala.tools.nsc.interpreter.Formatting;

import java.util.List;

public abstract class RenderPlayerHandler {
    public static void customize(RenderPlayer renderer) {
        List<LayerRenderer<EntityLivingBase>> features = ObfuscationReflectionHelper.getPrivateValue(RenderLivingBase.class, renderer, 4);
        for (int i = 0; i < features.size(); i++) {
            LayerRenderer feature = features.get(i);
            features.set(i, feature);
        }
    }

    private static RenderContext context;

    public static void render(EntityLivingBase playerEntity) {
        boolean boolean_1 = !playerEntity.isInvisible() || (boolean) ObfuscationReflectionHelper.getPrivateValue(Render.class,
                Minecraft.getMinecraft().getRenderManager().getEntityRenderObject(playerEntity), 4);
        boolean boolean_2 = !boolean_1 && !playerEntity.isInvisibleToPlayer(Minecraft.getMinecraft().player);
        if (boolean_1 || boolean_2) {
            if (boolean_2) {
                GlStateManager.enableBlendProfile(GlStateManager.Profile.TRANSPARENT_MODEL);
            }

            ModelPack model = null;
            if (context.isPlayer())
                model = CustomModelClient.getModelForPlayer(context.getPlayer());
            if (model != null) {
                context.currentJsonModel = model.getModel();
                context.currentJsonModel.clearTransform();
                if (CustomModelClient.isRenderingInventory) {
                    EntityParameter currentParameter = new EntityParameter(playerEntity);
                    CustomModelClient.inventoryEntityParameter.apply(playerEntity);
                    context.currentParameter = calculateTransform(playerEntity);
                    context.currentJsonModel.update(transform);
                    currentParameter.apply(playerEntity);
                }

                context.currentParameter = calculateTransform(playerEntity);
                context.currentInvTransform = transform.cpy().inv();

                model.getModel().updateSkeleton();
                context.currentJsonModel.render(transform);
                resetSkeleton();
            }

            if (boolean_2) {
                GlStateManager.disableBlendProfile(GlStateManager.Profile.TRANSPARENT_MODEL);
            }
        }

        if (context.isPlayer())
            setModelPose_c(context.getPlayer());
    }

    @SubscribeEvent
    public static void renderRightArm(RenderHandEvent) {
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

            GlStateManager.color3f(1.0F, 1.0F, 1.0F);
            PlayerEntityModel<AbstractClientPlayerEntity> playerEntityModel = getModel();
            GlStateManager.enableBlend();

            playerEntityModel.isSneaking = false;
            playerEntityModel.handSwingProgress = 0.0F;
            playerEntityModel.field_3396 = 0.0F;
            playerEntityModel.method_17087(abstractClientPlayerEntity, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F);
            playerEntityModel.rightArm.pitch = 0;
            playerEntityModel.rightArmOverlay.pitch = 0;

            model.clearTransform();
            model.update(this.transform);

            if (!model.isHidden(PlayerBone.RIGHT_ARM))
                playerEntityModel.rightArm.render(0.0625F);
            if (!model.isHidden(PlayerBone.RIGHT_ARM_OVERLAY))
                playerEntityModel.rightArmOverlay.render(0.0625F);
            model.renderArm(model.getTransform(PlayerBone.RIGHT_ARM.getBone()), Arm.RIGHT);

            GlStateManager.disableBlend();
            CustomModelClient.isRenderingFirstPerson = false;
            info.cancel();
        }
    }

    public static void renderLeftArm(AbstractClientPlayerEntity abstractClientPlayerEntity, CallbackInfo info) {
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

            GlStateManager.color3f(1.0F, 1.0F, 1.0F);
            PlayerEntityModel<AbstractClientPlayerEntity> playerEntityModel = getModel();
            GlStateManager.enableBlend();

            playerEntityModel.isSneaking = false;
            playerEntityModel.handSwingProgress = 0.0F;
            playerEntityModel.field_3396 = 0.0F;
            playerEntityModel.method_17087(abstractClientPlayerEntity, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F);
            playerEntityModel.leftArm.pitch = 0.0F;
            playerEntityModel.leftArmOverlay.pitch = 0.0F;

            model.clearTransform();
            model.update(this.transform);

            if (!model.isHidden(PlayerBone.LEFT_ARM))
                playerEntityModel.leftArm.render(0.0625F);
            if (!model.isHidden(PlayerBone.LEFT_ARM_OVERLAY))
                playerEntityModel.leftArmOverlay.render(0.0625F);
            model.renderArm(model.getTransform(PlayerBone.LEFT_ARM.getBone()), Arm.LEFT);

            GlStateManager.disableBlend();
            CustomModelClient.isRenderingFirstPerson = false;
            info.cancel();
        }
    }

    public static void tick(AbstractClientPlayerEntity playerEntity) {
        ModelPack model = CustomModelClient.getModelForPlayer(playerEntity);
        if (model != null) {
            this.model.handSwingProgress = this.getHandSwingProgress(playerEntity, 1);
            this.model.isRiding = playerEntity.hasVehicle();
            this.model.isChild = playerEntity.isBaby();

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

    @SubscribeEvent
    public static void render(RenderPlayerEvent.Pre event) {
        context.setPlayer((AbstractClientPlayer) event.getEntityPlayer());
        context.currentModel = getModel();
        ModelPack model = CustomModelClient.getModelForPlayer(context.getPlayer());

        setModelPose(context.getPlayer(), model.getModel());
        partial = partial;
    }

    public static void resetSkeleton() {
        ModelPlayer model = context.currentModel;
        model.bipedHead.setRotationPoint(0.0F, 0.0F, 0.0F);
        model.bipedHeadwear.setRotationPoint(0.0F, 0.0F, 0.0F);
        model.bipedBody.setRotationPoint(0.0F, 0.0F, 0.0F);
        model.bipedRightArm.setRotationPoint(-5.0F, 2.0F, 0.0F);
        model.bipedLeftArm.setRotationPoint(5.0F, 2.0F, 0.0F);
        model.bipedRightLeg.setRotationPoint(-1.9F, 12.0F, 0.0F);
        model.bipedLeftLeg.setRotationPoint(1.9F, 12.0F, 0.0F);

        if ((boolean) ObfuscationReflectionHelper.getPrivateValue(ModelPlayer.class, model, 7)) {
            model.bipedLeftArm.setRotationPoint(5.0F, 2.5F, 0.0F);
            model.bipedRightArm.setRotationPoint(-5.0F, 2.5F, 0.0F);
            model.bipedLeftArmwear.setRotationPoint(5.0F, 2.5F, 0.0F);
            model.bipedRightArmwear.setRotationPoint(-5.0F, 2.5F, 10.0F);
        } else {
            model.bipedLeftArm.setRotationPoint(5.0F, 2.0F, 0.0F);
            model.bipedLeftArmwear.setRotationPoint(5.0F, 2.0F, 0.0F);
            model.bipedRightArmwear.setRotationPoint(-5.0F, 2.0F, 10.0F);
        }

        model.bipedLeftLegwear.setRotationPoint(1.9F, 12.0F, 0.0F);
        model.bipedRightLegwear.setRotationPoint(-1.9F, 12.0F, 0.0F);
        model.bipedBodyWear.setRotationPoint(0.0F, 0.0F, 0.0F);
    }

    private static float partial;
    private static Matrix4 transform;

    private static void method_4048_c(EntityLivingBase entityLivingBaseIn, double partial) {
        double x = MathHelper.clampedLerp(entityLivingBaseIn.prevPosX, entityLivingBaseIn.posX, partial);
        double y = MathHelper.clampedLerp(entityLivingBaseIn.prevPosY, entityLivingBaseIn.posY, partial);
        double z = MathHelper.clampedLerp(entityLivingBaseIn.prevPosZ, entityLivingBaseIn.posZ, partial);

        if (context.isPlayer() && entityLivingBaseIn.isEntityAlive() && entityLivingBaseIn.isPlayerSleeping()) {
            AbstractClientPlayer player = context.getPlayer();
            transform.translate(x + (double)player.renderOffsetX, y + (double)player.renderOffsetY, z + (double)player.renderOffsetZ);
        } else transform.translate(x, y, z);
    }

    private static void setupTransforms_c(EntityLivingBase entityLiving, float float_1, float float_2, float float_3) {
        GlStateManager.rotate(180.0F - float_2, 0.0F, 1.0F, 0.0F);

        if (entityLiving.deathTime > 0)
        {
            float f = ((float)entityLiving.deathTime + partial - 1.0F) / 20.0F * 1.6F;
            f = MathHelper.sqrt(f);

            if (f > 1.0F)
            {
                f = 1.0F;
            }

            GlStateManager.rotate(f * 90, 0.0F, 0.0F, 1.0F);
        }
        else
        {
            String s = TextFormatting.getTextWithoutFormattingCodes(entityLiving.getName());

            if (s != null && ("Dinnerbone".equals(s) || "Grumm".equals(s)) && (!(entityLiving instanceof EntityPlayer) || ((EntityPlayer)entityLiving).isWearing(EnumPlayerModelParts.CAPE)))
            {
                GlStateManager.translate(0.0F, entityLiving.height + 0.1F, 0.0F);
                GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);
            }
        }
    }

    private static void method_4212_c(EntityLivingBase abstractClientPlayerEntity_1, float float_1, float float_2, float float_3) {
        if (context.isPlayer() && abstractClientPlayerEntity_1.isEntityAlive() && abstractClientPlayerEntity_1.isPlayerSleeping())
        {
            transform.rotate(0.0F, 1.0F, 0.0F, ((AbstractClientPlayer) abstractClientPlayerEntity_1).getBedOrientationInDegrees());
            transform.rotate(0.0F, 0.0F, 1.0F, 90);
            transform.rotate(0.0F, 1.0F, 0.0F, 270);
        }
        else if (abstractClientPlayerEntity_1.isElytraFlying())
        {
            setupTransforms_c(abstractClientPlayerEntity_1, float_1, float_2, float_3);
            float f = (float)abstractClientPlayerEntity_1.getTicksElytraFlying() + partial;
            float f1 = MathHelper.clamp(f * f / 100.0F, 0.0F, 1.0F);
            transform.rotate(1.0F, 0.0F, 0.0F, f1 * (-90.0F - abstractClientPlayerEntity_1.rotationPitch));
            Vec3d vec3d = abstractClientPlayerEntity_1.getLook(partial);
            double d0 = abstractClientPlayerEntity_1.motionX * abstractClientPlayerEntity_1.motionX + abstractClientPlayerEntity_1.motionZ * abstractClientPlayerEntity_1.motionZ;
            double d1 = vec3d.x * vec3d.x + vec3d.z * vec3d.z;

            if (d0 > 0.0D && d1 > 0.0D)
            {
                double d2 = (abstractClientPlayerEntity_1.motionX * vec3d.x + abstractClientPlayerEntity_1.motionZ * vec3d.z) / (Math.sqrt(d0) * Math.sqrt(d1));
                double d3 = abstractClientPlayerEntity_1.motionX * vec3d.z - abstractClientPlayerEntity_1.motionZ * vec3d.x;
                float angle = (float)(Math.signum(d3) * Math.acos(d2)) * 180.0F / (float)Math.PI;
                if (!Float.isNaN(angle))
                    transform.rotate(0.0F, 1.0F, 0.0F, angle);
            }
        }
        else
        {
            setupTransforms_c(abstractClientPlayerEntity_1, float_1, float_2, float_3);
        }
    }

    private static float scaleAndTranslate_c(EntityLivingBase playerEntity, float float_1) {
        transform.scale(-1.0F, -1.0F, 1.0F);
        transform.scale(0.9375F, 0.9375F, 0.9375F);
        transform.translate(0.0F, -1.501F, 0.0F);
        return 0.0625F;
    }

    protected static float interpolateRotation(float prevYawOffset, float yawOffset, float partialTicks) {
        float f;
        for (f = yawOffset - prevYawOffset; f < -180.0F; f += 360.0F);
        while (f >= 180.0F)
            f -= 360.0F;
        return prevYawOffset + partialTicks * f;
    }

    private static RenderParameter calculateTransform(EntityLivingBase playerEntity) {
        try {
            float yaw = interpolateRotation(playerEntity.prevRenderYawOffset, playerEntity.renderYawOffset, partial);
            float headYaw = interpolateRotation(playerEntity.prevRotationYawHead, playerEntity.rotationYawHead, partial);
            boolean shouldSit = playerEntity.isRiding() && (playerEntity.getRidingEntity() != null && playerEntity.getRidingEntity().shouldRiderSit());
            float delta = headYaw - yaw;
            float float_8;
            if (shouldSit && playerEntity.getRidingEntity() instanceof EntityLivingBase) {
                EntityLivingBase livingEntity_2 = (EntityLivingBase) playerEntity.getRidingEntity();
                yaw = interpolateRotation(livingEntity_2.prevRenderYawOffset, livingEntity_2.renderYawOffset, partial);
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
            float float_7 = playerEntity.prevRotationPitch + (playerEntity.rotationPitch - playerEntity.prevRotationPitch) * partial;
            method_4048_c(playerEntity, partial);
            float_8 = playerEntity.ticksExisted + partial;
            method_4212_c(playerEntity, float_8, yaw, partial);
            float float_9 = scaleAndTranslate_c(playerEntity, partial);
            float float_10 = 0.0F;
            float float_11 = 0.0F;
            if (!playerEntity.isRiding() && playerEntity.isEntityAlive()) {
                float_10 = playerEntity.prevLimbSwingAmount + (playerEntity.limbSwingAmount - playerEntity.prevLimbSwingAmount) * partial;
                float_11 = playerEntity.limbSwing - playerEntity.limbSwingAmount * (1.0F - partial);
                if (playerEntity.isChild()) {
                    float_11 *= 3.0F;
                }

                if (float_10 > 1.0F) {
                    float_10 = 1.0F;
                }
            }

            GlStateManager.enableAlpha();
            context.currentModel.setLivingAnimations(playerEntity, float_11, float_10, partial);
            context.currentModel.setRotationAngles(float_11, float_10, float_8, delta, float_7, float_9, playerEntity);
            return new RenderParameter(float_11, float_10, float_8, delta, float_7, 0.0625f, partial);
        } catch (Exception ignored) {
            return null;
        }
    }

    // copied
    private static void setModelPose_c(AbstractClientPlayer abstractClientPlayerEntity_1) {
        ModelPlayer playerEntityModel_1 = context.currentModel;
        if (abstractClientPlayerEntity_1.isSpectator()) {
            playerEntityModel_1.setVisible(false);
            playerEntityModel_1.bipedHead.showModel = true;
            playerEntityModel_1.bipedHeadwear.showModel = true;
        } else {
            ItemStack itemStack_1 = abstractClientPlayerEntity_1.getHeldItemMainhand();
            ItemStack itemStack_2 = abstractClientPlayerEntity_1.getHeldItemOffhand();
            playerEntityModel_1.setVisible(true);
            playerEntityModel_1.bipedHeadwear.showModel = abstractClientPlayerEntity_1.isWearing(EnumPlayerModelParts.HAT);
            playerEntityModel_1.bipedBodyWear.showModel = abstractClientPlayerEntity_1.isWearing(EnumPlayerModelParts.JACKET);
            playerEntityModel_1.bipedLeftLegwear.showModel = abstractClientPlayerEntity_1.isWearing(EnumPlayerModelParts.LEFT_PANTS_LEG);
            playerEntityModel_1.bipedRightLegwear.showModel = abstractClientPlayerEntity_1.isWearing(EnumPlayerModelParts.RIGHT_PANTS_LEG);
            playerEntityModel_1.bipedLeftArmwear.showModel = abstractClientPlayerEntity_1.isWearing(EnumPlayerModelParts.LEFT_SLEEVE);
            playerEntityModel_1.bipedRightArmwear.showModel = abstractClientPlayerEntity_1.isWearing(EnumPlayerModelParts.RIGHT_SLEEVE);
            playerEntityModel_1.isSneak = abstractClientPlayerEntity_1.isSneaking();
            ModelBiped.ArmPose modelbiped$armpose = ModelBiped.ArmPose.EMPTY;
            ModelBiped.ArmPose modelbiped$armpose1 = ModelBiped.ArmPose.EMPTY;
            if (!itemStack_1.isEmpty()) {
                modelbiped$armpose = ModelBiped.ArmPose.ITEM;
                if (abstractClientPlayerEntity_1.getItemInUseCount() > 0) {
                    EnumAction enumaction = itemStack_1.getItemUseAction();
                    if (enumaction == EnumAction.BLOCK)
                        modelbiped$armpose = ModelBiped.ArmPose.BLOCK;
                    else if (enumaction == EnumAction.BOW)
                        modelbiped$armpose = ModelBiped.ArmPose.BOW_AND_ARROW;
                }
            }

            if (!itemStack_2.isEmpty()) {
                modelbiped$armpose1 = ModelBiped.ArmPose.ITEM;
                if (abstractClientPlayerEntity_1.getItemInUseCount() > 0) {
                    EnumAction enumaction1 = itemStack_2.getItemUseAction();
                    if (enumaction1 == EnumAction.BLOCK)
                        modelbiped$armpose1 = ModelBiped.ArmPose.BLOCK;
                    else if (enumaction1 == EnumAction.BOW)
                        modelbiped$armpose1 = ModelBiped.ArmPose.BOW_AND_ARROW;
                }
            }

            if (abstractClientPlayerEntity_1.getPrimaryHand() == EnumHandSide.RIGHT) {
                playerEntityModel_1.rightArmPose = modelbiped$armpose;
                playerEntityModel_1.leftArmPose = modelbiped$armpose1;
            } else {
                playerEntityModel_1.rightArmPose = modelbiped$armpose1;
                playerEntityModel_1.leftArmPose = modelbiped$armpose;
            }
        }

    }

    private void setModelPose(AbstractClientPlayer abstractClientPlayerEntity_1, CustomJsonModel model) {
        ModelPlayer playerEntityModel_1 = context.currentModel;
        if (abstractClientPlayerEntity_1.isSpectator()) {
            playerEntityModel_1.setVisible(false);
            model.setVisible(false);
            playerEntityModel_1.bipedHead.showModel = true;
            model.setVisible(PlayerBone.HEAD, true);
            playerEntityModel_1.bipedHeadwear.showModel = true;
            model.setVisible(PlayerBone.HEAD_OVERLAY, true);
        } else {
            ItemStack itemStack_1 = abstractClientPlayerEntity_1.getHeldItemMainhand();
            ItemStack itemStack_2 = abstractClientPlayerEntity_1.getHeldItemOffhand();
            playerEntityModel_1.setVisible(true);
            model.setVisible(true);
            playerEntityModel_1.bipedHeadwear.showModel = abstractClientPlayerEntity_1.isWearing(EnumPlayerModelParts.HAT);
            model.setVisible(PlayerBone.HEAD_OVERLAY, playerEntityModel_1.bipedHeadwear.showModel);
            playerEntityModel_1.bipedBodyWear.showModel = abstractClientPlayerEntity_1.isWearing(EnumPlayerModelParts.JACKET);
            model.setVisible(PlayerBone.BODY_OVERLAY, playerEntityModel_1.bipedBodyWear.showModel);
            playerEntityModel_1.bipedLeftLegwear.showModel = abstractClientPlayerEntity_1.isWearing(EnumPlayerModelParts.LEFT_PANTS_LEG);
            model.setVisible(PlayerBone.LEFT_LEG_OVERLAY, playerEntityModel_1.bipedLeftLegwear.showModel);
            playerEntityModel_1.bipedRightLegwear.showModel = abstractClientPlayerEntity_1.isWearing(EnumPlayerModelParts.RIGHT_PANTS_LEG);
            model.setVisible(PlayerBone.RIGHT_LEG_OVERLAY, playerEntityModel_1.bipedRightLegwear.showModel);
            playerEntityModel_1.bipedLeftArmwear.showModel = abstractClientPlayerEntity_1.isWearing(EnumPlayerModelParts.LEFT_SLEEVE);
            model.setVisible(PlayerBone.LEFT_ARM_OVERLAY, playerEntityModel_1.bipedLeftArmwear.showModel);
            playerEntityModel_1.bipedRightArmwear.showModel = abstractClientPlayerEntity_1.isWearing(EnumPlayerModelParts.RIGHT_SLEEVE);
            model.setVisible(PlayerBone.RIGHT_ARM_OVERLAY, playerEntityModel_1.bipedRightArmwear.showModel);

            playerEntityModel_1.isSneak = abstractClientPlayerEntity_1.isSneaking();
            ModelBiped.ArmPose modelbiped$armpose = ModelBiped.ArmPose.EMPTY;
            ModelBiped.ArmPose modelbiped$armpose1 = ModelBiped.ArmPose.EMPTY;
            if (!itemStack_1.isEmpty()) {
                modelbiped$armpose = ModelBiped.ArmPose.ITEM;
                if (abstractClientPlayerEntity_1.getItemInUseCount() > 0) {
                    EnumAction enumaction = itemStack_1.getItemUseAction();
                    if (enumaction == EnumAction.BLOCK)
                        modelbiped$armpose = ModelBiped.ArmPose.BLOCK;
                    else if (enumaction == EnumAction.BOW)
                        modelbiped$armpose = ModelBiped.ArmPose.BOW_AND_ARROW;
                }
            }

            if (!itemStack_2.isEmpty()) {
                modelbiped$armpose1 = ModelBiped.ArmPose.ITEM;
                if (abstractClientPlayerEntity_1.getItemInUseCount() > 0) {
                    EnumAction enumaction1 = itemStack_2.getItemUseAction();
                    if (enumaction1 == EnumAction.BLOCK)
                        modelbiped$armpose1 = ModelBiped.ArmPose.BLOCK;
                    else if (enumaction1 == EnumAction.BOW)
                        modelbiped$armpose1 = ModelBiped.ArmPose.BOW_AND_ARROW;
                }
            }

            if (abstractClientPlayerEntity_1.getPrimaryHand() == EnumHandSide.RIGHT) {
                playerEntityModel_1.rightArmPose = modelbiped$armpose;
                playerEntityModel_1.leftArmPose = modelbiped$armpose1;
            } else {
                playerEntityModel_1.rightArmPose = modelbiped$armpose1;
                playerEntityModel_1.leftArmPose = modelbiped$armpose;
            }
        }

        for (PlayerBone bone : model.getHiddenBones()) {
            if (bone != PlayerBone.NONE)
                bone.getCuboid(playerEntityModel_1).showModel = false;
        }
    }
}
