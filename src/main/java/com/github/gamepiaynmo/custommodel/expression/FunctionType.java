package com.github.gamepiaynmo.custommodel.expression;

import com.github.gamepiaynmo.custommodel.client.CustomModelClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.MathHelper;

import java.util.HashMap;
import java.util.Map;

public enum FunctionType {
   PLUS(10, ExpressionType.FLOAT, "+", new ExpressionType[]{ExpressionType.FLOAT, ExpressionType.FLOAT}),
   MINUS(10, ExpressionType.FLOAT, "-", new ExpressionType[]{ExpressionType.FLOAT, ExpressionType.FLOAT}),
   MUL(11, ExpressionType.FLOAT, "*", new ExpressionType[]{ExpressionType.FLOAT, ExpressionType.FLOAT}),
   DIV(11, ExpressionType.FLOAT, "/", new ExpressionType[]{ExpressionType.FLOAT, ExpressionType.FLOAT}),
   MOD(11, ExpressionType.FLOAT, "%", new ExpressionType[]{ExpressionType.FLOAT, ExpressionType.FLOAT}),
   NEG(12, ExpressionType.FLOAT, "neg", new ExpressionType[]{ExpressionType.FLOAT}),
   PI(ExpressionType.FLOAT, "pi", new ExpressionType[0]),
   SIN(ExpressionType.FLOAT, "sin", new ExpressionType[]{ExpressionType.FLOAT}),
   COS(ExpressionType.FLOAT, "cos", new ExpressionType[]{ExpressionType.FLOAT}),
   ASIN(ExpressionType.FLOAT, "asin", new ExpressionType[]{ExpressionType.FLOAT}),
   ACOS(ExpressionType.FLOAT, "acos", new ExpressionType[]{ExpressionType.FLOAT}),
   TAN(ExpressionType.FLOAT, "tan", new ExpressionType[]{ExpressionType.FLOAT}),
   ATAN(ExpressionType.FLOAT, "atan", new ExpressionType[]{ExpressionType.FLOAT}),
   ATAN2(ExpressionType.FLOAT, "atan2", new ExpressionType[]{ExpressionType.FLOAT, ExpressionType.FLOAT}),
   TORAD(ExpressionType.FLOAT, "torad", new ExpressionType[]{ExpressionType.FLOAT}),
   TODEG(ExpressionType.FLOAT, "todeg", new ExpressionType[]{ExpressionType.FLOAT}),
   MIN(ExpressionType.FLOAT, "min", (new ParametersVariable()).first(ExpressionType.FLOAT).repeat(ExpressionType.FLOAT)),
   MAX(ExpressionType.FLOAT, "max", (new ParametersVariable()).first(ExpressionType.FLOAT).repeat(ExpressionType.FLOAT)),
   CLAMP(ExpressionType.FLOAT, "clamp", new ExpressionType[]{ExpressionType.FLOAT, ExpressionType.FLOAT, ExpressionType.FLOAT}),
   ABS(ExpressionType.FLOAT, "abs", new ExpressionType[]{ExpressionType.FLOAT}),
   FLOOR(ExpressionType.FLOAT, "floor", new ExpressionType[]{ExpressionType.FLOAT}),
   CEIL(ExpressionType.FLOAT, "ceil", new ExpressionType[]{ExpressionType.FLOAT}),
   EXP(ExpressionType.FLOAT, "exp", new ExpressionType[]{ExpressionType.FLOAT}),
   FRAC(ExpressionType.FLOAT, "frac", new ExpressionType[]{ExpressionType.FLOAT}),
   LOG(ExpressionType.FLOAT, "log", new ExpressionType[]{ExpressionType.FLOAT}),
   POW(ExpressionType.FLOAT, "pow", new ExpressionType[]{ExpressionType.FLOAT, ExpressionType.FLOAT}),
   RANDOM(ExpressionType.FLOAT, "random", new ExpressionType[0]),
   ROUND(ExpressionType.FLOAT, "round", new ExpressionType[]{ExpressionType.FLOAT}),
   SIGNUM(ExpressionType.FLOAT, "signum", new ExpressionType[]{ExpressionType.FLOAT}),
   SQRT(ExpressionType.FLOAT, "sqrt", new ExpressionType[]{ExpressionType.FLOAT}),
   FMOD(ExpressionType.FLOAT, "fmod", new ExpressionType[]{ExpressionType.FLOAT, ExpressionType.FLOAT}),
   TIME(ExpressionType.FLOAT, "time", new ExpressionType[0]),
   IF(ExpressionType.FLOAT, "if", (new ParametersVariable()).first(ExpressionType.BOOL, ExpressionType.FLOAT).repeat(ExpressionType.BOOL, ExpressionType.FLOAT).last(ExpressionType.FLOAT)),
   NOT(12, ExpressionType.BOOL, "!", new ExpressionType[]{ExpressionType.BOOL}),
   AND(3, ExpressionType.BOOL, "&&", new ExpressionType[]{ExpressionType.BOOL, ExpressionType.BOOL}),
   OR(2, ExpressionType.BOOL, "||", new ExpressionType[]{ExpressionType.BOOL, ExpressionType.BOOL}),
   GREATER(8, ExpressionType.BOOL, ">", new ExpressionType[]{ExpressionType.FLOAT, ExpressionType.FLOAT}),
   GREATER_OR_EQUAL(8, ExpressionType.BOOL, ">=", new ExpressionType[]{ExpressionType.FLOAT, ExpressionType.FLOAT}),
   SMALLER(8, ExpressionType.BOOL, "<", new ExpressionType[]{ExpressionType.FLOAT, ExpressionType.FLOAT}),
   SMALLER_OR_EQUAL(8, ExpressionType.BOOL, "<=", new ExpressionType[]{ExpressionType.FLOAT, ExpressionType.FLOAT}),
   EQUAL(7, ExpressionType.BOOL, "==", new ExpressionType[]{ExpressionType.FLOAT, ExpressionType.FLOAT}),
   NOT_EQUAL(7, ExpressionType.BOOL, "!=", new ExpressionType[]{ExpressionType.FLOAT, ExpressionType.FLOAT}),
   BETWEEN(7, ExpressionType.BOOL, "between", new ExpressionType[]{ExpressionType.FLOAT, ExpressionType.FLOAT, ExpressionType.FLOAT}),
   EQUALS(7, ExpressionType.BOOL, "equals", new ExpressionType[]{ExpressionType.FLOAT, ExpressionType.FLOAT, ExpressionType.FLOAT}),
   IN(ExpressionType.BOOL, "in", (new ParametersVariable()).first(ExpressionType.FLOAT).repeat(ExpressionType.FLOAT).last(ExpressionType.FLOAT)),
   SMOOTH(ExpressionType.FLOAT, "smooth", (new ParametersVariable()).first(ExpressionType.FLOAT).repeat(ExpressionType.FLOAT).maxCount(4)),
   TRUE(ExpressionType.BOOL, "true", new ExpressionType[0]),
   FALSE(ExpressionType.BOOL, "false", new ExpressionType[0]);

