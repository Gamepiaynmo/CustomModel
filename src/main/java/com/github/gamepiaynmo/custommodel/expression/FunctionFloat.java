package com.github.gamepiaynmo.custommodel.expression;

import com.github.gamepiaynmo.custommodel.render.RenderParameter;
import net.minecraft.entity.player.PlayerEntity;

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
   public float eval() {
      return this.type.evalFloat(this.arguments);
   }
}
