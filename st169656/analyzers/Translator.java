package st169656.analyzers;

import java.io.*;
import st169656.clockwork.*;

public class Translator {
  private Lexer lex;
  private BufferedReader pbr;
  private Token look;

  SymbolTable st = new SymbolTable();
  CodeGenerator code = new CodeGenerator();
  int count = 0;

  public Translator(Lexer l, BufferedReader br) {
    lex = l;
    pbr = br;
    move();
  }

  void move() {
    look = lex.lexical_scan(pbr);
    System.out.println("token = " + look);
  }

  void error(String s) {
    throw new Error("\n\nnear line " + lex.line + ": " + s);
  }

  void match(int t) {
    if (look.tag == t) {
      if (look.tag != Tag.EOF)
        move();
    } else
      error("syntax error (match), expected: " + t);
  }

  private void prog() {
    if (look.tag == Tag.ID || look.tag == Tag.PRINT || look.tag == Tag.READ || look.tag == Tag.IF || look.tag == Tag.FOR
        || look.tag == Tag.BEGIN) {
      int lnext_prog = code.newLabel();
      statlist(lnext_prog);
      code.emitLabel(lnext_prog);
      match(Tag.EOF);
      try {
        code.toJasmin();
      } catch (java.io.IOException e) {
        System.out.println("IO error\n");
      }
    } else
      error("syntax error (prog)");
  }

  private void statlist(int lnext_prog) {
    if (look.tag == Tag.ID || look.tag == Tag.PRINT || look.tag == Tag.READ || look.tag == Tag.IF || look.tag == Tag.FOR
        || look.tag == Tag.BEGIN) {
      stat(lnext_prog);
      int slp_next = code.newLabel();
      statlistp(slp_next);
    } else
      error("syntax error(statlist)");
  }

  private void statlistp(int lnext_slp) {
    if (look.tag == ';' || look.tag == Tag.END || look.tag == Tag.EOF || look.tag == Tag.IF) {
      switch (look.tag) {
      case ';':
        match(';');
        stat(lnext_slp);
        int slp_next = code.newLabel();
        statlistp(slp_next);
        break;
      }
    } else
      error("syntax error(statlistp)");
  }

  private void stat(int s_next) {
    if (look.tag == Tag.ID || look.tag == Tag.PRINT || look.tag == Tag.READ || look.tag == Tag.IF || look.tag == Tag.FOR
        || look.tag == Tag.BEGIN || look.tag == Tag.END || look.tag == Tag.ELSE) {
      switch (look.tag) {
      case Tag.ID:
        int newvar_addr = st.lookupAddress(((Word) look).lexeme);
        if (newvar_addr == -1) {
          newvar_addr = count;
          st.insert(((Word) look).lexeme, count++);
        }
        match(Tag.ID);
        match('=');
        expr();
        code.emit(OpCode.istore, newvar_addr);
        break;

      case Tag.PRINT:
        match(Tag.PRINT);
        match('(');
        expr();
        code.emit(OpCode.invokestatic, 1);
        match(')');
        break;

      case Tag.READ:
        match(Tag.READ);
        match('(');
        if (look.tag == Tag.ID) {
          int read_id_addr = st.lookupAddress(((Word) look).lexeme);
          if (read_id_addr == -1) {
            read_id_addr = count;
            st.insert(((Word) look).lexeme, count++);
          }
          match(Tag.ID);
          match(')');
          code.emit(OpCode.invokestatic, 0);
          code.emit(OpCode.istore, read_id_addr);
        } else {
          error("[STAT]: error after read: " + look);
        }
        break;

      case Tag.IF:
        match(Tag.IF);
        int b_true = code.newLabel();
        int b_false = code.newLabel();
        int next = code.newLabel();
        bexpr(b_true, b_false);
        code.emitLabel(b_true);
        match(Tag.THEN);
        stat(next);
        code.emit(OpCode.GOto, ++next);
        code.emitLabel(b_false);
        break;

      case Tag.ELSE:
        match(Tag.ELSE);
        stat(s_next);
        code.emitLabel(--s_next);
        break;

      case Tag.FOR:
        match(Tag.FOR);
        match('(');
        int id_var = st.lookupAddress(((Word) look).lexeme);
        if (id_var == -1) {
          id_var = count;
          st.insert(((Word) look).lexeme, count++);
        }
        int for_check_label = code.newLabel();
        code.emitLabel(for_check_label);
        match(Tag.ID);
        match('=');
        expr();
        code.emit(OpCode.istore, id_var);
        match(';');
        int for_body_label = code.newLabel();
        bexpr(for_body_label, s_next);
        code.emitLabel(for_body_label);
        match(')');
        match(Tag.DO);
        stat(for_check_label);
        code.emit(OpCode.GOto, for_check_label);
        code.emitLabel(s_next);
        break;

      case Tag.BEGIN:
        match(Tag.BEGIN);
        statlist(s_next);
        match(Tag.END);
        break;
      }
    } else
      error("syntax error(stat)");
  }

