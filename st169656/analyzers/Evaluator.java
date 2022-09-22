package st169656.analyzers;

import java.io.*;
import st169656.clockwork.Token;
import st169656.clockwork.Tag;
import st169656.clockwork.Utils;

public class Evaluator
  {
    private Lexer lex;
    private BufferedReader pbr;
    private Token look;
    
    public Evaluator (Lexer l, BufferedReader br)
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
          error("syntax error (match), expected: " + t);
      }
    
    public void start ()
      {
        int expr_val;
        expr_val = expr();
        match(Tag.EOF);
        
        System.out.println("Risultato: "+expr_val);
      }
    
    private int expr ()
      {
        int term_val, exprp_val = 0;
        term_val = term();
        exprp_val = exprp(term_val);
        return exprp_val;
      }
    
    private int exprp (int exprp_i)
      {
        int term_val, exprp_val = 0;
        switch (look.tag)
          {
            case '+':
              match('+');
              term_val = term();
              exprp_val = exprp(exprp_i + term_val);
              break;
            
            case '-':
              match('-');
              term_val = term();
              exprp_val = exprp(exprp_i - term_val);
              break;
            
            default:
              exprp_val = exprp_i;
              break;
          }
        return exprp_val;
      }
    
    private int term ()
      {
        int fact_val, termp_val = 0;
        fact_val = fact();
        termp_val = termp(fact_val);
        return termp_val;
      }
    
    private int termp (int termp_i)
      {
        int fact_val, termp_val = 0;
        switch (look.tag)
          {
            case '*':
              match('*');
              fact_val = fact();
              termp_val = termp(fact_val * termp_i);
              break;
            
            case '/':
              match('/');
              fact_val = fact();
              termp_val = termp(fact_val / termp_i);
              break;
            
            default:
              termp_val = termp_i;
              break;
          }
        return termp_val;
      }
    
    private int fact ()
      {
        int fact_val = 0;
        switch (look.tag)
          {
            case '(':
              match('(');
              fact_val = expr();
              match(')');
              break;
            
            case Tag.NUM:
              match(look.tag);
              fact_val = lex.value;
              break;
          }
        return fact_val;
      }
    
    public static void main (String[] args)
      {
        Lexer lex = new Lexer();
        String path = "samples/source.evaluator"; // il percorso del file da leggere
        try
          {
            BufferedReader br = new BufferedReader(new FileReader(path));
            Evaluator evaluator = new Evaluator(lex, br);
            evaluator.start();
            br.close();
          } catch (IOException e)
          {
            e.printStackTrace();
          }
      }
  }
