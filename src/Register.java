import org.antlr.v4.runtime.*;

public class Register {
    public String name;
    public boolean isDirty;
   	public boolean isFree;
    public Irnode var;
    public Register(String _num){
    	isDirty = false; 
    	isFree = true;
    	name = "r" + _num;
    }
}
