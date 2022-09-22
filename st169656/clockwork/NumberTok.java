package st169656.clockwork;

public class NumberTok extends Token
  {
    private int number = 0;
    
    public NumberTok (int tag, int number)
      {
        super(tag);
        this.number = number;
      }
  
    public int getNumber ()
      {
        return number;
      }
  
    public String toString ()
      {
        return "<" + tag + ", " + number + ">";
      }
  }