  private void bexpr(int ltrue, int lfalse) {
    Token tmp;
    if (look.tag == '(' || look.tag == Tag.NUM || look.tag == Tag.ID) {
      expr();
      tmp = look;
      match(Tag.RELOP);
      expr();
      switch (tmp.tag) {
      case Tag.OR:
        code.emit(OpCode.ior, ltrue);
        break;

      case Tag.AND:
        code.emit(OpCode.iand, ltrue);
        break;

      default:
        if ((Word) tmp == Word.ge)
          code.emit(OpCode.if_icmpge, ltrue);

        if ((Word) tmp == Word.le)
          code.emit(OpCode.if_icmple, ltrue);

        if ((Word) tmp == Word.gt)
          code.emit(OpCode.if_icmpgt, ltrue);

        if ((Word) tmp == Word.lt)
          code.emit(OpCode.if_icmplt, ltrue);

        if ((Word) tmp == Word.eq)
          code.emit(OpCode.if_icmpeq, ltrue);

        if ((Word) tmp == Word.ne)
          code.emit(OpCode.if_icmpne, ltrue);
        break;
      }

      code.emit(OpCode.GOto, lfalse);

    } else
      error("syntax error(bexpr)");
  }

  private void expr() {
    if (look.tag == '(' || look.tag == Tag.NUM || look.tag == Tag.ID) {
      term();
      exprp();
    } else
      error("syntax error(expr)");
  }

  private void exprp() {
    if (look.tag == '+' || look.tag == '-' || look.tag == Tag.RELOP || look.tag == ';' || look.tag == ')'
        || look.tag == Tag.THEN || look.tag == Tag.ELSE || look.tag == Tag.END) {
      switch (look.tag) {
      case '+':
        match(look.tag);
        term();
        exprp();
        code.emit(OpCode.iadd);
        break;

      case '-':
        match(look.tag);
        term();
        exprp();
        code.emit(OpCode.isub);
        break;
      }
    } else
      error("syntax error(exprp)");
  }

  private void term() {
    if (look.tag == '(' || look.tag == Tag.NUM || look.tag == Tag.ID) {
      fact();
      termp();
    } else
      error("syntax error(term)");
  }

  private void termp() {
    if (look.tag == '*' || look.tag == '-' || look.tag == '/' || look.tag == '+' || look.tag == ';'
        || look.tag == Tag.RELOP || look.tag == ')' || look.tag == Tag.THEN || look.tag == Tag.END) {
      switch (look.tag) {
      case '*':
        match(look.tag);
        fact();
        termp();
        code.emit(OpCode.imul);
        break;

      case '/':
        match(look.tag);
        fact();
        termp();
        code.emit(OpCode.idiv);
        break;
      }
    } else
      error("syntax error(termp)");
  }

  private void fact() {
    if (look.tag == '(' || look.tag == Tag.NUM || look.tag == Tag.ID) {
      switch (look.tag) {
      case '(':
        match('(');
        expr();
        match(')');
        break;

      case Tag.ID:
        code.emit(OpCode.iload, st.lookupAddress(((Word) look).lexeme));
        match(Tag.ID);
        break;

      case Tag.NUM:
        code.emit(OpCode.ldc, ((NumberTok) look).getNumber());
        match(look.tag);
        break;
      }
    } else
      error("syntax error(fact)");
  }

  public static void main(String[] args) {
    Lexer lex = new Lexer();
    String path = "samples/source.translator"; // il percorso del file da leggere

    try {
      BufferedReader br = Utils.removeComments(new BufferedReader(new FileReader(path)));
      // BufferedReader br = new BufferedReader(new FileReader(path));
      Translator t = new Translator(lex, br);
      t.prog();
      System.out.println("Code Accepted!");
      br.close();
    } catch (IOException e) {
      e.printStackTrace();
    }

  }
}
