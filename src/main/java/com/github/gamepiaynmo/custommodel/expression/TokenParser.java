package com.github.gamepiaynmo.custommodel.expression;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;

public class TokenParser {
   public static Token[] parse(String str) throws IOException, ParseException {
      Reader r = new StringReader(str);
      PushbackReader pr = new PushbackReader(r);
      ArrayList list = new ArrayList();

      while(true) {
         int i = pr.read();
         if (i < 0) {
            Token[] tokens = (Token[])((Token[])list.toArray(new Token[list.size()]));
            return tokens;
         }

         char ch = (char)i;
         if (!Character.isWhitespace(ch)) {
            TokenType type = TokenType.getTypeByFirstChar(ch);
            if (type == null) {
               throw new ParseException("Invalid character: '" + ch + "', in: " + str);
            }

            Token token = readToken(ch, type, pr);
            list.add(token);
         }
      }
   }

   private static Token readToken(char chFirst, TokenType type, PushbackReader pr) throws IOException {
      StringBuffer sb = new StringBuffer();
      sb.append(chFirst);

      while(true) {
         int i = pr.read();
         if (i < 0) {
            break;
         }

         char ch = (char)i;
         if (!type.hasCharNext(ch)) {
            pr.unread(ch);
            break;
         }

         sb.append(ch);
      }

      return new Token(type, sb.toString());
   }
}
