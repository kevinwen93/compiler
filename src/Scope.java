import org.antlr.v4.runtime.*;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Stack;

public class Scope {
    String type;
    String name;
    Scope parent;
    HashMap<String, Symbol> table = new HashMap<String, Symbol>();
    public Scope(String _type)
    {
        type = _type;
    }
    public Scope(String _type, String _name)
    {
        name = _name;
        type = _type;
    }

}
