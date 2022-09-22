package st169656.clockwork;

import java.io.*;
import java.nio.charset.Charset;

public class Utils
  {
    
    public static final int LINE_COMMENT = 1;
    public static final int BLOCK_COMMENT_OPENED = 2;
    public static final int BLOCK_COMMENT_CLOSED = 3;
    public static final int ERR_NO_COMMENT = - 2;
    
    // Contains helper methods, to declutter Lexer.java
    public static boolean shouldAccept (char c)
      {
        return (c == '_' || Character.isLetter(c) || Character.isDigit(c));
      }
    
    public static int isComment (String s)
      {
        switch (s) {
          case "//":
            return LINE_COMMENT;
          case "/*":
            return BLOCK_COMMENT_OPENED;
          case "*/":
            return BLOCK_COMMENT_CLOSED;
          default:
            return ERR_NO_COMMENT;
        }
      }
    
    public static BufferedReader removeComments (BufferedReader br)
      {
        String code = "", loc = "", tmp = "";
        char ptr;
        try {
          // Before we get into lexing, let's remove comments, so we don't have to worry for them
          while ((loc = br.readLine()) != null) {
            for (int i = 0; i < loc.length(); i++) {
              ptr = loc.charAt(i);
              if (ptr == '/' && loc.charAt(i + 1) == '/') {
                for (int j = i; j < loc.length(); j++)
                  tmp += loc.charAt(j);
                loc = loc.replace(tmp, "");
                tmp = "";
              } else if (ptr == '/' && loc.charAt(i + 1) == '*') {
                int rel = loc.length();
                for (int k = i; k < rel; k++) {
                  if (loc.charAt(k) == '*' && loc.charAt(k + 1) == '/') {
                    tmp += "*/";
                    rel = k;
                  } else {
                    tmp += loc.charAt(k);
                  }
                }
                loc = loc.replace(tmp, "");
                tmp = "";
              }
            }
            code += loc;
          }
        } catch (IOException e) {
          System.out.println("OOPS, something went wrong...");
          System.out.println(e.getMessage());
        }
        return new BufferedReader(new InputStreamReader(new ByteArrayInputStream(code.getBytes(Charset.forName("UTF-8")))));
      }
    
    public static void debug (String s)
      {
        System.out.println("\n================\n");
        System.out.println(s + "\n");
      }
  }
