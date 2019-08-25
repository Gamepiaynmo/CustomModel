package com.github.gamepiaynmo.custommodel.expression;

import com.github.gamepiaynmo.custommodel.render.RenderParameter;
import net.minecraft.entity.player.PlayerEntity;

public interface IExpressionFloat extends IExpression {
   float eval();

   default ExpressionType getExpressionType() {
      return ExpressionType.FLOAT;
   }
}
