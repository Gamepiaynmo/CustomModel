package com.github.gamepiaynmo.custommodel.expression;

import com.github.gamepiaynmo.custommodel.util.TranslatableException;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class ExpressionParser {
   private IExpressionResolver expressionResolver;

   public ExpressionParser(IExpressionResolver expressionResolver) {
      this.expressionResolver = expressionResolver;
   }

   public IExpressionFloat parseFloat(String str) throws ParseException {
      IExpression expr = this.parse(str);
      if (!(expr instanceof IExpressionFloat)) {
         throw new TranslatableException("error.custommodel.parse.notfloat", expr.getExpressionType());
      } else {
         return (IExpressionFloat)expr;
      }
   }

   public IExpressionBool parseBool(String str) throws ParseException {
      IExpression expr = this.parse(str);
      if (!(expr instanceof IExpressionBool)) {
         throw new TranslatableException("error.custommodel.parse.notbool", expr.getExpressionType());
      } else {
         return (IExpressionBool)expr;
      }
   }

   public IExpression parse(String str) throws ParseException {
      try {
         Token[] tokens = TokenParser.parse(str);
         if (tokens == null) {
            return null;
         } else {
            Deque deque = new ArrayDeque(Arrays.asList(tokens));
            return this.parseInfix(deque);
         }
      } catch (IOException var4) {
         throw new ParseException(var4.getMessage(), var4);
      }
   }

   private IExpression parseInfix(Deque deque) throws ParseException {
      if (deque.isEmpty()) {
         return null;
      } else {
         List listExpr = new LinkedList();
         List listOperTokens = new LinkedList();
         IExpression expr = this.parseExpression(deque);
         checkNull(expr, "error.custommodel.parse.missexpr");
         listExpr.add(expr);

         while(true) {
            Token tokenOper = (Token)deque.poll();
            if (tokenOper == null) {
               return this.makeInfix(listExpr, listOperTokens);
            }

            if (tokenOper.getType() != TokenType.OPERATOR) {
               throw new TranslatableException("error.custommodel.parse.invalidop", tokenOper);
            }

            IExpression expr2 = this.parseExpression(deque);
            checkNull(expr2, "error.custommodel.parse.missexpr");
            listOperTokens.add(tokenOper);
            listExpr.add(expr2);
         }
      }
   }

   private IExpression makeInfix(List listExpr, List listOper) throws ParseException {
      List listFunc = new LinkedList();
      Iterator it = listOper.iterator();

      while(it.hasNext()) {
         Token token = (Token)it.next();
         FunctionType type = FunctionType.parse(token.getText());
         checkNull(type, "error.custommodel.parse.invalidop", token);
         listFunc.add(type);
      }

      return this.makeInfixFunc(listExpr, listFunc);
   }

   private IExpression makeInfixFunc(List listExpr, List listFunc) throws ParseException {
      if (listExpr.size() != listFunc.size() + 1) {
         throw new TranslatableException("error.custommodel.parse.invalidinfix", listExpr.size(), listFunc.size());
      } else if (listExpr.size() == 1) {
         return (IExpression)listExpr.get(0);
      } else {
         int minPrecedence = Integer.MAX_VALUE;
         int maxPrecedence = Integer.MIN_VALUE;

         FunctionType type;
         for(Iterator it = listFunc.iterator(); it.hasNext(); maxPrecedence = Math.max(type.getPrecedence(), maxPrecedence)) {
            type = (FunctionType)it.next();
            minPrecedence = Math.min(type.getPrecedence(), minPrecedence);
         }

         if (maxPrecedence >= minPrecedence && maxPrecedence - minPrecedence <= 10) {
            for(int i = maxPrecedence; i >= minPrecedence; --i) {
               this.mergeOperators(listExpr, listFunc, i);
            }

            if (listExpr.size() == 1 && listFunc.size() == 0) {
               return (IExpression)listExpr.get(0);
            } else {
               throw new TranslatableException("error.custommodel.parse.mergeop", listExpr.size(), listFunc.size());
            }
         } else {
            throw new TranslatableException("error.custommodel.parse.invalidprec", minPrecedence, maxPrecedence);
         }
      }
   }

   private void mergeOperators(List listExpr, List listFuncs, int precedence) throws ParseException {
      for(int i = 0; i < listFuncs.size(); ++i) {
         FunctionType type = (FunctionType)listFuncs.get(i);
         if (type.getPrecedence() == precedence) {
            listFuncs.remove(i);
            IExpression expr1 = (IExpression)listExpr.remove(i);
            IExpression expr2 = (IExpression)listExpr.remove(i);
            IExpression exprOper = makeFunction(type, new IExpression[]{expr1, expr2});
            listExpr.add(i, exprOper);
            --i;
         }
      }

   }

   private IExpression parseExpression(Deque deque) throws ParseException {
      Token token = (Token)deque.poll();
      checkNull(token, "error.custommodel.parse.missexpr");
      switch(token.getType()) {
      case NUMBER:
         return makeConstantFloat(token);
      case IDENTIFIER:
         FunctionType type = this.getFunctionType(token, deque);
         if (type != null) {
            return this.makeFunction(type, deque);
         }

         return this.makeVariable(token);
      case BRACKET_OPEN:
         return this.makeBracketed(token, deque);
      case OPERATOR:
         FunctionType operType = FunctionType.parse(token.getText());
         checkNull(operType, "error.custommodel.parse.invalidop", token);
         if (operType == FunctionType.PLUS) {
            return this.parseExpression(deque);
         } else {
            IExpression exprNot;
            if (operType == FunctionType.MINUS) {
               exprNot = this.parseExpression(deque);
               return makeFunction(FunctionType.NEG, new IExpression[]{exprNot});
            } else if (operType == FunctionType.NOT) {
               exprNot = this.parseExpression(deque);
               return makeFunction(FunctionType.NOT, new IExpression[]{exprNot});
            }
         }
      default:
         throw new TranslatableException("error.custommodel.parse.invalidexpr", token);
      }
   }

   public static float parseFloat(String str, float defVal) {
      try {
         if (str == null) {
            return defVal;
         } else {
            str = str.trim();
            return Float.parseFloat(str);
         }
      } catch (NumberFormatException var3) {
         return defVal;
      }
   }

   private static IExpression makeConstantFloat(Token token) throws ParseException {
      float val = parseFloat(token.getText(), Float.NaN);
      if (val == Float.NaN) {
         throw new TranslatableException("error.custommodel.parse.invalidfloat", token);
      } else {
         return new ConstantFloat(val);
      }
   }

   private FunctionType getFunctionType(Token token, Deque deque) throws ParseException {
      Token tokenNext = (Token)deque.peek();
      FunctionType type;
      if (tokenNext != null && tokenNext.getType() == TokenType.BRACKET_OPEN) {
         type = FunctionType.parse(token.getText());
         checkNull(type, "error.custommodel.parse.invalidfunc", token);
         return type;
      } else {
         type = FunctionType.parse(token.getText());
         if (type == null) {
            return null;
         } else if (type.getParameterCount(new IExpression[0]) > 0) {
            throw new TranslatableException("error.custommodel.parse.missargs", type);
         } else {
            return type;
         }
      }
   }

   private IExpression makeFunction(FunctionType type, Deque deque) throws ParseException {
      Token tokenNext;
      if (type.getParameterCount(new IExpression[0]) == 0) {
         tokenNext = (Token)deque.peek();
         if (tokenNext == null || tokenNext.getType() != TokenType.BRACKET_OPEN) {
            return makeFunction(type, new IExpression[0]);
         }
      }

      tokenNext = (Token)deque.poll();
      Deque dequeBracketed = getGroup(deque, TokenType.BRACKET_CLOSE, true);
      IExpression[] exprs = this.parseExpressions(dequeBracketed);
      return makeFunction(type, exprs);
   }

   private IExpression[] parseExpressions(Deque deque) throws ParseException {
      ArrayList list = new ArrayList();

      while(true) {
         Deque dequeArg = getGroup(deque, TokenType.COMMA, false);
         IExpression expr = this.parseInfix(dequeArg);
         if (expr == null) {
            IExpression[] exprs = (IExpression[]) list.toArray(new IExpression[list.size()]);
            return exprs;
         }

         list.add(expr);
      }
   }

   private static IExpression makeFunction(FunctionType type, IExpression[] args) throws ParseException {
      ExpressionType[] funcParamTypes = type.getParameterTypes(args);
      if (args.length != funcParamTypes.length) {
         throw new TranslatableException("error.custommodel.parse.numargs", type.getName(), args.length, funcParamTypes.length);
      } else {
         for(int i = 0; i < args.length; ++i) {
            IExpression arg = args[i];
            ExpressionType argType = arg.getExpressionType();
            ExpressionType funcParamType = funcParamTypes[i];
            if (argType != funcParamType) {
               throw new TranslatableException("error.custommodel.parse.argtype", type.getName(), i, argType, funcParamType);
            }
         }

         if (type.getExpressionType() == ExpressionType.FLOAT) {
            return new FunctionFloat(type, args);
         } else if (type.getExpressionType() == ExpressionType.BOOL) {
            return new FunctionBool(type, args);
         } else {
            throw new TranslatableException("error.custommodel.parse.functype", type.getExpressionType(), type.getName());
         }
      }
   }

   private IExpression makeVariable(Token token) throws ParseException {
      if (this.expressionResolver == null) {
         throw new TranslatableException("error.custommodel.parse.invalidvar", token);
      } else {
         IExpression expr = this.expressionResolver.getExpression(token.getText());
         if (expr == null) {
            throw new TranslatableException("error.custommodel.parse.invalidvar", token);
         } else {
            return expr;
         }
      }
   }

   private IExpression makeBracketed(Token token, Deque deque) throws ParseException {
      Deque dequeBracketed = getGroup(deque, TokenType.BRACKET_CLOSE, true);
      return this.parseInfix(dequeBracketed);
   }

   private static Deque getGroup(Deque deque, TokenType tokenTypeEnd, boolean tokenEndRequired) throws ParseException {
      Deque dequeGroup = new ArrayDeque();
      int level = 0;
      Iterator it = deque.iterator();

      while(it.hasNext()) {
         Token token = (Token)it.next();
         it.remove();
         if (level == 0 && token.getType() == tokenTypeEnd) {
            return dequeGroup;
         }

         dequeGroup.add(token);
         if (token.getType() == TokenType.BRACKET_OPEN) {
            ++level;
         }

         if (token.getType() == TokenType.BRACKET_CLOSE) {
            --level;
         }
      }

      if (tokenEndRequired) {
         throw new TranslatableException("error.custommodel.parse.missend", tokenTypeEnd);
      } else {
         return dequeGroup;
      }
   }

   private static void checkNull(Object obj, String message, Object... args) throws ParseException {
      if (obj == null) {
         throw new TranslatableException(message, args);
      }
   }
}
