import org.antlr.v4.runtime.*;
public class Acode {
	public String opcode = null;
	public String oprand1 = null;
	public String oprand2 = null;
	public String oprand3 = null;
	public Acode(String _opcode){
		opcode = _opcode;
	}
	public Acode(String _opcode, String _oprand1){
		opcode = _opcode;
		oprand1 = _oprand1;
	}
	public Acode(String _opcode, String _oprand1, String _oprand2){
		opcode = _opcode;
		oprand1 = _oprand1;
		oprand2 = _oprand2;
	}
	public Acode(String _opcode, String _oprand1, String _oprand2, String _oprand3){
		opcode = _opcode;
		oprand1 = _oprand1;
		oprand2 = _oprand2;
		oprand3 = _oprand3;
	}
	public void printCode(){
		if(oprand1 == null && oprand2 == null && oprand3 ==  null){
			System.out.printf("%s \n", opcode);
		}
		else if(oprand2 == null && oprand3 == null){
			System.out.printf("%s %s \n",opcode, oprand1);
		}
		else if(oprand3 == null){
			System.out.printf("%s %s %s \n",opcode, oprand1, oprand2);
		}
		else{
			System.out.printf("%s    %s    %s    %s\n",opcode, oprand1, oprand2, oprand3);
		}
	}
}