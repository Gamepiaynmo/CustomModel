package com.github.gamepiaynmo.custommodel.expression;

import com.github.gamepiaynmo.custommodel.render.CustomJsonModel;
import com.github.gamepiaynmo.custommodel.render.model.IBone;

import java.util.ArrayList;
import java.util.StringTokenizer;

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
      StringTokenizer tok = new StringTokenizer(str, delim);
      ArrayList<String> list = new ArrayList();

      while(tok.hasMoreTokens()) {
         String token = tok.nextToken();
         list.add(token);
      }

      return list.toArray(new String[list.size()]);
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
