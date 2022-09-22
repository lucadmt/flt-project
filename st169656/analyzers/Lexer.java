package st169656.analyzers;

import java.io.*;
import java.util.*;
import st169656.clockwork.Word;
import st169656.clockwork.Token;
import st169656.clockwork.NumberTok;
import st169656.clockwork.Tag;
import st169656.clockwork.Utils;

public class Lexer {

  public static int line = 1;
  private char peek = ' ';
  private char prev_peek = ' ';
  public int value = 0;

  private void readch(BufferedReader br) {
    try {
      prev_peek = peek;
      peek = (char) br.read();
    } catch (IOException exc) {
      peek = (char) -1; // ERROR
    }
    // System.out.println("Peek: " + peek);
  }

  public Token lexical_scan(BufferedReader br) {
    while (peek == ' ' || peek == '\t' || peek == '\n' || peek == '\r' || peek == '\b') {
      if (peek == '\n')
        line++;
      readch(br);
    }

    switch (peek) {
      case '!':
        peek = ' ';
        return Token.not;

      case '(':
        peek = ' ';
        return Token.lpt;

      case ')':
        peek = ' ';
        return Token.rpt;

      case '+':
        peek = ' ';
        return Token.plus;

      case '-':
        peek = ' ';
        return Token.minus;

      case '*':
        peek = ' ';
        return Token.mult;

      case ';':
        peek = ' ';
        return Token.semicolon;

      // Paired Characters

      case '&':
        readch(br);
        if (peek == '&') {
          peek = ' ';
          return Word.and;
        } else {
          System.err.println("Erroneous character" + " after & : " + peek);
          return null;
        }

      case '|':
        readch(br);
        if (peek == '|') {
          peek = ' ';
          return Word.or;
        } else {
          System.err.println("Erroneous character" + " after | : " + peek);
          return null;
        }

      case '<':
        readch(br);
        if (peek == '=') {
          peek = ' ';
          return Word.le;
        } else if (Character.isDigit(peek) || Character.isLetter(peek) || peek == ' ') {
          peek = ' ';
          return Word.lt;
        } else if (peek == '>') {
          peek = ' ';
          return Word.ne;
        }

      case '>':
        readch(br);
        if (peek == '=') {
          peek = ' ';
          return Word.ge;
        } else if (Character.isDigit(peek) || Character.isLetter(peek) || peek == ' ') {
          peek = ' ';
          return Word.gt;
        } else {
          System.err.println("Erroneous character" + " after > : " + peek);
          return null;
        }

      case '=':
        readch(br);
        if (peek == '=') {
          peek = ' ';
          return Word.eq;
        } else if (Character.isDigit(peek) || Character.isLetter(peek) || peek == ' ') {
          peek = ' ';
          return Token.assign;
        } else {
          System.err.println("Erroneous character" + " after = : " + prev_peek + " " + peek);
          return null;
        }

      case (char) -1:
        return new Token(Tag.EOF);

      case '/':
        peek = ' ';
        return Token.div;

      default:
        String temp = "";
        if (Character.isLetter(peek)) {
          while (peek != '=' && Utils.shouldAccept(peek)) {
            temp += peek;
            readch(br);
          }

          switch (temp) {
          case "if":
            return Word.iftok;
          case "then":
            return Word.then;
          case "else":
            return Word.elsetok;
          case "for":
            return Word.fortok;
          case "do":
            return Word.dotok;
          case "print":
            return Word.print;
          case "read":
            return Word.read;
          case "begin":
            return Word.begin;
          case "end":
            return Word.end;
          default:
            return new Word(Tag.ID, temp);
          }

        } else if (Character.isDigit(peek)) {
          while (peek != ';' && Character.isDigit(peek)) {
            temp += peek;
            readch(br);
          }
          value = Integer.parseInt(temp);
          return new NumberTok(Tag.NUM, Integer.parseInt(temp));

        } else {
          System.err.println("Erroneous character: " + peek);
          return null;
        }
      }
  }

  public static void main(String[] args) {

    Lexer lex = new Lexer();
    String path = "samples/source.lexer"; // il percorso del file da leggere
    try {
      BufferedReader br = Utils.removeComments(new BufferedReader(new FileReader(path)));
      Token tok;
      do {
        tok = lex.lexical_scan(br);
        System.out.println("Scan: " + tok);
      } while (tok.tag != Tag.EOF);

      br.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
