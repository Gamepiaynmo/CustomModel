package com.github.gamepiaynmo.custommodel.expression;

import net.minecraft.entity.player.PlayerEntity;

public interface IExpressionBool extends IExpression {
   boolean eval();

   default ExpressionType getExpressionType() {
      return ExpressionType.BOOL;
   }
}
