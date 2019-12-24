package com.github.gamepiaynmo.custommodel.expression;

import com.github.gamepiaynmo.custommodel.render.RenderContext;
import com.github.gamepiaynmo.custommodel.render.model.IBone;

public class ModelVariableFloat implements IExpressionFloat {
   private String name;
   private IBone modelRenderer;
   private ModelVariableType enumModelVariable;

   public ModelVariableFloat(String name, IBone modelRenderer, ModelVariableType enumModelVariable) {
      this.name = name;
      this.modelRenderer = modelRenderer;
      this.enumModelVariable = enumModelVariable;
   }

   public float eval(RenderContext context) {
      return this.getValue(context);
   }

   public float getValue(RenderContext context) {
      return this.enumModelVariable.getFloat(this.modelRenderer, context);
   }

   public String toString() {
      return this.name;
   }
}
