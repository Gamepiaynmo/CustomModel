package com.github.gamepiaynmo.custommodel.client.render.feature;

import com.github.gamepiaynmo.custommodel.client.CustomModelClient;
import com.github.gamepiaynmo.custommodel.client.ModelPack;
import com.github.gamepiaynmo.custommodel.client.render.CustomJsonModel;
import com.github.gamepiaynmo.custommodel.client.render.PlayerBone;
import com.github.gamepiaynmo.custommodel.client.render.RenderContext;
import com.github.gamepiaynmo.custommodel.client.render.model.Bone;
import com.github.gamepiaynmo.custommodel.client.render.model.Quad;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.model.Box;
import net.minecraft.client.model.Cuboid;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.GuiLighting;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.StuckArrowsFeatureRenderer;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL11;

import java.util.List;
import java.util.Random;

public class CustomStuckArrows<T extends AbstractClientPlayerEntity, M extends PlayerEntityModel<T>> extends StuckArrowsFeatureRenderer<T, M> {
    private final EntityRenderDispatcher field_17153;
    private final RenderContext context;

    public CustomStuckArrows(LivingEntityRenderer<T, M> livingEntityRenderer_1, RenderContext context) {
        super(livingEntityRenderer_1);
        this.field_17153 = livingEntityRenderer_1.getRenderManager();
        this.context = context;
    }

    @Override
    public void render(T livingEntity_1, float float_1, float float_2, float float_3, float float_4, float float_5, float float_6, float float_7) {
        ModelPack pack = CustomModelClient.manager.getModelForPlayer(livingEntity_1);
        CustomJsonModel model = pack == null ? null : pack.getModel();
        if (model == null) {
            super.method_17158(livingEntity_1, float_1, float_2, float_3, float_4, float_5, float_6, float_7);
            return;
        }

        int int_1 = livingEntity_1.getStuckArrows();
        if (int_1 > 0) {
            Entity entity_1 = new ArrowEntity(livingEntity_1.world, livingEntity_1.x, livingEntity_1.y, livingEntity_1.z);
            Random random_1 = new Random((long)livingEntity_1.getEntityId());
            GuiLighting.disable();

            List<Bone> bones = Lists.newArrayList();
            for (Bone bone : model.getBones()) {
                if (bone.isVisible(context) && (bone.getBoxes().size() > 0 || bone.getQuads().size() > 0))
                    bones.add(bone);
            }

            List<PlayerBone> playerBones = Lists.newArrayList();
            for (PlayerBone bone : PlayerBone.values()) {
                if (bone != PlayerBone.NONE && !model.isHidden(bone))
                    playerBones.add(bone);
            }

            for(int int_2 = 0; int_2 < int_1; ++int_2) {
                GlStateManager.pushMatrix();
                float float_8 = random_1.nextFloat();
                float float_9 = random_1.nextFloat();
                float float_10 = random_1.nextFloat();
                float float_11 = 0, float_12 = 0, float_13 = 0;

                // =================================================

                int index = random_1.nextInt(bones.size() + playerBones.size());
                if (index < playerBones.size()) {
                    Cuboid cuboid_1 = playerBones.get(index).getCuboid(context.currentModel);
                    Box box_1 = (Box) cuboid_1.boxes.get(random_1.nextInt(cuboid_1.boxes.size()));
                    cuboid_1.applyTransform(0.0625F);
                    float_11 = MathHelper.lerp(float_8, box_1.xMin, box_1.xMax) / 16.0F;
                    float_12 = MathHelper.lerp(float_9, box_1.yMin, box_1.yMax) / 16.0F;
                    float_13 = MathHelper.lerp(float_10, box_1.zMin, box_1.zMax) / 16.0F;
                } else {
                    Bone bone = bones.get(index - playerBones.size());
                    index = random_1.nextInt(bone.getBoxes().size() + bone.getQuads().size());
                    GL11.glMultMatrixd(context.currentInvTransform.val);
                    GL11.glMultMatrixd(model.getTransform(bone).val);
                    if (index < bone.getBoxes().size()) {
                        com.github.gamepiaynmo.custommodel.client.render.model.Box box_1 = bone.getBoxes().get(index);
                        float_11 = MathHelper.lerp(float_8, box_1.xMin, box_1.xMax) / 16.0F;
                        float_12 = MathHelper.lerp(float_9, box_1.yMin, box_1.yMax) / 16.0F;
                        float_13 = MathHelper.lerp(float_10, box_1.zMin, box_1.zMax) / 16.0F;
                    } else {
                        Quad quad = bone.getQuads().get(index - bone.getBoxes().size());
                        float_11 = MathHelper.lerp(float_8, quad.xMin, quad.xMax) / 16.0F;
                        float_12 = MathHelper.lerp(float_9, quad.yMin, quad.yMax) / 16.0F;
                        float_13 = quad.z / 16.0F;
                    }
                }

                // =================================================

                GlStateManager.translatef(float_11, float_12, float_13);
                float_8 = float_8 * 2.0F - 1.0F;
                float_9 = float_9 * 2.0F - 1.0F;
                float_10 = float_10 * 2.0F - 1.0F;
                float_8 *= -1.0F;
                float_9 *= -1.0F;
                float_10 *= -1.0F;
                float float_14 = MathHelper.sqrt(float_8 * float_8 + float_10 * float_10);
                entity_1.yaw = (float)(Math.atan2((double)float_8, (double)float_10) * 57.2957763671875D);
                entity_1.pitch = (float)(Math.atan2((double)float_9, (double)float_14) * 57.2957763671875D);
                entity_1.prevYaw = entity_1.yaw;
                entity_1.prevPitch = entity_1.pitch;
                double double_1 = 0.0D;
                double double_2 = 0.0D;
                double double_3 = 0.0D;
                this.field_17153.render(entity_1, 0.0D, 0.0D, 0.0D, 0.0F, float_3, false);
                GlStateManager.popMatrix();
            }

            GuiLighting.enable();
        }
    }
}
