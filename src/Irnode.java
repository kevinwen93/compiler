import org.antlr.v4.runtime.*;
public class Irnode {
        String value;
        String dtype;
        String gtype;
        public Irnode(String _gtype, String _dtype, String _value){
                gtype = _gtype;
                dtype = _dtype;
                value = _value;
        }
        public boolean equal(Irnode n){
        	boolean equal;
	        if(!value.equals(n.value)){
	            equal = false;
	        }
	        else{
	        	equal = true;
	        }
	        return equal;
         
    }
}
