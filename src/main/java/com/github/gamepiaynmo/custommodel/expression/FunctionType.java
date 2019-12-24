package com.github.gamepiaynmo.custommodel.expression;

import com.github.gamepiaynmo.custommodel.client.CustomModelClient;
import com.github.gamepiaynmo.custommodel.render.RenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.MathHelper;

import java.util.function.BiFunction;
import java.util.function.Function;

public enum FunctionType {
   PLUS(10, ExpressionType.FLOAT, "+", (args, context) -> evalFloat(args, 0, context) + evalFloat(args, 1, context), ExpressionType.FLOAT, ExpressionType.FLOAT),
   MINUS(10, ExpressionType.FLOAT, "-", (args, context) -> evalFloat(args, 0, context) - evalFloat(args, 1, context), ExpressionType.FLOAT, ExpressionType.FLOAT),
   MUL(11, ExpressionType.FLOAT, "*", (args, context) -> evalFloat(args, 0, context) * evalFloat(args, 1, context), ExpressionType.FLOAT, ExpressionType.FLOAT),
   DIV(11, ExpressionType.FLOAT, "/", (args, context) -> evalFloat(args, 0, context) / evalFloat(args, 1, context), ExpressionType.FLOAT, ExpressionType.FLOAT),
   MOD(11, ExpressionType.FLOAT, "%", (args, context) -> {
      float modX = evalFloat(args, 0, context);
      float modY = evalFloat(args, 1, context);
      return modX - modY * (float)((int)(modX / modY));
   }, ExpressionType.FLOAT, ExpressionType.FLOAT),
   NEG(12, ExpressionType.FLOAT, "neg", (args, context) -> -evalFloat(args, 0, context), ExpressionType.FLOAT),
   PI(ExpressionType.FLOAT, "pi", (args, context) -> (float) Math.PI),
   SIN(ExpressionType.FLOAT, "sin", (args, context)-> MathHelper.sin(evalFloat(args, 0, context)), ExpressionType.FLOAT),
   COS(ExpressionType.FLOAT, "cos", (args, context) -> MathHelper.cos(evalFloat(args, 0, context)), ExpressionType.FLOAT),
   ASIN(ExpressionType.FLOAT, "asin", (args, context) -> (float) Math.asin(evalFloat(args, 0, context)), ExpressionType.FLOAT),
   ACOS(ExpressionType.FLOAT, "acos", (args, context) -> (float) Math.acos(evalFloat(args, 0, context)), ExpressionType.FLOAT),
   TAN(ExpressionType.FLOAT, "tan", (args, context) -> (float) Math.tan(evalFloat(args, 0, context)), ExpressionType.FLOAT),
   ATAN(ExpressionType.FLOAT, "atan", (args, context) -> (float) Math.atan(evalFloat(args, 0, context)), ExpressionType.FLOAT),
   ATAN2(ExpressionType.FLOAT, "atan2", (args, context) -> (float) MathHelper.atan2(evalFloat(args, 0, context), evalFloat(args, 1, context)), ExpressionType.FLOAT, ExpressionType.FLOAT),
   TORAD(ExpressionType.FLOAT, "torad", (args, context) -> FunctionType.D2R * evalFloat(args, 0, context), ExpressionType.FLOAT),
   TODEG(ExpressionType.FLOAT, "todeg", (args, context) -> FunctionType.R2D * evalFloat(args, 0, context), ExpressionType.FLOAT),
   MIN(ExpressionType.FLOAT, "min", FunctionType::getMin, (new ParametersVariable()).first(ExpressionType.FLOAT).repeat(ExpressionType.FLOAT)),
   MAX(ExpressionType.FLOAT, "max", FunctionType::getMax, (new ParametersVariable()).first(ExpressionType.FLOAT).repeat(ExpressionType.FLOAT)),
   CLAMP(ExpressionType.FLOAT, "clamp", (args, context) -> MathHelper.clamp(evalFloat(args, 0, context), evalFloat(args, 1, context), evalFloat(args, 2, context)), ExpressionType.FLOAT, ExpressionType.FLOAT, ExpressionType.FLOAT),
   ABS(ExpressionType.FLOAT, "abs", (args, context) -> MathHelper.abs(evalFloat(args, 0, context)), ExpressionType.FLOAT),
   FLOOR(ExpressionType.FLOAT, "floor", (args, context) -> (float) MathHelper.floor(evalFloat(args, 0, context)), ExpressionType.FLOAT),
   CEIL(ExpressionType.FLOAT, "ceil", (args, context) -> (float) MathHelper.ceil(evalFloat(args, 0, context)), ExpressionType.FLOAT),
   EXP(ExpressionType.FLOAT, "exp", (args, context) -> (float) Math.exp(evalFloat(args, 0, context)), ExpressionType.FLOAT),
   FRAC(ExpressionType.FLOAT, "frac", (args, context) -> (float) MathHelper.fractionalPart(evalFloat(args, 0, context)), ExpressionType.FLOAT),
   LOG(ExpressionType.FLOAT, "log", (args, context) -> (float) Math.log(evalFloat(args, 0, context)), ExpressionType.FLOAT),
   POW(ExpressionType.FLOAT, "pow", (args, context) -> (float) Math.pow(evalFloat(args, 0, context), evalFloat(args, 1, context)), ExpressionType.FLOAT, ExpressionType.FLOAT),
   RANDOM(ExpressionType.FLOAT, "random", (args, context) -> (float) Math.random()),
   ROUND(ExpressionType.FLOAT, "round", (args, context) -> (float) Math.round(evalFloat(args, 0, context)), ExpressionType.FLOAT),
   SIGNUM(ExpressionType.FLOAT, "signum", (args, context) -> Math.signum(evalFloat(args, 0, context)), ExpressionType.FLOAT),
   SQRT(ExpressionType.FLOAT, "sqrt", (args, context) -> MathHelper.sqrt(evalFloat(args, 0, context)), ExpressionType.FLOAT),
   FMOD(ExpressionType.FLOAT, "fmod", (args, context) -> {
      float fmodX = evalFloat(args, 0, context);
      float fmodY = evalFloat(args, 1, context);
      return fmodX - fmodY * (float)MathHelper.floor(fmodX / fmodY);
   }, ExpressionType.FLOAT, ExpressionType.FLOAT),
   TIME(ExpressionType.FLOAT, "time", (args, context) -> {
      MinecraftClient mc = MinecraftClient.getInstance();
      ClientWorld world = mc.world;
      if (world == null)
         return 0.0F;
      return (float)(world.getTime() % 24000L) + mc.getTickDelta();
   }),
   IF(ExpressionType.FLOAT, "if", (args, context) -> {
      int countChecks = (args.length - 1) / 2;
      for(int i = 0; i < countChecks; ++i) {
         int index = i * 2;
         if (evalBool(args, index, context))
            return evalFloat(args, index + 1, context);
      }
      return evalFloat(args, countChecks * 2, context);
   }, (new ParametersVariable()).first(ExpressionType.BOOL, ExpressionType.FLOAT).repeat(ExpressionType.BOOL, ExpressionType.FLOAT).last(ExpressionType.FLOAT)),
   NOT(12, ExpressionType.BOOL, "!", (args, context) -> !evalBool(args, 0, context), ExpressionType.BOOL),
   AND(3, ExpressionType.BOOL, "&&", (args, context) -> evalBool(args, 0, context) && evalBool(args, 1, context), ExpressionType.BOOL, ExpressionType.BOOL),
   OR(2, ExpressionType.BOOL, "||", (args, context) -> evalBool(args, 0, context) || evalBool(args, 1, context), ExpressionType.BOOL, ExpressionType.BOOL),
   GREATER(8, ExpressionType.BOOL, ">", (args, context) -> evalFloat(args, 0, context) > evalFloat(args, 1, context) + 1e-6f, ExpressionType.FLOAT, ExpressionType.FLOAT),
   GREATER_OR_EQUAL(8, ExpressionType.BOOL, ">=", (args, context) -> evalFloat(args, 0, context) >= evalFloat(args, 1, context) - 1e-6f, ExpressionType.FLOAT, ExpressionType.FLOAT),
   SMALLER(8, ExpressionType.BOOL, "<", (args, context) -> evalFloat(args, 0, context) < evalFloat(args, 1, context) - 1e-6f, ExpressionType.FLOAT, ExpressionType.FLOAT),
   SMALLER_OR_EQUAL(8, ExpressionType.BOOL, "<=", (args, context) -> evalFloat(args, 0, context) <= evalFloat(args, 1, context) + 1e-6f, ExpressionType.FLOAT, ExpressionType.FLOAT),
   EQUAL(7, ExpressionType.BOOL, "==", (args, context) -> Math.abs(evalFloat(args, 0, context) - evalFloat(args, 1, context)) < 1e-6f, ExpressionType.FLOAT, ExpressionType.FLOAT),
   NOT_EQUAL(7, ExpressionType.BOOL, "!=", (args, context) -> Math.abs(evalFloat(args, 0, context) - evalFloat(args, 1, context)) >= 1e-6f, ExpressionType.FLOAT, ExpressionType.FLOAT),
   BETWEEN(7, ExpressionType.BOOL, "between", (args, context) -> {
      float val = evalFloat(args, 0, context);
      return val >= evalFloat(args, 1, context) - 1e-6f && val <= evalFloat(args, 2, context) + 1e-6f;
   }, ExpressionType.FLOAT, ExpressionType.FLOAT, ExpressionType.FLOAT),
   EQUALS(7, ExpressionType.BOOL, "equals", (args, context) -> {
      float diff = evalFloat(args, 0, context) - evalFloat(args, 1, context);
      float delta = evalFloat(args, 2, context);
      return Math.abs(diff) <= delta;
   }, ExpressionType.FLOAT, ExpressionType.FLOAT, ExpressionType.FLOAT),
   IN(ExpressionType.BOOL, "in", (args, context) -> {
      float valIn = evalFloat(args, 0, context);
      for(int i = 1; i < args.length; ++i) {
         float valCheck = evalFloat(args, i, context);
         if (Math.abs(valIn - valCheck) < 1e-6f)
            return true;
      }
      return false;
   }, (new ParametersVariable()).first(ExpressionType.FLOAT).repeat(ExpressionType.FLOAT).last(ExpressionType.FLOAT)),
   TRUE(ExpressionType.BOOL, "true", (args, context) -> true),
   FALSE(ExpressionType.BOOL, "false", (args, context) -> false);

