package com.github.gamepiaynmo.custommodel.expression;

import com.github.gamepiaynmo.custommodel.client.render.RenderContext;

public class FunctionBool implements IExpressionBool {
   private FunctionType type;
   private IExpression[] arguments;

   public FunctionBool(FunctionType type, IExpression[] arguments) {
      this.type = type;
      this.arguments = arguments;
   }

   public String toString() {
      return "" + this.type + "()";
   }

   @Override
   public boolean eval(RenderContext context) {
      return this.type.evalBool(this.arguments, context);
   }
}
