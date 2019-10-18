package com.github.gamepiaynmo.custommodel.expression;

import com.github.gamepiaynmo.custommodel.render.CustomJsonModel;
import com.github.gamepiaynmo.custommodel.render.model.IBone;

public class ModelResolver implements IExpressionResolver {
   CustomJsonModel model;

   public ModelResolver(CustomJsonModel model) {
      this.model = model;
   }

   public IExpression getParameter(String name) {
      RenderEntityParameterBool parBool = RenderEntityParameterBool.parse(name);
      if (parBool != null) {
         return parBool;
      } else {
         RenderEntityParameterFloat parFloat = RenderEntityParameterFloat.parse(name);
         return parFloat;
      }
   }

   public IExpression getExpression(String name) {
      IExpression mv = this.getModelVariable(name);
      if (mv != null) {
         return mv;
      } else {
         IExpression param = getParameter(name);
         return param;
      }
   }

   public IBone getModelRenderer(String name) {
      return model.getBone(name);
   }

   public static String[] tokenize(String str, String delim) {
      return str.split(delim);
   }

   public ModelVariableFloat getModelVariable(String name) {
      String[] parts = tokenize(name, ".");
      if (parts.length != 2) {
         return null;
      } else {
         String modelName = parts[0];
         String varName = parts[1];
         IBone mr = this.getModelRenderer(modelName);
         if (mr == null) {
            return null;
         } else {
            ModelVariableType varType = ModelVariableType.parse(varName);
            return varType == null ? null : new ModelVariableFloat(name, mr, varType);
         }
      }
   }
}
