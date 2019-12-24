package com.github.gamepiaynmo.custommodel.expression;

import com.github.gamepiaynmo.custommodel.render.RenderContext;

public class ConstantFloat implements IExpressionFloat {
   private float value;

   public ConstantFloat(float value) {
      this.value = value;
   }

   public float eval(RenderContext context) {
      return this.value;
   }

   public String toString() {
      return "" + this.value;
   }
}
