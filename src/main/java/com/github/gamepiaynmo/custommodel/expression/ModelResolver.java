package com.github.gamepiaynmo.custommodel.expression;

import com.github.gamepiaynmo.custommodel.client.ModelPack;
import com.github.gamepiaynmo.custommodel.render.CustomJsonModel;
import com.github.gamepiaynmo.custommodel.render.model.IBone;

public class ModelResolver implements IExpressionResolver {
   final ModelPack pack;
   final CustomJsonModel model;

   public ModelResolver(ModelPack pack) {
      this.pack = pack;
      this.model = pack.getModel();
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

   public IExpressionFloat getModelVariable(String name) {
      String[] parts = tokenize(name, "\\.");
      if (parts.length != 2) {
         return null;
      } else {
         String first = parts[0];
         String second = parts[1];

         switch (first) {
            case "tex": {
               return pack.getTexture(second);
            }
            case "item": {
               return null;
            }
            default: {
               IBone mr = this.getModelRenderer(first);
               if (mr == null) {
                  return null;
               } else {
                  ModelVariableType varType = ModelVariableType.parse(second);
                  return varType == null ? null : new ModelVariableFloat(name, mr, varType);
               }
            }
         }
      }
   }
}
