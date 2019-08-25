package com.github.gamepiaynmo.custommodel.expression;

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

   public float eval() {
      return this.getValue();
   }

   public float getValue() {
      return this.enumModelVariable.getFloat(this.modelRenderer);
   }

   public String toString() {
      return this.name;
   }
}
