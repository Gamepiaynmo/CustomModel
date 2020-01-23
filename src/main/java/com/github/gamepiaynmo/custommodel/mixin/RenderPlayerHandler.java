package com.github.gamepiaynmo.custommodel.mixin;

import com.github.gamepiaynmo.custommodel.client.CustomModelClient;
import com.github.gamepiaynmo.custommodel.client.ModelPack;
import com.github.gamepiaynmo.custommodel.entity.CustomModelNpc;
import com.github.gamepiaynmo.custommodel.client.render.*;
import com.github.gamepiaynmo.custommodel.client.render.feature.*;
import com.github.gamepiaynmo.custommodel.util.Matrix4;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.entity.layers.*;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EnumPlayerModelParts;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.event.RenderSpecificHandEvent;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.List;

public abstract class RenderPlayerHandler {
    public static void customize(RenderPlayer renderer) {
        List<LayerRenderer<EntityLivingBase>> features = ObfuscationReflectionHelper.getPrivateValue(RenderLivingBase.class, renderer, 4);
        for (int i = 0; i < features.size(); i++) {
            LayerRenderer<? extends EntityLivingBase> feature = features.get(i);
            if (feature instanceof LayerBipedArmor)
                feature = new CustomBipedArmor(renderer, context);
            if (feature instanceof LayerHeldItem)
                feature = new CustomHeldItem(renderer, context);
            if (feature instanceof LayerArrow)
                feature = new CustomArrow(renderer, context);
            if (feature instanceof LayerCape)
                feature = new CustomCape(renderer, context);
            if (feature instanceof LayerCustomHead)
                feature = new CustomHead(renderer.getMainModel().bipedHead, context);
            if (feature instanceof LayerElytra)
                feature = new CustomElytra(renderer, context);
            if (feature instanceof LayerEntityOnShoulder)
                feature = new CustomEntityOnShoulder(Minecraft.getMinecraft().getRenderManager(), context);
            features.set(i, (LayerRenderer<EntityLivingBase>) feature);
        }

        features.add(new CustomEmissive(context));
    }

    private static RenderContext context = new RenderContext();
    public static RenderContext getContext() {
        return context;
    }

    private static ModelPlayer getModel(EntityLivingBase entityLivingBase) {
        Render<? extends EntityLivingBase> render = Minecraft.getMinecraft().getRenderManager().getEntityRenderObject(entityLivingBase);
        if (entityLivingBase instanceof AbstractClientPlayer) {
            if (render instanceof RenderLivingBase) {
                ModelBase model = ((RenderLivingBase<? extends EntityLivingBase>) render).getMainModel();
                if (model instanceof ModelPlayer)
                    return (ModelPlayer) model;
            }
        } else {
            if (render instanceof RenderNpc) {
                return ((RenderNpc) render).getModelPlayer();
            }
        }
        return null;
    }

