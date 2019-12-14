package com.github.gamepiaynmo.custommodel.expression;

import com.github.gamepiaynmo.custommodel.client.CustomModelClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;

import java.util.function.Function;

public enum RenderEntityParameterBool implements IExpressionBool {
   IS_ALIVE("is_alive", LivingEntity::isAlive),
   IS_BURNING("is_burning", Entity::isOnFire),
   IS_GLOWING("is_glowing", Entity::isGlowing),
   IS_HURT("is_hurt", entity -> entity.hurtTime > 0),
   IS_IN_LAVA("is_in_lava", Entity::isInLava),
   IS_IN_WATER("is_in_water", Entity::isInWater),
   IS_INVISIBLE("is_invisible", Entity::isInvisible),
   IS_ON_GROUND("is_on_ground", entity -> entity.onGround),
   IS_RIDING("is_riding", Entity::hasVehicle),
   IS_SNEAKING("is_sneaking", Entity::isInSneakingPose),
   IS_SPRINTING("is_sprinting", Entity::isSprinting),
   IS_WET("is_wet", Entity::isTouchingWater);

   private String name;
   private static final RenderEntityParameterBool[] VALUES = values();
   private final Function<PlayerEntity, Boolean> valueGetter;

   RenderEntityParameterBool(String name, Function<PlayerEntity, Boolean> getter) {
      this.name = name;
      valueGetter = getter;
   }

   public String getName() {
      return this.name;
   }

   public boolean eval() {
      return valueGetter.apply(CustomModelClient.currentPlayer);
   }

   public static RenderEntityParameterBool parse(String str) {
      if (str == null) {
         return null;
      } else {
         for (RenderEntityParameterBool type : VALUES) {
            if (type.getName().equals(str)) {
               return type;
            }
         }

         return null;
      }
   }
}
