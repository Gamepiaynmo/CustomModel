package com.github.gamepiaynmo.custommodel.expression;

public interface IExpressionBool extends IExpression {
   boolean eval();

   default ExpressionType getExpressionType() {
      return ExpressionType.BOOL;
   }
}
