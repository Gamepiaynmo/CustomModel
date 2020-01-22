package com.github.gamepiaynmo.custommodel.expression;

import com.github.gamepiaynmo.custommodel.client.render.RenderContext;

public class FunctionFloat implements IExpressionFloat {
   private FunctionType type;
   private IExpression[] arguments;
   private int smoothId = -1;

   public FunctionFloat(FunctionType type, IExpression[] arguments) {
      this.type = type;
      this.arguments = arguments;
   }

   public String toString() {
      return "" + this.type + "()";
   }

   @Override
   public float eval(RenderContext context) {
      return this.type.evalFloat(this.arguments, context);
   }
}