   private int precedence;
   private ExpressionType expressionType;
   private String name;
   private IParameters parameters;
   public static FunctionType[] VALUES = values();
   private static final Map mapSmooth = new HashMap();

   private final float R2D = 180 / (float) Math.PI;
   private final float D2R = (float) Math.PI / 180;

   private FunctionType(ExpressionType expressionType, String name, ExpressionType... parameterTypes) {
      this(0, expressionType, name, (ExpressionType[])parameterTypes);
   }

   private FunctionType(int precedence, ExpressionType expressionType, String name, ExpressionType... parameterTypes) {
      this(precedence, expressionType, name, (IParameters)(new Parameters(parameterTypes)));
   }

   private FunctionType(ExpressionType expressionType, String name, IParameters parameters) {
      this(0, expressionType, name, (IParameters)parameters);
   }

   private FunctionType(int precedence, ExpressionType expressionType, String name, IParameters parameters) {
      this.precedence = precedence;
      this.expressionType = expressionType;
      this.name = name;
      this.parameters = parameters;
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
      int i;
      switch(this) {
      case PLUS:
         return evalFloat(args, 0) + evalFloat(args, 1);
      case MINUS:
         return evalFloat(args, 0) - evalFloat(args, 1);
      case MUL:
         return evalFloat(args, 0) * evalFloat(args, 1);
      case DIV:
         return evalFloat(args, 0) / evalFloat(args, 1);
      case MOD:
         float modX = evalFloat(args, 0);
         float modY = evalFloat(args, 1);
         return modX - modY * (float)((int)(modX / modY));
      case NEG:
         return -evalFloat(args, 0);
      case PI:
         return (float) Math.PI;
      case SIN:
         return MathHelper.sin(evalFloat(args, 0));
      case COS:
         return MathHelper.cos(evalFloat(args, 0));
      case ASIN:
         return (float) Math.asin(evalFloat(args, 0));
      case ACOS:
         return (float) Math.acos(evalFloat(args, 0));
      case TAN:
         return (float) Math.tan(evalFloat(args, 0));
      case ATAN:
         return (float) Math.atan(evalFloat(args, 0));
      case ATAN2:
         return (float) MathHelper.atan2(evalFloat(args, 0), evalFloat(args, 1));
      case TORAD:
         return D2R * evalFloat(args, 0);
      case TODEG:
         return R2D * evalFloat(args, 0);
      case MIN:
         return this.getMin(args);
      case MAX:
         return this.getMax(args);
      case CLAMP:
         return MathHelper.clamp(evalFloat(args, 0), evalFloat(args, 1), evalFloat(args, 2));
      case ABS:
         return MathHelper.abs(evalFloat(args, 0));
      case EXP:
         return (float)Math.exp((double)evalFloat(args, 0));
      case FLOOR:
         return (float)MathHelper.floor(evalFloat(args, 0));
      case CEIL:
         return (float)MathHelper.ceil(evalFloat(args, 0));
      case FRAC:
         return (float)MathHelper.fractionalPart((double)evalFloat(args, 0));
      case LOG:
         return (float)Math.log((double)evalFloat(args, 0));
      case POW:
         return (float)Math.pow((double)evalFloat(args, 0), (double)evalFloat(args, 1));
      case RANDOM:
         return (float)Math.random();
      case ROUND:
         return (float)Math.round(evalFloat(args, 0));
      case SIGNUM:
         return Math.signum(evalFloat(args, 0));
      case SQRT:
         return MathHelper.sqrt(evalFloat(args, 0));
      case FMOD:
         float fmodX = evalFloat(args, 0);
         float fmodY = evalFloat(args, 1);
         return fmodX - fmodY * (float)MathHelper.floor(fmodX / fmodY);
      case TIME:
         MinecraftClient mc = MinecraftClient.getInstance();
         ClientWorld world = mc.world;
         if (world == null) {
            return 0.0F;
         }

         return (float)(world.getTime() % 24000L) + mc.getTickDelta();
      case IF:
         int countChecks = (args.length - 1) / 2;

         for(i = 0; i < countChecks; ++i) {
            int index = i * 2;
            if (evalBool(args, index)) {
               return evalFloat(args, index + 1);
            }
         }

         return evalFloat(args, countChecks * 2);
      default:
         CustomModelClient.LOGGER.warn("Unknown function type: " + this);
         return 0.0F;
      }
   }

