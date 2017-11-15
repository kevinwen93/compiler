import org.antlr.v4.runtime.*;
import java.util.*;

public class Ircode {
	public String opcode;
	public Irnode oprand1;
	public Irnode oprand2;
	public Irnode result;
	public String code;
	public boolean endofF = false;
	public boolean endofB = false;
	public int debug = 0;
	public ArrayList<Ircode> successors = new ArrayList<Ircode>();
	public ArrayList<Ircode> predecessors = new ArrayList<Ircode>();
	public LinkedHashSet<String> in = new LinkedHashSet<String>();
	public LinkedHashSet<String> out = new LinkedHashSet<String>();
	public LinkedHashSet<String> kill = new LinkedHashSet<String>();
	public LinkedHashSet<String> gen = new LinkedHashSet<String>();

	public Ircode(String _opcode){
		opcode = _opcode;
		code = ";" + opcode;
	}
	public Ircode(String _opcode, Irnode _result){
		opcode = _opcode;
		result = _result;
		code = ";" + opcode + " " + result.value;
	}
	public Ircode(String _opcode, Irnode _oprand1, Irnode _result){
		opcode = _opcode;
		oprand1 = _oprand1;
		result = _result;
		code = ";" + opcode + " " + oprand1.value + " " + result.value;
	}
	public Ircode(String _opcode, Irnode _oprand1, Irnode _oprand2, Irnode _result){
		opcode = _opcode;
		oprand1 = _oprand1;
		oprand2 = _oprand2;
		result = _result;
		code = ";" + opcode + " " + oprand1.value + " " + oprand2.value + " " + result.value;
	}

	public void printCode(){
		System.out.println(code);
		if(endofF == true){
			System.out.println();
		}
	}	
}