   private int precedence;
   private ExpressionType expressionType;
   private String name;
   private IParameters parameters;
   public static FunctionType[] VALUES = values();
   private BiFunction<IExpression[], RenderContext, Object> evaluator;

   private static final float R2D = 180 / (float) Math.PI;
   private static final float D2R = (float) Math.PI / 180;

   FunctionType(ExpressionType expressionType, String name, BiFunction<IExpression[], RenderContext, Object> eval, ExpressionType... parameterTypes) {
      this(0, expressionType, name, eval, parameterTypes);
   }

   FunctionType(int precedence, ExpressionType expressionType, String name, BiFunction<IExpression[], RenderContext, Object> eval, ExpressionType... parameterTypes) {
      this(precedence, expressionType, name, eval, new Parameters(parameterTypes));
   }

   FunctionType(ExpressionType expressionType, String name, BiFunction<IExpression[], RenderContext, Object> eval, IParameters parameters) {
      this(0, expressionType, name, eval, parameters);
   }

   FunctionType(int precedence, ExpressionType expressionType, String name, BiFunction<IExpression[], RenderContext, Object> eval, IParameters parameters) {
      this.precedence = precedence;
      this.expressionType = expressionType;
      this.name = name;
      this.parameters = parameters;
      this.evaluator = eval;
   }

