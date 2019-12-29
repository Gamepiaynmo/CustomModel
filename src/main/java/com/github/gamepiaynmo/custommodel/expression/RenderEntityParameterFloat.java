package com.github.gamepiaynmo.custommodel.expression;

import com.github.gamepiaynmo.custommodel.client.CustomModelClient;
import com.github.gamepiaynmo.custommodel.mixin.PlayerStatureHandler;
import com.github.gamepiaynmo.custommodel.render.RenderContext;
import com.github.gamepiaynmo.custommodel.render.RenderParameter;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.MathHelper;

import java.util.function.BiFunction;

public enum RenderEntityParameterFloat implements IExpressionFloat {
   LIMB_SWING("limb_swing", (entity, params) -> params.limbSwing),
   LIMB_SWING_SPEED("limb_speed", (entity, params) -> params.limbSwingAmount),
   AGE("age", (entity, params) -> params.age),
   HEAD_YAW("head_yaw", (entity, params) -> params.headYaw),
   HEAD_PITCH("head_pitch", (entity, params) -> params.headPitch),
   SCALE("scale", (entity, params) -> params.scale),
   HEALTH("health", (entity, params) -> entity.getHealth()),
   FOOD_LEVEL("food_level", (entity, params) -> (float) (entity instanceof EntityPlayer ? ((EntityPlayer) entity).getFoodStats().getFoodLevel() : 0)),
   HURT_TIME("hurt_time", (entity, params) -> (float) entity.hurtTime - params.partial),
   POS_X("pos_x", (entity, params) -> (float) MathHelper.clampedLerp(entity.prevPosX, entity.posX, params.partial)),
   POS_Y("pos_y", (entity, params) -> (float) MathHelper.clampedLerp(entity.prevPosY, entity.posY, params.partial)),
   POS_Z("pos_z", (entity, params) -> (float) MathHelper.clampedLerp(entity.prevPosZ, entity.posZ, params.partial)),
   SPEED_X("speed_x", (entity, params) -> (float) entity.motionX),
   SPEED_Y("speed_y", (entity, params) -> (float) entity.motionY),
   SPEED_Z("speed_z", (entity, params) -> (float) entity.motionZ),
   YAW("yaw", (entity, params) -> (float) MathHelper.clampedLerp(entity.prevRotationYaw, entity.rotationYaw, params.partial)),
   BODY_YAW("body_yaw", (entity, params) -> (float) MathHelper.clampedLerp(entity.prevRenderYawOffset, entity.renderYawOffset, params.partial)),
   PITCH("pitch", (entity, params) -> (float) MathHelper.clampedLerp(entity.prevRotationPitch, entity.rotationPitch, params.partial)),
   SWING_PROGRESS("swing_progress", (entity, params) -> entity.getSwingProgress(params.partial)),
   CURRENT_POSE("current_pose", (entity, params) -> (float) PlayerStatureHandler.getPose(entity).getId());

   private String name;
   private static final RenderEntityParameterFloat[] VALUES = values();
   private final BiFunction<EntityLivingBase, RenderParameter, Float> valueGetter;

   RenderEntityParameterFloat(String name, BiFunction<EntityLivingBase, RenderParameter, Float> getter) {
      this.name = name;
      valueGetter = getter;
   }

   public String getName() {
      return this.name;
   }

   @Override
   public float eval(RenderContext context) {
      return valueGetter.apply(context.currentEntity, context.currentParameter);
   }

   public static RenderEntityParameterFloat parse(String str) {
      if (str == null) {
         return null;
      } else {
         for (RenderEntityParameterFloat type : VALUES) {
            if (type.getName().equals(str)) {
               return type;
            }
         }

         return null;
      }
   }
}
