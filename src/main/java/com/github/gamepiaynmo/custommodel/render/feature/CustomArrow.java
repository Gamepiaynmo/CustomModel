package com.github.gamepiaynmo.custommodel.render.feature;

import com.github.gamepiaynmo.custommodel.client.CustomModelClient;
import com.github.gamepiaynmo.custommodel.client.ModelPack;
import com.github.gamepiaynmo.custommodel.render.CustomJsonModel;
import com.github.gamepiaynmo.custommodel.render.PlayerBone;
import com.github.gamepiaynmo.custommodel.render.RenderContext;
import com.github.gamepiaynmo.custommodel.render.model.Bone;
import com.github.gamepiaynmo.custommodel.render.model.Quad;
import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.layers.LayerArrow;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityTippedArrow;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL11;

import java.util.List;
import java.util.Random;

public class CustomArrow extends LayerArrow {
    private final RenderContext context;

    public CustomArrow(RenderLivingBase<?> rendererIn, RenderContext context) {
        super(rendererIn);
        this.context = context;
    }

    @Override
    public void doRenderLayer(EntityLivingBase entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        ModelPack pack = CustomModelClient.getModelForPlayer(entitylivingbaseIn);
        CustomJsonModel model = pack == null ? null : pack.getModel();
        if (model == null) {
            super.doRenderLayer(entitylivingbaseIn, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, scale);
            return;
        }

        int i = entitylivingbaseIn.getArrowCountInEntity();
        if (i > 0) {
            Entity entity = new EntityTippedArrow(entitylivingbaseIn.world, entitylivingbaseIn.posX, entitylivingbaseIn.posY, entitylivingbaseIn.posZ);
            Random random = new Random((long)entitylivingbaseIn.getEntityId());
            RenderHelper.disableStandardItemLighting();

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

            for(int int_2 = 0; int_2 < i; ++int_2) {
                GlStateManager.pushMatrix();
                float float_8 = random.nextFloat();
                float float_9 = random.nextFloat();
                float float_10 = random.nextFloat();
                float float_11 = 0, float_12 = 0, float_13 = 0;

                // =================================================

                int index = random.nextInt(bones.size() + playerBones.size());
                if (index < playerBones.size()) {
                    ModelRenderer cuboid_1 = playerBones.get(index).getCuboid(context.currentModel);
                    ModelBox box_1 = (ModelBox) cuboid_1.cubeList.get(random.nextInt(cuboid_1.cubeList.size()));
                    cuboid_1.postRender(0.0625F);
                    float_11 = (float) (MathHelper.clampedLerp(box_1.posX1, box_1.posX2, float_8) / 16.0F);
                    float_12 = (float) (MathHelper.clampedLerp(box_1.posY1, box_1.posY2, float_9) / 16.0F);
                    float_13 = (float) (MathHelper.clampedLerp(box_1.posZ1, box_1.posZ2, float_10) / 16.0F);
                } else {
                    Bone bone = bones.get(index - playerBones.size());
                    index = random.nextInt(bone.getBoxes().size() + bone.getQuads().size());
                    GL11.glMultMatrix(context.currentInvTransform.toBuffer());
                    GL11.glMultMatrix(model.getTransform(bone).toBuffer());
                    if (index < bone.getBoxes().size()) {
                        com.github.gamepiaynmo.custommodel.render.model.Box box_1 = bone.getBoxes().get(index);
                        float_11 = (float) (MathHelper.clampedLerp(box_1.xMin, box_1.xMax, float_8) / 16.0F);
                        float_12 = (float) (MathHelper.clampedLerp(box_1.yMin, box_1.yMax, float_9) / 16.0F);
                        float_13 = (float) (MathHelper.clampedLerp(box_1.zMin, box_1.zMax, float_10) / 16.0F);
                    } else {
                        Quad quad = bone.getQuads().get(index - bone.getBoxes().size());
                        float_11 = (float) (MathHelper.clampedLerp(quad.xMin, quad.xMax, float_8) / 16.0F);
                        float_12 = (float) (MathHelper.clampedLerp(quad.yMin, quad.yMax, float_9) / 16.0F);
                        float_13 = quad.z / 16.0F;
                    }
                }

                // =================================================

                GlStateManager.translate(float_11, float_12, float_13);
                float_8 = float_8 * 2.0F - 1.0F;
                float_9 = float_9 * 2.0F - 1.0F;
                float_10 = float_10 * 2.0F - 1.0F;
                float_8 *= -1.0F;
                float_9 *= -1.0F;
                float_10 *= -1.0F;
                float float_14 = MathHelper.sqrt(float_8 * float_8 + float_10 * float_10);
                entity.rotationYaw = (float)(Math.atan2((double)float_8, (double)float_10) * 57.2957763671875D);
                entity.rotationPitch = (float)(Math.atan2((double)float_9, (double)float_14) * 57.2957763671875D);
                entity.prevRotationYaw = entity.rotationYaw;
                entity.prevRotationPitch = entity.rotationPitch;
                double double_1 = 0.0D;
                double double_2 = 0.0D;
                double double_3 = 0.0D;
                Minecraft.getMinecraft().getRenderManager().renderEntity(entity, 0.0D, 0.0D, 0.0D, 0.0F, partialTicks, false);
                GlStateManager.popMatrix();
            }

            RenderHelper.enableStandardItemLighting();
        }
    }
}
