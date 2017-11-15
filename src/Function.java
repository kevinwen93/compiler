import org.antlr.v4.runtime.*;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Stack;

public class Function {
    String name;
    String retType;
    int tn = 0;
    int pn = 0;
    int ln = 0;
    HashMap<String, Symbol> paramTable = new HashMap<String, Symbol>();
    HashMap<String, Symbol> localTable = new HashMap<String, Symbol>();
    public Function(String _name ,String _retType)
    {
        name = _name;
        retType = _retType;
    }
}
