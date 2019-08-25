package com.github.gamepiaynmo.custommodel.expression;

import com.github.gamepiaynmo.custommodel.client.CustomModelClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.MathHelper;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public enum FunctionType {
   PLUS(10, ExpressionType.FLOAT, "+", args -> evalFloat(args, 0) + evalFloat(args, 1), ExpressionType.FLOAT, ExpressionType.FLOAT),
   MINUS(10, ExpressionType.FLOAT, "-", args -> evalFloat(args, 0) - evalFloat(args, 1), ExpressionType.FLOAT, ExpressionType.FLOAT),
   MUL(11, ExpressionType.FLOAT, "*", args -> evalFloat(args, 0) * evalFloat(args, 1), ExpressionType.FLOAT, ExpressionType.FLOAT),
   DIV(11, ExpressionType.FLOAT, "/", args -> evalFloat(args, 0) / evalFloat(args, 1), ExpressionType.FLOAT, ExpressionType.FLOAT),
   MOD(11, ExpressionType.FLOAT, "%", args -> {
      float modX = evalFloat(args, 0);
      float modY = evalFloat(args, 1);
      return modX - modY * (float)((int)(modX / modY));
   }, ExpressionType.FLOAT, ExpressionType.FLOAT),
   NEG(12, ExpressionType.FLOAT, "neg", args -> -evalFloat(args, 0), ExpressionType.FLOAT),
   PI(ExpressionType.FLOAT, "pi", args -> (float) Math.PI),
   SIN(ExpressionType.FLOAT, "sin", args-> MathHelper.sin(evalFloat(args, 0)), ExpressionType.FLOAT),
   COS(ExpressionType.FLOAT, "cos", args -> MathHelper.cos(evalFloat(args, 0)), ExpressionType.FLOAT),
   ASIN(ExpressionType.FLOAT, "asin", args -> (float) Math.asin(evalFloat(args, 0)), ExpressionType.FLOAT),
   ACOS(ExpressionType.FLOAT, "acos", args -> (float) Math.acos(evalFloat(args, 0)), ExpressionType.FLOAT),
   TAN(ExpressionType.FLOAT, "tan", args -> (float) Math.tan(evalFloat(args, 0)), ExpressionType.FLOAT),
   ATAN(ExpressionType.FLOAT, "atan", args -> (float) Math.atan(evalFloat(args, 0)), ExpressionType.FLOAT),
   ATAN2(ExpressionType.FLOAT, "atan2", args -> MathHelper.atan2(evalFloat(args, 0), evalFloat(args, 1)), ExpressionType.FLOAT, ExpressionType.FLOAT),
   TORAD(ExpressionType.FLOAT, "torad", args -> FunctionType.D2R * evalFloat(args, 0), ExpressionType.FLOAT),
   TODEG(ExpressionType.FLOAT, "todeg", args -> FunctionType.R2D * evalFloat(args, 0), ExpressionType.FLOAT),
   MIN(ExpressionType.FLOAT, "min", args -> getMin(args), (new ParametersVariable()).first(ExpressionType.FLOAT).repeat(ExpressionType.FLOAT)),
   MAX(ExpressionType.FLOAT, "max", args -> getMax(args), (new ParametersVariable()).first(ExpressionType.FLOAT).repeat(ExpressionType.FLOAT)),
   CLAMP(ExpressionType.FLOAT, "clamp", args -> MathHelper.clamp(evalFloat(args, 0), evalFloat(args, 1), evalFloat(args, 2)), ExpressionType.FLOAT, ExpressionType.FLOAT, ExpressionType.FLOAT),
   ABS(ExpressionType.FLOAT, "abs", args -> MathHelper.abs(evalFloat(args, 0)), ExpressionType.FLOAT),
   FLOOR(ExpressionType.FLOAT, "floor", args -> MathHelper.floor(evalFloat(args, 0)), ExpressionType.FLOAT),
   CEIL(ExpressionType.FLOAT, "ceil", args -> MathHelper.ceil(evalFloat(args, 0)), ExpressionType.FLOAT),
   EXP(ExpressionType.FLOAT, "exp", args -> (float) Math.exp((double)evalFloat(args, 0)), ExpressionType.FLOAT),
   FRAC(ExpressionType.FLOAT, "frac", args -> MathHelper.fractionalPart((double)evalFloat(args, 0)), ExpressionType.FLOAT),
   LOG(ExpressionType.FLOAT, "log", args -> (float) Math.log((double)evalFloat(args, 0)), ExpressionType.FLOAT),
   POW(ExpressionType.FLOAT, "pow", args -> (float) Math.pow((double)evalFloat(args, 0), (double)evalFloat(args, 1)), ExpressionType.FLOAT, ExpressionType.FLOAT),
   RANDOM(ExpressionType.FLOAT, "random", args -> (float) Math.random()),
   ROUND(ExpressionType.FLOAT, "round", args -> (float) Math.round(evalFloat(args, 0)), ExpressionType.FLOAT),
   SIGNUM(ExpressionType.FLOAT, "signum", args -> Math.signum(evalFloat(args, 0)), ExpressionType.FLOAT),
   SQRT(ExpressionType.FLOAT, "sqrt", args -> MathHelper.sqrt(evalFloat(args, 0)), ExpressionType.FLOAT),
   FMOD(ExpressionType.FLOAT, "fmod", args -> {
      float fmodX = evalFloat(args, 0);
      float fmodY = evalFloat(args, 1);
      return fmodX - fmodY * (float)MathHelper.floor(fmodX / fmodY);
   }, ExpressionType.FLOAT, ExpressionType.FLOAT),
   TIME(ExpressionType.FLOAT, "time", args -> {
      MinecraftClient mc = MinecraftClient.getInstance();
      ClientWorld world = mc.world;
      if (world == null)
         return 0.0F;
      return (float)(world.getTime() % 24000L) + mc.getTickDelta();
   }),
   IF(ExpressionType.FLOAT, "if", args -> {
      int countChecks = (args.length - 1) / 2;
      for(int i = 0; i < countChecks; ++i) {
         int index = i * 2;
         if (evalBool(args, index))
            return evalFloat(args, index + 1);
      }
      return evalFloat(args, countChecks * 2);
   }, (new ParametersVariable()).first(ExpressionType.BOOL, ExpressionType.FLOAT).repeat(ExpressionType.BOOL, ExpressionType.FLOAT).last(ExpressionType.FLOAT)),
   NOT(12, ExpressionType.BOOL, "!", args -> !evalBool(args, 0), ExpressionType.BOOL),
   AND(3, ExpressionType.BOOL, "&&", args -> evalBool(args, 0) && evalBool(args, 1), ExpressionType.BOOL, ExpressionType.BOOL),
   OR(2, ExpressionType.BOOL, "||", args -> evalBool(args, 0) || evalBool(args, 1), ExpressionType.BOOL, ExpressionType.BOOL),
   GREATER(8, ExpressionType.BOOL, ">", args -> evalFloat(args, 0) > evalFloat(args, 1), ExpressionType.FLOAT, ExpressionType.FLOAT),
   GREATER_OR_EQUAL(8, ExpressionType.BOOL, ">=", args -> evalFloat(args, 0) >= evalFloat(args, 1), ExpressionType.FLOAT, ExpressionType.FLOAT),
   SMALLER(8, ExpressionType.BOOL, "<", args -> evalFloat(args, 0) < evalFloat(args, 1), ExpressionType.FLOAT, ExpressionType.FLOAT),
   SMALLER_OR_EQUAL(8, ExpressionType.BOOL, "<=", args -> evalFloat(args, 0) <= evalFloat(args, 1), ExpressionType.FLOAT, ExpressionType.FLOAT),
   EQUAL(7, ExpressionType.BOOL, "==", args -> evalFloat(args, 0) == evalFloat(args, 1), ExpressionType.FLOAT, ExpressionType.FLOAT),
   NOT_EQUAL(7, ExpressionType.BOOL, "!=", args -> evalFloat(args, 0) != evalFloat(args, 1), ExpressionType.FLOAT, ExpressionType.FLOAT),
   BETWEEN(7, ExpressionType.BOOL, "between", args -> {
      float val = evalFloat(args, 0);
      return val >= evalFloat(args, 1) && val <= evalFloat(args, 2);
   }, ExpressionType.FLOAT, ExpressionType.FLOAT, ExpressionType.FLOAT),
   EQUALS(7, ExpressionType.BOOL, "equals", args -> {
      float diff = evalFloat(args, 0) - evalFloat(args, 1);
      float delta = evalFloat(args, 2);
      return Math.abs(diff) <= delta;
   }, ExpressionType.FLOAT, ExpressionType.FLOAT, ExpressionType.FLOAT),
   IN(ExpressionType.BOOL, "in", args -> {
      float valIn = evalFloat(args, 0);
      for(int i = 1; i < args.length; ++i) {
         float valCheck = evalFloat(args, i);
         if (valIn == valCheck)
            return true;
      }
      return false;
   }, (new ParametersVariable()).first(ExpressionType.FLOAT).repeat(ExpressionType.FLOAT).last(ExpressionType.FLOAT)),
   TRUE(ExpressionType.BOOL, "true", args -> true),
   FALSE(ExpressionType.BOOL, "false", args -> false);

   private int precedence;
   private ExpressionType expressionType;
   private String name;
   private IParameters parameters;
   public static FunctionType[] VALUES = values();
   private Function<IExpression[], Object> evaluator;

   private static final float R2D = 180 / (float) Math.PI;
   private static final float D2R = (float) Math.PI / 180;

   private FunctionType(ExpressionType expressionType, String name, Function<IExpression[], Object> eval, ExpressionType... parameterTypes) {
      this(0, expressionType, name, eval, parameterTypes);
   }

   private FunctionType(int precedence, ExpressionType expressionType, String name, Function<IExpression[], Object> eval, ExpressionType... parameterTypes) {
      this(precedence, expressionType, name, eval, new Parameters(parameterTypes));
   }

   private FunctionType(ExpressionType expressionType, String name, Function<IExpression[], Object> eval, IParameters parameters) {
      this(0, expressionType, name, eval, parameters);
   }

   private FunctionType(int precedence, ExpressionType expressionType, String name, Function<IExpression[], Object> eval, IParameters parameters) {
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

   public float evalFloat(IExpression[] args) {
      if (expressionType == ExpressionType.FLOAT) {
         return (float) evaluator.apply(args);
      } else {
         CustomModelClient.LOGGER.warn("Unknown function type: " + this);
         return 0.0F;
      }
   }

   private static float getMin(IExpression[] exprs) {
      if (exprs.length == 2) {
         return Math.min(evalFloat(exprs, 0), evalFloat(exprs, 1));
      } else {
         float valMin = evalFloat(exprs, 0);

         for(int i = 1; i < exprs.length; ++i) {
            float valExpr = evalFloat(exprs, i);
            if (valExpr < valMin) {
               valMin = valExpr;
            }
         }

         return valMin;
      }
   }

   private static float getMax(IExpression[] exprs) {
      if (exprs.length == 2) {
         return Math.max(evalFloat(exprs, 0), evalFloat(exprs, 1));
      } else {
         float valMax = evalFloat(exprs, 0);

         for(int i = 1; i < exprs.length; ++i) {
            float valExpr = evalFloat(exprs, i);
            if (valExpr > valMax) {
               valMax = valExpr;
            }
         }

         return valMax;
      }
   }

   private static float evalFloat(IExpression[] exprs, int index) {
      IExpressionFloat ef = (IExpressionFloat)exprs[index];
      float val = ef.eval();
      return val;
   }

   public boolean evalBool(IExpression[] args) {
      if (expressionType == ExpressionType.BOOL) {
         return (boolean) evaluator.apply(args);
      } else {
         CustomModelClient.LOGGER.warn("Unknown function type: " + this);
         return false;
      }
   }

   private static boolean evalBool(IExpression[] exprs, int index) {
      IExpressionBool eb = (IExpressionBool)exprs[index];
      boolean val = eb.eval();
      return val;
   }

   public static FunctionType parse(String str) {
      for(int i = 0; i < VALUES.length; ++i) {
         FunctionType ef = VALUES[i];
         if (ef.getName().equals(str)) {
            return ef;
         }
      }

      return null;
   }
}
