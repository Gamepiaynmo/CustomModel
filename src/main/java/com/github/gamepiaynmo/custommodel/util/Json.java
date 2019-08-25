package com.github.gamepiaynmo.custommodel.util;

import com.github.gamepiaynmo.custommodel.expression.*;
import com.google.gson.*;
import net.minecraft.text.TranslatableText;

public class Json {
   public static float getFloat(JsonObject obj, String field, float def) {
      JsonElement elem = obj.get(field);
      return elem == null ? def : elem.getAsFloat();
   }

   public static IExpressionFloat getFloatExpression(JsonElement jsonElement, float def, ExpressionParser parser) throws ParseException {
      if (jsonElement == null) return new ConstantFloat(def);
      JsonPrimitive primitive = jsonElement.getAsJsonPrimitive();
      if (primitive.isNumber()) return new ConstantFloat(primitive.getAsFloat());
      return parser.parseFloat(primitive.getAsString());
   }

   public static IExpressionFloat getFloatExpression(JsonObject obj, String field, float def, ExpressionParser parser) throws ParseException {
      return getFloatExpression(obj.get(field), def, parser);
   }

   public static double getDouble(JsonObject obj, String field, double def) {
      JsonElement elem = obj.get(field);
      return elem == null ? def : elem.getAsDouble();
   }

   public static boolean getBoolean(JsonObject obj, String field, boolean def) {
      JsonElement elem = obj.get(field);
      return elem == null ? def : elem.getAsBoolean();
   }

   private static IExpressionBool getConstantBoolean(boolean val) {
      return new FunctionBool(val ? FunctionType.TRUE : FunctionType.FALSE, new IExpression[0]);
   }

   public static IExpressionBool getBooleanExpression(JsonObject obj, String field, boolean def, ExpressionParser parser) throws ParseException {
      JsonElement elem = obj.get(field);
      if (elem == null) return getConstantBoolean(def);
      JsonPrimitive primitive = elem.getAsJsonPrimitive();
      if (primitive.isBoolean()) return getConstantBoolean(primitive.getAsBoolean());
      return parser.parseBool(primitive.getAsString());
   }

   public static String getString(JsonObject jsonObj, String field) {
      return getString(jsonObj, field, (String)null);
   }

   public static String getString(JsonObject jsonObj, String field, String def) {
      JsonElement jsonElement = jsonObj.get(field);
      return jsonElement == null ? def : jsonElement.getAsString();
   }

   public static float[] parseFloatArray(JsonElement jsonElement, int len) {
      return parseFloatArray(jsonElement, len, (float[])null);
   }

   public static float[] parseFloatArray(JsonElement jsonElement, int len, float[] def) {
      if (jsonElement == null) {
         return def;
      } else {
         JsonArray arr = jsonElement.getAsJsonArray();
         if (arr.size() != len) {
            throw new TranslatableException("error.custommodel.loadmodelpack.jsonarraylen", arr, len);
         } else {
            float[] floatArr = new float[arr.size()];

            for(int i = 0; i < floatArr.length; ++i) {
               floatArr[i] = arr.get(i).getAsFloat();
            }

            return floatArr;
         }
      }
   }

   public static IExpressionFloat[] parseFloatExpressionArray(JsonElement jsonElement, int len, float[] def, ExpressionParser parser) throws ParseException {
      if (jsonElement == null) {
         IExpressionFloat[] floatArr = new IExpressionFloat[len];
         for (int i = 0; i < len; i++)
            floatArr[i] = new ConstantFloat(def[i]);
         return floatArr;
      } else {
         JsonArray arr = jsonElement.getAsJsonArray();
         if (arr.size() != len) {
            throw new TranslatableException("error.custommodel.loadmodelpack.jsonarraylen", arr, len);
         } else {
            IExpressionFloat[] floatArr = new IExpressionFloat[arr.size()];
            for(int i = 0; i < floatArr.length; ++i)
               floatArr[i] = getFloatExpression(arr.get(i), def[i], parser);
            return floatArr;
         }
      }
   }

   public static double[] parseDoubleArray(JsonElement jsonElement, int len) {
      return parseDoubleArray(jsonElement, len, (double[])null);
   }

   public static double[] parseDoubleArray(JsonElement jsonElement, int len, double[] def) {
      if (jsonElement == null) {
         return def;
      } else {
         JsonArray arr = jsonElement.getAsJsonArray();
         if (arr.size() != len) {
            throw new TranslatableException("error.custommodel.loadmodelpack.jsonarraylen", arr, len);
         } else {
            double[] floatArr = new double[arr.size()];

            for(int i = 0; i < floatArr.length; ++i) {
               floatArr[i] = arr.get(i).getAsFloat();
            }

            return floatArr;
         }
      }
   }

   public static int[] parseIntArray(JsonElement jsonElement, int len) {
      return parseIntArray(jsonElement, len, (int[])null);
   }

   public static int[] parseIntArray(JsonElement jsonElement, int len, int[] def) {
      if (jsonElement == null) {
         return def;
      } else {
         JsonArray arr = jsonElement.getAsJsonArray();
         if (arr.size() != len) {
            throw new TranslatableException("error.custommodel.loadmodelpack.jsonarraylen", arr, len);
         } else {
            int[] intArr = new int[arr.size()];

            for(int i = 0; i < intArr.length; ++i) {
               intArr[i] = arr.get(i).getAsInt();
            }

            return intArr;
         }
      }
   }
}
