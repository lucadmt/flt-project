package st169656.analyzers;

import java.io.*;
import st169656.clockwork.Token;
import st169656.clockwork.Tag;
import st169656.clockwork.Utils;

public class ParserO
  {
    private Lexer lex;
    private BufferedReader pbr;
    private Token look;
    
    public ParserO (Lexer l, BufferedReader br)
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
    
    public void start ()
      {
        if (look.tag == '(' || look.tag == Tag.NUM)
          {
            expr();
            match(Tag.EOF);
          } else
          {
            error("Syntax Error (start)");
          }
      }
    
    private void expr ()
      {
        if (look.tag == '(' || look.tag == Tag.NUM || look.tag == '+' || look.tag == '-')
          {
            term();
            exprp();
          } else
          error("Syntax error (expr)");
      }
    
    private void exprp ()
      {
        if (look.tag == '+' || look.tag == '-' || look.tag == Tag.EOF || look.tag == ')')
          {
            switch (look.tag)
              {
                case '+':
                  match('+');
                  term();
                  exprp();
                  break;
                
                case '-':
                  match('-');
                  term();
                  exprp();
                  break;
              }
          } else
          error("Syntax error (exprp)");
      }
    
    private void term ()
      {
        if (look.tag == '(' || look.tag == Tag.NUM)
          {
            fact();
            termp();
          } else
          error("Syntax error (term)");
      }
    
    private void termp ()
      {
        if (look.tag == '*' || look.tag == '/' || look.tag == '+' || look.tag == '-' || look.tag == Tag.EOF || look.tag == ')')
          {
            switch (look.tag)
              {
                case '*':
                  match('*');
                  fact();
                  termp();
                  break;
                
                case '/':
                  match('/');
                  fact();
                  termp();
                  break;
              }
          } else
          error("Syntax error (termp)");
      }
    
    private void fact ()
      {
        if (look.tag == '(' || look.tag == Tag.NUM)
          {
            switch (look.tag)
              {
                case '(':
                  match('(');
                  expr();
                  match(')');
                  break;
                
                case Tag.NUM:
                  match(look.tag);
                  break;
              }
          } else
          error("Syntax error (fact)");
      }
    
    public static void main (String[] args)
      {
        Lexer lex = new Lexer();
        String path = "samples/source.lexer"; // il percorso del file da leggere
        
        try
          {
            BufferedReader br = new BufferedReader(new FileReader(path));
            BufferedReader tr = new BufferedReader(new FileReader(path));
            ParserO parser = new ParserO(lex, br);
            parser.start();
            System.out.println("Accepted: " + tr.readLine());
            br.close();
          } catch (IOException e) {e.printStackTrace();}
      }
  }
