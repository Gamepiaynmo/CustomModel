package com.github.gamepiaynmo.custommodel.expression;

public interface IExpressionFloat extends IExpression {
   float eval();

   default ExpressionType getExpressionType() {
      return ExpressionType.FLOAT;
   }
}
