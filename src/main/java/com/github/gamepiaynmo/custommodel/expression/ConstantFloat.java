package com.github.gamepiaynmo.custommodel.expression;

public class ConstantFloat implements IExpressionFloat {
   private float value;

   public ConstantFloat(float value) {
      this.value = value;
   }

   public float eval() {
      return this.value;
   }

   public String toString() {
      return "" + this.value;
   }
}
