package com.github.gamepiaynmo.custommodel.expression;

import net.minecraft.entity.player.PlayerEntity;

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
   public boolean eval() {
      return this.type.evalBool(this.arguments);
   }
}
