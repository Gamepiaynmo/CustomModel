package com.github.gamepiaynmo.custommodel.expression;

import com.github.gamepiaynmo.custommodel.render.model.IBone;

import java.util.function.Function;

public enum ModelVariableType {
   POS_X("tx", (bone) -> (float) bone.getPosition().x),
   POS_Y("ty", (bone) -> (float) bone.getPosition().y),
   POS_Z("tz", (bone) -> (float) bone.getPosition().z),
   ANGLE_X("rx", (bone) -> (float) bone.getRotation().x),
   ANGLE_Y("ry", (bone) -> (float) bone.getRotation().y),
   ANGLE_Z("rz", (bone) -> (float) bone.getRotation().z),
   SCALE_X("sx", (bone) -> (float) bone.getScale().x),
   SCALE_Y("sy", (bone) -> (float) bone.getScale().y),
   SCALE_Z("sz", (bone) -> (float) bone.getScale().z);

   private String name;
   public static ModelVariableType[] VALUES = values();

   private final Function<IBone, Float> valueGetter;

   ModelVariableType(String name, Function<IBone, Float> getter) {
      this.name = name;
      valueGetter = getter;
   }

   public String getName() {
      return this.name;
   }

   public float getFloat(IBone bone) {
      return valueGetter.apply(bone);
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
