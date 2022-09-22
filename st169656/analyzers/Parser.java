package st169656.analyzers;

import java.io.*;
import st169656.clockwork.Token;
import st169656.clockwork.Tag;
import st169656.clockwork.Utils;

public class Parser
  {
    private Lexer lex;
    private BufferedReader pbr;
    private Token look;
    
    public Parser (Lexer l, BufferedReader br)
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
        throw new Error("\n\nnear line " + lex.line + ": " + s + " : " + look.tag + " / " + look);
      }
    
    void match (int t)
      {
        if (look.tag == t)
          {
            if (look.tag != Tag.EOF)
              move();
          } else
          error("syntax error (match), expected: " + t + "\n got: " + look.tag);
      }
    
    private void prog ()
      {
        if (look.tag == Tag.ID || look.tag == Tag.PRINT || look.tag == Tag.READ || look.tag == Tag.IF
                || look.tag == Tag.FOR || look.tag == Tag.BEGIN)
          {
            statlist();
            match(Tag.EOF);
          } else
          error("syntax error (prog)");
      }
    
    private void statlist ()
      {
        if (look.tag == Tag.ID || look.tag == Tag.PRINT || look.tag == Tag.READ || look.tag == Tag.IF
                || look.tag == Tag.FOR || look.tag == Tag.BEGIN)
          {
            stat();
            statlistp();
          } else
          error("syntax error(statlist)");
      }
    
    private void statlistp ()
      {
        if (look.tag == ';' || look.tag == Tag.END || look.tag == Tag.EOF || look.tag == Tag.IF)
          {
            switch (look.tag)
              {
                case ';':
                  match(';');
                  stat();
                  statlistp();
                break;
              }
          } else
          error("syntax error(statlistp)");
      }
    
    private void stat ()
      {
        if (look.tag == Tag.ID || look.tag == Tag.PRINT || look.tag == Tag.READ || look.tag == Tag.IF
                || look.tag == Tag.FOR || look.tag == Tag.BEGIN || look.tag == Tag.END || look.tag == Tag.ELSE)
          {
            switch (look.tag)
              {
                case Tag.ID:
                  match(Tag.ID);
                  match('=');
                  expr();
                  break;
                
                case Tag.PRINT:
                  match(Tag.PRINT);
                  match('(');
                  expr();
                  match(')');
                  break;
                
                case Tag.READ:
                  match(Tag.READ);
                  match('(');
                  match(Tag.ID);
                  match(')');
                  break;
                
                case Tag.IF:
                  match(Tag.IF);
                  bexpr();
                  match(Tag.THEN);
                  stat();
                  break;
                
                case Tag.ELSE:
                  match(Tag.ELSE);
                  stat();
                  break;
                
                case Tag.FOR:
                  match(Tag.FOR);
                  match('(');
                  match(Tag.ID);
                  match('=');
                  expr();
                  match(';');
                  bexpr();
                  match(')');
                  match(Tag.DO);
                  stat();
                  break;
                
                case Tag.BEGIN:
                  match(Tag.BEGIN);
                  statlist();
                  match(Tag.END);
                  break;
              }
          } else
          error("syntax error(stat)");
      }
    
    private void bexpr ()
      {
        if (look.tag == '(' || look.tag == Tag.NUM || look.tag == Tag.ID)
          {
            expr();
            match(Tag.RELOP);
            expr();
          } else
          error("syntax error(bexpr)");
      }
    
    private void expr ()
      {
        if (look.tag == '(' || look.tag == Tag.NUM || look.tag == Tag.ID)
          {
            term();
            exprp();
          } else
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
                  match(look.tag);
                  term();
                  exprp();
                  break;
                
                case '-':
                  match(look.tag);
                  term();
                  exprp();
                  break;
              }
          } else
          error("syntax error(exprp)");
      }
    
    private void term ()
      {
        if (look.tag == '(' || look.tag == Tag.NUM || look.tag == Tag.ID)
          {
            fact();
            termp();
          } else
          error("syntax error(term)");
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
                  termp();
                  break;
                
                case '/':
                  match(look.tag);
                  fact();
                  termp();
                  break;
              }
          } else
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
                  match(')');
                  break;
                
                case Tag.ID:
                  match(look.tag);
                  break;
                
                case Tag.NUM:
                  match(look.tag);
                  break;
              }
          } else
          error("syntax error(fact)");
      }
    
    public static void main (String[] args)
      {
        Lexer lex = new Lexer();
        String path = "samples/source.parser"; // il percorso del file da leggere
        
        try
          {
            BufferedReader br = Utils.removeComments(new BufferedReader(new FileReader(path)));
            BufferedReader tr = br;
            Parser parser = new Parser(lex, br);
            parser.prog();
            System.out.println("Code Accepted!");
            br.close();
          } catch (IOException e) {e.printStackTrace();}
      }
  }