   private float getMin(IExpression[] exprs) {
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

   private float getMax(IExpression[] exprs) {
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
      switch(this) {
      case TRUE:
         return true;
      case FALSE:
         return false;
      case NOT:
         return !evalBool(args, 0);
      case AND:
         return evalBool(args, 0) && evalBool(args, 1);
      case OR:
         return evalBool(args, 0) || evalBool(args, 1);
      case GREATER:
         return evalFloat(args, 0) > evalFloat(args, 1);
      case GREATER_OR_EQUAL:
         return evalFloat(args, 0) >= evalFloat(args, 1);
      case SMALLER:
         return evalFloat(args, 0) < evalFloat(args, 1);
      case SMALLER_OR_EQUAL:
         return evalFloat(args, 0) <= evalFloat(args, 1);
      case EQUAL:
         return evalFloat(args, 0) == evalFloat(args, 1);
      case NOT_EQUAL:
         return evalFloat(args, 0) != evalFloat(args, 1);
      case BETWEEN:
         float val = evalFloat(args, 0);
         return val >= evalFloat(args, 1) && val <= evalFloat(args, 2);
      case EQUALS:
         float diff = evalFloat(args, 0) - evalFloat(args, 1);
         float delta = evalFloat(args, 2);
         return Math.abs(diff) <= delta;
      case IN:
         float valIn = evalFloat(args, 0);

         for(int i = 1; i < args.length; ++i) {
            float valCheck = evalFloat(args, i);
            if (valIn == valCheck) {
               return true;
            }
         }

         return false;
      default:
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