   public String getName() {
      return this.name;
   }

   public int getPrecedence() {
      return this.precedence;
   }

   public ExpressionType getExpressionType() {
      return this.expressionType;
   }

   public IParameters getParameters() {
      return this.parameters;
   }

   public int getParameterCount(IExpression[] arguments) {
      return this.parameters.getParameterTypes(arguments).length;
   }

   public ExpressionType[] getParameterTypes(IExpression[] arguments) {
      return this.parameters.getParameterTypes(arguments);
   }

   public float evalFloat(IExpression[] args, RenderContext context) {
      if (expressionType == ExpressionType.FLOAT) {
         return (float) evaluator.apply(args, context);
      } else {
         CustomModelClient.LOGGER.warn("Unknown function type: " + this);
         return 0.0F;
      }
   }

   private static float getMin(IExpression[] exprs, RenderContext context) {
      if (exprs.length == 2) {
         return Math.min(evalFloat(exprs, 0, context), evalFloat(exprs, 1, context));
      } else {
         float valMin = evalFloat(exprs, 0, context);

         for(int i = 1; i < exprs.length; ++i) {
            float valExpr = evalFloat(exprs, i, context);
            if (valExpr < valMin) {
               valMin = valExpr;
            }
         }

         return valMin;
      }
   }

   private static float getMax(IExpression[] exprs, RenderContext context) {
      if (exprs.length == 2) {
         return Math.max(evalFloat(exprs, 0, context), evalFloat(exprs, 1, context));
      } else {
         float valMax = evalFloat(exprs, 0, context);

         for(int i = 1; i < exprs.length; ++i) {
            float valExpr = evalFloat(exprs, i, context);
            if (valExpr > valMax) {
               valMax = valExpr;
            }
         }

         return valMax;
      }
   }

   private static float evalFloat(IExpression[] exprs, int index, RenderContext context) {
      IExpressionFloat ef = (IExpressionFloat)exprs[index];
      float val = ef.eval(context);
      return val;
   }

   public boolean evalBool(IExpression[] args, RenderContext context) {
      if (expressionType == ExpressionType.BOOL) {
         return (boolean) evaluator.apply(args, context);
      } else {
         CustomModelClient.LOGGER.warn("Unknown function type: " + this);
         return false;
      }
   }

   private static boolean evalBool(IExpression[] exprs, int index, RenderContext context) {
      IExpressionBool eb = (IExpressionBool)exprs[index];
      boolean val = eb.eval(context);
      return val;
   }

   public static FunctionType parse(String str) {
      for (FunctionType ef : VALUES) {
         if (ef.getName().equals(str)) {
            return ef;
         }
      }

      return null;
   }
}
