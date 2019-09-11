package com.github.gamepiaynmo.custommodel.expression;

import com.github.gamepiaynmo.custommodel.client.CustomModelClient;
import com.github.gamepiaynmo.custommodel.render.CustomPlayerEntityRenderer;
import com.github.gamepiaynmo.custommodel.render.RenderParameter;
import com.mojang.datafixers.util.Either;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;

import java.util.function.BiFunction;
import java.util.function.Function;

public enum RenderEntityParameterFloat implements IExpressionFloat {
   LIMB_SWING("limb_swing", (entity, params) -> params.limbSwing),
   LIMB_SWING_SPEED("limb_speed", (entity, params) -> params.limbSwingAmount),
   AGE("age", (entity, params) -> params.age),
   HEAD_YAW("head_yaw", (entity, params) -> params.headYaw),
   HEAD_PITCH("head_pitch", (entity, params) -> params.headPitch),
   SCALE("scale", (entity, params) -> params.scale),
   HEALTH("health", (entity, params) -> entity.getHealth()),
   FOOD_LEVEL("food_level", (entity, params) -> (float) entity.getHungerManager().getFoodLevel()),
   HURT_TIME("hurt_time", (entity, params) -> (float) entity.hurtTime - params.partial),
   POS_X("pos_x", (entity, params) -> (float) MathHelper.lerp(params.partial, entity.prevX, entity.x)),
   POS_Y("pos_y", (entity, params) -> (float) MathHelper.lerp(params.partial, entity.prevY, entity.y)),
   POS_Z("pos_z", (entity, params) -> (float) MathHelper.lerp(params.partial, entity.prevZ, entity.z)),
   SPEED_X("speed_x", (entity, params) -> (float) entity.getVelocity().x),
   SPEED_Y("speed_y", (entity, params) -> (float) entity.getVelocity().y),
   SPEED_Z("speed_z", (entity, params) -> (float) entity.getVelocity().z),
   YAW("yaw", (entity, params) -> MathHelper.lerp(params.partial, entity.prevYaw, entity.yaw)),
   PITCH("pitch", (entity, params) -> MathHelper.lerp(params.partial, entity.prevPitch, entity.pitch)),
   SWING_PROGRESS("swing_progress", (entity, params) -> entity.getHandSwingProgress(params.partial));

   private String name;
   private static final RenderEntityParameterFloat[] VALUES = values();
   private final BiFunction<PlayerEntity, RenderParameter, Float> valueGetter;

   RenderEntityParameterFloat(String name, BiFunction<PlayerEntity, RenderParameter, Float> getter) {
      this.name = name;
      valueGetter = getter;
   }

   public String getName() {
      return this.name;
   }

   public float eval() {
      return valueGetter.apply(CustomModelClient.currentPlayer, CustomModelClient.currentParameter);
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
