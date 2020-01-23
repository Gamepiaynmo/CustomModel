package com.github.gamepiaynmo.custommodel.expression;

import com.github.gamepiaynmo.custommodel.client.render.RenderContext;
import com.github.gamepiaynmo.custommodel.client.render.model.IBone;

import java.util.function.BiFunction;

public enum ModelVariableType {
   POS_X("tx", (bone, context) -> (float) bone.getPosition(context).x),
   POS_Y("ty", (bone, context) -> (float) bone.getPosition(context).y),
   POS_Z("tz", (bone, context) -> (float) bone.getPosition(context).z),
   ANGLE_X("rx", (bone, context) -> (float) bone.getRotation(context).x),
   ANGLE_Y("ry", (bone, context) -> (float) bone.getRotation(context).y),
   ANGLE_Z("rz", (bone, context) -> (float) bone.getRotation(context).z),
   SCALE_X("sx", (bone, context) -> (float) bone.getScale(context).x),
   SCALE_Y("sy", (bone, context) -> (float) bone.getScale(context).y),
   SCALE_Z("sz", (bone, context) -> (float) bone.getScale(context).z);

   private String name;
   public static ModelVariableType[] VALUES = values();

   private final BiFunction<IBone, RenderContext, Float> valueGetter;

   ModelVariableType(String name, BiFunction<IBone, RenderContext, Float> getter) {
      this.name = name;
      valueGetter = getter;
   }

   public String getName() {
      return this.name;
   }

   public float getFloat(IBone bone, RenderContext context) {
      return valueGetter.apply(bone, context);
   }

   public static ModelVariableType parse(String str) {
      for (ModelVariableType var : VALUES) {
         if (var.getName().equals(str)) {
            return var;
         }
      }

      return null;
   }
}
