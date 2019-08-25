package com.github.gamepiaynmo.custommodel.expression;

import com.github.gamepiaynmo.custommodel.client.CustomModelClient;
import net.minecraft.entity.player.PlayerEntity;

import java.util.function.Function;

public enum RenderEntityParameterBool implements IExpressionBool {
   IS_ALIVE("is_alive", entity -> entity.isAlive()),
   IS_BURNING("is_burning", entity -> entity.isOnFire()),
   IS_GLOWING("is_glowing", entity -> entity.isGlowing()),
   IS_HURT("is_hurt", entity -> entity.hurtTime > 0),
   IS_IN_LAVA("is_in_lava", entity -> entity.isInLava()),
   IS_IN_WATER("is_in_water", entity -> entity.isInWater()),
   IS_INVISIBLE("is_invisible", entity -> entity.isInvisible()),
   IS_ON_GROUND("is_on_ground", entity -> entity.onGround),
   IS_RIDING("is_riding", entity -> entity.hasVehicle()),
   IS_SNEAKING("is_sneaking", entity -> entity.isSneaking()),
   IS_SPRINTING("is_sprinting", entity -> entity.isSprinting()),
   IS_WET("is_wet", entity -> entity.isTouchingWater());

   private String name;
   private static final RenderEntityParameterBool[] VALUES = values();
   private final Function<PlayerEntity, Boolean> valueGetter;

   private RenderEntityParameterBool(String name, Function<PlayerEntity, Boolean> getter) {
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
         for(int i = 0; i < VALUES.length; ++i) {
            RenderEntityParameterBool type = VALUES[i];
            if (type.getName().equals(str)) {
               return type;
            }
         }

         return null;
      }
   }
}