    public static boolean renderModel(EntityLivingBase playerEntity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        boolean boolean_1 = !playerEntity.isInvisible() || (boolean) ObfuscationReflectionHelper.getPrivateValue(Render.class,
                Minecraft.getMinecraft().getRenderManager().getEntityRenderObject(playerEntity), 4);
        boolean boolean_2 = !boolean_1 && !playerEntity.isInvisibleToPlayer(Minecraft.getMinecraft().player);
        if (boolean_1 || boolean_2) {
            if (boolean_2) {
                GlStateManager.enableBlendProfile(GlStateManager.Profile.TRANSPARENT_MODEL);
            }

            ModelPack model = null;
            model = CustomModelClient.manager.getModelForEntity(context.currentEntity);
            if (model != null) {
                context.currentModel.setVisible(true);
                if (context.isPlayer())
                    setModelPose(context.getPlayer(), model.getModel());
                for (PlayerBone bone : model.getModel().getHiddenBones()) {
                    if (bone != PlayerBone.NONE)
                        bone.getCuboid(context.currentModel).showModel = false;
                }
            }

            if (playerEntity instanceof AbstractClientPlayer) {
                Minecraft.getMinecraft().renderEngine.bindTexture(((AbstractClientPlayer) playerEntity).getLocationSkin());
                getModel(playerEntity).render(playerEntity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
            } else if (playerEntity instanceof CustomModelNpc) {
                Minecraft.getMinecraft().renderEngine.bindTexture(((CustomModelNpc) playerEntity).getTextureSkin());
                getModel(playerEntity).render(playerEntity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
            }

            if (model != null) {
                context.isInvisible = boolean_2;
                context.currentJsonModel = model.getModel();
                context.currentJsonModel.clearTransform();
                if (CustomModelClient.isRenderingInventory) {
                    partial = 1;
                    EntityParameter currentParameter = new EntityParameter(playerEntity);
                    CustomModelClient.inventoryEntityParameter.apply(playerEntity);
                    context.currentParameter = calculateTransform(playerEntity);
                    context.currentJsonModel.update(transform, context);
                    currentParameter.apply(playerEntity);
                }

                context.currentParameter = calculateTransform(playerEntity);
                context.currentInvTransform = transform.cpy().inv();

                model.getModel().updateSkeleton(context);
                context.currentJsonModel.render(transform, context);
                resetSkeleton();
            }

            if (boolean_2) {
                GlStateManager.disableBlendProfile(GlStateManager.Profile.TRANSPARENT_MODEL);
            }
        }

        if (context.isPlayer())
            setModelPose_c(context.getPlayer());
        return true;
    }

    public static boolean renderRightArm(AbstractClientPlayer abstractClientPlayerEntity) {
        ModelPack pack = CustomModelClient.manager.getModelForPlayer(abstractClientPlayerEntity);
        if (pack != null && !pack.getModel().getFirstPersonList(EnumHandSide.RIGHT).isEmpty()) {
            CustomModelClient.isRenderingFirstPerson = true;
            CustomJsonModel model = pack.getModel();
            partial = CustomModelClient.getPartial();
            context.currentJsonModel = model;
            context.currentModel = getModel(abstractClientPlayerEntity);
            context.currentModel.isSneak = false;
            context.currentParameter = calculateTransform(abstractClientPlayerEntity);
            context.isInvisible = false;
            context.setPlayer(abstractClientPlayerEntity);

            GlStateManager.color(1.0F, 1.0F, 1.0F);
            ModelPlayer playerModel = getModel(abstractClientPlayerEntity);
            GlStateManager.enableBlend();

            playerModel.swingProgress = 0.0F;
            playerModel.setRotationAngles(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F, abstractClientPlayerEntity);
            playerModel.bipedRightArm.rotateAngleX = 0;
            playerModel.bipedRightArmwear.rotateAngleX = 0;

            model.clearTransform();
            model.update(transform, context);

            if (!model.isHidden(PlayerBone.RIGHT_ARM))
                playerModel.bipedRightArm.render(0.0625F);
            if (!model.isHidden(PlayerBone.RIGHT_ARM_OVERLAY))
                playerModel.bipedRightArmwear.render(0.0625F);
            model.renderArm(model.getTransform(PlayerBone.RIGHT_ARM.getBone()), EnumHandSide.RIGHT, context);

            GlStateManager.disableBlend();
            CustomModelClient.isRenderingFirstPerson = false;
            return true;
        }
        return false;
    }

    public static boolean renderLeftArm(AbstractClientPlayer abstractClientPlayerEntity) {
        ModelPack pack = CustomModelClient.manager.getModelForPlayer(abstractClientPlayerEntity);
        if (pack != null && !pack.getModel().getFirstPersonList(EnumHandSide.LEFT).isEmpty()) {
            CustomModelClient.isRenderingFirstPerson = true;
            CustomJsonModel model = pack.getModel();
            partial = CustomModelClient.getPartial();
            context.currentJsonModel = model;
            context.currentModel = getModel(abstractClientPlayerEntity);
            context.currentModel.isSneak = false;
            context.currentParameter = calculateTransform(abstractClientPlayerEntity);
            context.isInvisible = false;
            context.setPlayer(abstractClientPlayerEntity);

            GlStateManager.color(1.0F, 1.0F, 1.0F);
            ModelPlayer playerModel = getModel(abstractClientPlayerEntity);
            GlStateManager.enableBlend();

            playerModel.swingProgress = 0.0F;
            playerModel.setRotationAngles(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F, abstractClientPlayerEntity);
            playerModel.bipedLeftArm.rotateAngleX = 0.0F;
            playerModel.bipedLeftArmwear.rotateAngleX = 0.0F;

            model.clearTransform();
            model.update(transform, context);

            if (!model.isHidden(PlayerBone.LEFT_ARM))
                playerModel.bipedLeftArm.render(0.0625F);
            if (!model.isHidden(PlayerBone.LEFT_ARM_OVERLAY))
                playerModel.bipedLeftArmwear.render(0.0625F);
            model.renderArm(model.getTransform(PlayerBone.LEFT_ARM.getBone()), EnumHandSide.LEFT, context);

            GlStateManager.disableBlend();
            CustomModelClient.isRenderingFirstPerson = false;
            return true;
        }
        return false;
    }

    @SubscribeEvent
    public static void renderFirstPerson(RenderSpecificHandEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayerSP player = mc.player;
        ModelPack pack = CustomModelClient.manager.getModelForPlayer(player);

        if (player.getActiveHand() == event.getHand()) {
            GlStateManager.pushMatrix();
            if (pack != null && !pack.getModel().getFirstPersonList().isEmpty()) {
                CustomModelClient.isRenderingFirstPerson = true;
                CustomJsonModel model = pack.getModel();
                partial = CustomModelClient.getPartial();
                context.currentJsonModel = model;
                context.currentModel = getModel(player);
                context.currentModel.isSneak = player.isSneaking();
                context.currentParameter = calculateTransform(player);
                context.isInvisible = false;
                context.setPlayer(player);

                GlStateManager.color(1.0F, 1.0F, 1.0F);
                ModelPlayer playerModel = getModel(player);
                GlStateManager.enableBlend();
                float yaw = (float) MathHelper.clampedLerp(player.prevRotationYaw, player.rotationYaw, partial);
                float pitch = (float) MathHelper.clampedLerp(player.prevRotationPitch, player.rotationPitch, partial);
                yaw -= (float) MathHelper.clampedLerp(player.prevRenderYawOffset, player.renderYawOffset, partial);
                float armYaw = (float) MathHelper.clampedLerp(player.prevRenderArmYaw, player.renderArmYaw, partial);
                float armPitch = (float) MathHelper.clampedLerp(player.prevRenderArmPitch, player.renderArmPitch, partial);
                GlStateManager.rotate(-(player.rotationYaw - armYaw) * 0.1F, 0.0F, 1.0F, 0.0F);
                GlStateManager.rotate(-(player.rotationPitch - armPitch) * 0.1F, 1.0F, 0.0F, 0.0F);
                GlStateManager.scale(-1, -1, 1);
                GlStateManager.translate(0.0F, 1.501F, 0.0F);
                GlStateManager.scale(0.9375F, 0.9375F, 0.9375F);
                GlStateManager.translate(0.0F, -1.501F, 0.0F);
                GlStateManager.rotate(-pitch, 1, 0, 0);
                GlStateManager.rotate(-yaw, 0, 1, 0);

                model.clearTransform();
                model.update(transform, context);
                model.renderFp(model.getTransform(PlayerBone.NONE.getBone()), context);

                GlStateManager.disableBlend();
                CustomModelClient.isRenderingFirstPerson = false;
            }
            GlStateManager.popMatrix();
        }
    }

    public static void tick(EntityLivingBase playerEntity) {
        context.setEntity(playerEntity);
        ModelPack model = CustomModelClient.manager.getModelForEntity(context.currentEntity);
        ModelPlayer playerModel = getModel(playerEntity);
        if (model != null) {
            playerModel.swingProgress = playerEntity.getSwingProgress(1);
            playerModel.isRiding = playerEntity.isRiding();
            playerModel.isChild = playerEntity.isChild();

            partial = 1;
            context.currentModel = playerModel;
            context.currentModel.isSneak = context.currentEntity.isSneaking();
            context.currentParameter = calculateTransform(playerEntity);
            context.currentJsonModel = model.getModel();
            context.currentInvTransform = transform.cpy().inv();

            model.getModel().updateSkeleton(context);
            model.getModel().tick(transform, context);
            resetSkeleton();
        }
    }

    @SubscribeEvent
    public static void renderPre(RenderPlayerEvent.Pre event) {
        EntityLivingBase entity = event.getEntityLiving();
        renderPre(entity, getModel(entity), event.getX(), event.getY(), event.getZ(), event.getPartialRenderTick(), null);
    }

    @SubscribeEvent
    public static void renderPost(RenderPlayerEvent.Post event) {
        renderPost();
    }

    public static void renderPre(EntityLivingBase entity, ModelPlayer model) {
        context.setEntity(entity);
        context.currentModel = model;
        context.currentModel.isSneak = entity.isSneaking();
        RenderPlayerHandler.partial = CustomModelClient.getPartial();
    }

    public static void renderPre(EntityLivingBase entity, ModelPlayer model, double x, double y, double z, float partial, EntityParameter params) {
        context.setEntity(entity);
        context.currentModel = model;
        context.currentModel.isSneak = entity.isSneaking();
        RenderPlayerHandler.partial = partial;

        if (x == 0 && y == 0 && z == 0 && partial == 1 && !CustomModelClient.isRenderingInventory) {
            CustomModelClient.isRenderingInventory = true;
            CustomModelClient.inventoryEntityParameter = params;
        }
    }

    public static void renderPost() {
        CustomModelClient.isRenderingInventory = false;
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
        double x = (partial - 1) * (entityLivingBaseIn.posX - entityLivingBaseIn.prevPosX);
        double y = (partial - 1) * (entityLivingBaseIn.posY - entityLivingBaseIn.prevPosY);
        double z = (partial - 1) * (entityLivingBaseIn.posZ - entityLivingBaseIn.prevPosZ);
        transform.translate(x, y, z);
    }

    private static void setupTransforms_c(EntityLivingBase entityLiving, float float_1, float float_2, float float_3) {
        transform.rotate(0.0F, 1.0F, 0.0F, 180.0F - float_2);

        if (entityLiving.deathTime > 0)
        {
            float f = ((float)entityLiving.deathTime + partial - 1.0F) / 20.0F * 1.6F;
            f = MathHelper.sqrt(f);

            if (f > 1.0F)
            {
                f = 1.0F;
            }

            transform.rotate(0.0F, 0.0F, 1.0F, f * 90);
        }
        else
        {
            String s = TextFormatting.getTextWithoutFormattingCodes(entityLiving.getName());

            if (s != null && ("Dinnerbone".equals(s) || "Grumm".equals(s)) && (!(entityLiving instanceof EntityPlayer) || ((EntityPlayer)entityLiving).isWearing(EnumPlayerModelParts.CAPE)))
            {
                transform.translate(0.0F, entityLiving.height + 0.1F, 0.0F);
                transform.rotate(0.0F, 0.0F, 1.0F, 180.0F);
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

    private static void setModelPose(AbstractClientPlayer abstractClientPlayerEntity_1, CustomJsonModel model) {
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
    }
}
