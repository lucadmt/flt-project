package st169656.analyzers;

import java.io.*;
import st169656.clockwork.Token;
import st169656.clockwork.Tag;
import st169656.clockwork.Utils;
import st169656.clockwork.OpCode;
import st169656.clockwork.CodeGenerator;

public class ExpressionTranslator
  {
    private Lexer lex;
    private BufferedReader pbr;
    private Token look;
    
    CodeGenerator code = new CodeGenerator();
    
    public ExpressionTranslator (Lexer l, BufferedReader br)
      {
        lex = l;
        pbr = br;
        move();
      }
    
    void move ()
      {
        look = lex.lexical_scan(pbr);
        System.out.println("token = " + look);
      }
    
    void error (String s)
      {
        throw new Error("\n\nnear line " + lex.line + ": " + s);
      }
    
    void match (int t)
      {
        if (look.tag == t)
          {
            if (look.tag != Tag.EOF)
              move();
          } else
          error("syntax error (match), expected: " + t);
      }
    
    public void prog ()
      {
        if (look.tag == Tag.ID || look.tag == Tag.PRINT || look.tag == Tag.READ || look.tag == Tag.IF
                || look.tag == Tag.FOR || look.tag == Tag.BEGIN)
          {
            match(Tag.PRINT);
            match('(');
            expr();
            code.emit(OpCode.invokestatic, 1);
            match(')');
            match(Tag.EOF);
            try
              {
                code.toJasmin();
              } catch (java.io.IOException e)
              {
                System.out.println("IO error\n");
              }
          } 
          else
            error("syntax error (prog)");
      }
    
    private void expr ()
      {
        if (look.tag == '(' || look.tag == Tag.NUM || look.tag == Tag.ID)
          {
            term();
            exprp();
          }
          else
            error("syntax error(expr)");
      }
    
    private void exprp ()
      {
        if (look.tag == '+' || look.tag == '-' || look.tag == Tag.RELOP || look.tag == ';'
                || look.tag == ')' || look.tag == Tag.THEN || look.tag == Tag.ELSE || look.tag == Tag.END)
          {
            switch (look.tag)
              {
                case '+':
                  match('+');
                  term();
                  code.emit(OpCode.iadd);
                  exprp();
                  break;
                
                case '-':
                  match('-');
                  term();
                  code.emit(OpCode.isub);
                  exprp();
                  break;
              }
          }
          else
            error("syntax error(exprp)");
      }
    
    private void term ()
      {
        if (look.tag == '(' || look.tag == Tag.NUM || look.tag == Tag.ID)
          {
            fact();
            termp();
          }
          else
            error("syntax error (term)");
      }
    
    private void termp ()
      {
        if (look.tag == '*' || look.tag == '-' || look.tag == '/' || look.tag == '+' || look.tag == ';' || look.tag == Tag.RELOP || look.tag == ')'
                || look.tag == Tag.THEN || look.tag == Tag.END)
          {
            switch (look.tag)
              {
                case '*':
                  match(look.tag);
                  fact();
                  code.emit(OpCode.imul);
                  termp();
                  break;
                
                case '/':
                  match(look.tag);
                  fact();
                  code.emit(OpCode.idiv);
                  termp();
                  break;
              }
          }
          else
            error("syntax error(termp)");
      }
    
    private void fact ()
      {
        if (look.tag == '(' || look.tag == Tag.NUM || look.tag == Tag.ID)
          {
            switch (look.tag)
              {
                case '(':
                  match('(');
                  expr();
                  code.emit(OpCode.invokestatic, 1);
                  match(')');
                  break;
                
                case Tag.NUM:
                  match(look.tag);
                  int t = lex.value;
                  code.emit(OpCode.ldc, t);
                  break;
              }
          }
          else
            error("syntax error(fact)");
      }
    
    public static void main(String [] args)
      {
        Lexer lex = new Lexer();
        String path = "samples/source.et"; // il percorso del file da leggere
  
        try
          {
            // BufferedReader br = Utils.removeComments(new BufferedReader(new FileReader(path)));
            BufferedReader br = new BufferedReader(new FileReader(path));
            ExpressionTranslator et = new ExpressionTranslator(lex, br);
            et.prog();
            System.out.println("Code Accepted!");
            br.close();
          } catch (IOException e) {e.printStackTrace();}
      }
  }
