package com.github.gamepiaynmo.custommodel.expression;

import com.github.gamepiaynmo.custommodel.client.CustomModelClient;
import com.github.gamepiaynmo.custommodel.client.render.RenderContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;

import java.util.function.Function;

public enum RenderEntityParameterBool implements IExpressionBool {
   IS_ALIVE("is_alive", EntityLivingBase::isEntityAlive),
   IS_BURNING("is_burning", Entity::isBurning),
   IS_GLOWING("is_glowing", Entity::isGlowing),
   IS_HURT("is_hurt", entity -> entity.hurtTime > 0),
   IS_IN_LAVA("is_in_lava", Entity::isInLava),
   IS_IN_WATER("is_in_water", Entity::isInWater),
   IS_INVISIBLE("is_invisible", Entity::isInvisible),
   IS_ON_GROUND("is_on_ground", entity -> entity.onGround),
   IS_RIDING("is_riding", Entity::isRiding),
   IS_SNEAKING("is_sneaking", Entity::isSneaking),
   IS_SPRINTING("is_sprinting", Entity::isSprinting),
   IS_WET("is_wet", Entity::isWet),
   IS_INVENTORY("is_inventory", entity -> CustomModelClient.isRenderingInventory),
   IS_FIRST_PERSON("is_first_person", entity -> CustomModelClient.isRenderingFirstPerson);

   private String name;
   private static final RenderEntityParameterBool[] VALUES = values();
   private final Function<EntityLivingBase, Boolean> valueGetter;

   RenderEntityParameterBool(String name, Function<EntityLivingBase, Boolean> getter) {
      this.name = name;
      valueGetter = getter;
   }

   public String getName() {
      return this.name;
   }

   public boolean eval(RenderContext context) {
      return valueGetter.apply(context.currentEntity);
   }

   public static RenderEntityParameterBool parse(String str) {
      if (str != null) {
         for (RenderEntityParameterBool type : VALUES) {
            if (type.getName().equals(str)) {
               return type;
            }
         }

      }
      return null;
   }
}
