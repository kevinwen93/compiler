    // Generated from Micro.g4 by ANTLR 4.5.1

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.TerminalNode;
import java.lang.Integer;
import java.util.*;
/**
 * This class provides an empty implementation of {@link MicroListener},
 * which can be extended to create a listener which only needs to handle a subset
 * of the available methods.
 */
public class CustomListener extends MicroBaseListener {
    private int printIr = 0;
    private int tempNo;
    private int rn = 0;
    private Stack<Scope> scopes;
    private Stack<Scope> p_scopes;
    private Stack<Irnode> irnode;
    private ArrayList<Ircode> ircode;
    private Stack<Irnode> incr_irnode;
    private Stack<Irnode> tnode;
    private ArrayList<Acode> acode; 
    private HashMap<String, String> rtable;
    private HashMap<String, Symbol> globalTable;
    private HashMap<String, Function> funcTable;
    private ArrayList<Register> rgstrs;
    private Stack<String> labels;
    private Irnode none = new Irnode("none", "none", "none");
    private Irnode done = new Irnode("done", "done", "done");
    private int labelnum = 1;
    public boolean in_incr = false;
    public boolean enterfor = false;
    public boolean enterif = false;
    public Function curFun;
    public int call_expr = 0;
    public String callname;
    public String rettype;
    public Ircode currentIrcode;
    //Leaders of basic blocks
    public ArrayList<Ircode> leaders;
    
    public CustomListener() {
        scopes = new Stack<Scope>();
        p_scopes = new Stack<Scope>();
        ircode = new ArrayList<Ircode>();
        irnode = new Stack<Irnode>();
        tnode = new Stack<Irnode>();
        acode = new ArrayList<Acode>();
        rtable = new HashMap<String, String>();
        labels = new Stack<String>();
        incr_irnode = new Stack<Irnode>();
        globalTable = new HashMap<String, Symbol>();
        funcTable = new HashMap<String, Function>();
        leaders = new ArrayList<Ircode>();
        rgstrs = new ArrayList<Register>();
        for(int i = 3; i >= 0; i--){
            Register r = new Register(Integer.toString(i));
            rgstrs.add(r);
        }
    }
    @Override public void enterProgram(MicroParser.ProgramContext ctx){
        Scope scope = new Scope("GLOBAL");
        scopes.push(scope);
        p_scopes.push(scope);
    }

    @Override public void exitProgram(MicroParser.ProgramContext ctx) {
        basicBlocks();
        ctrFlowGraph();
        livenessCheck();
        genTiny();
        //scopes.pop();
        /*int num = 1;
        for (Scope scope : p_scopes)
        {
            if(scope.name.equals("BLOCK"))
            {
               System.out.println("Symbol table "+ scope.name+" "+num);
               num++;

            }
            else
        {
               System.out.println("Symbol table "+ scope.name);

            }

                for (String key : scope.keys) {
                        Symbol var = scope.table.get(key);
                    if (var.type.equals("STRING")) {
                        System.out.println("name " + key + " type " + var.type + " value " + var.value);
                    }
                    else{

                        System.out.println("name "+ key + " type " + var.type);
                        }
                }
                System.out.println("");

        }*/
        /*
        for (Scope scope : p_scopes)
        {
            if(scope.type.equals("FUNCTION")){
                System.out.println(scope.type + " " + scope.name);
            }
            else{
                System.out.println(scope.type);
            }
            for(String key :scope.table.keySet()){
                System.out.println(key + ": " + scope.table.get(key).value);
            }

        }*/
        if(printIr == 1){
            for(Irnode ir : irnode){
                System.out.println(ir.gtype + " " + ir.dtype +" "+ ir.value);
            }
        }
        
        if(printIr == 0){
            /*
            ircode.get(3).printCode();
            for(String liveOut: ircode.get(3).out){
                System.out.printf(liveOut + " ");
            }
            if(ircode.get(3).out.contains("$T1")){
                System.out.println("true");
            }
            else{
                System.out.println("false");
            }
            */
            //System.out.println(";IR code");
            for(Ircode c: ircode){
                
                c.printCode();    
                
                /*System.out.println(";liveOut:");
                System.out.printf(";");
                for(String liveOut: c.out){
                    System.out.printf(liveOut + " ");
                }
                System.out.println();*/
                
            }
            
            System.out.println(";tiny code");
            for(Acode ac: acode){
                ac.printCode();
            }
            System.out.println("end");
        }
    }
    @Override public void enterString_decl(MicroParser.String_declContext ctx) {
        String lv;
        String varType = "STRING";
        String varName =ctx.id().getText();
        String varValue = ctx.str().getText();
        Symbol var = new Symbol(varType, varValue);
        Scope top = scopes.peek();
        checkDeclError(top.table, varName);
        if(curFun != null){
            checkShadow(curFun.localTable, varName);    
        }
        
        //top.table.put(varName, var);
        //top.keys.add(varName);
        
        if(top.type.equals("GLOBAL")){
            Acode c = new Acode("str", varName, varValue);
            acode.add(c);
            globalTable.put(varName,var);
        }
        else{
            curFun.ln++;  
            lv = String.format("$L%d", curFun.ln);
            var = new Symbol(varType, lv);
            curFun.localTable.put(varName, var);
              
        }
    }
    @Override public void exitString_decl(MicroParser.String_declContext ctx) {

    }

    @Override public void enterVar_decl(MicroParser.Var_declContext ctx) {

        String varNames= ctx.id_list().getText();
        String varType = ctx.var_type().getText();
        String varValue = "";
        String[] Names = varNames.split(",");
        Scope top = scopes.peek();
        int i;

        for (i=0;i<Names.length;i++){
            String name = Names[i];
            String lv;
            Symbol var = new Symbol(varType, varValue);
            checkDeclError(top.table, name);
            if(curFun != null){
                checkShadow(curFun.localTable, name);    
            }
            top.table.put(name, var);
            
            if(top.type.equals("GLOBAL")){
                //System.out.println(top.type);
                Acode c = new Acode("var", name);
                acode.add(c);
                globalTable.put(name,var);
            }
            else{
                curFun.ln++;
                lv = String.format("$L%d", curFun.ln);
                var = new Symbol(varType, lv);
                curFun.localTable.put(name, var);
            }
        }
    }
    @Override public void enterFunc_decl(MicroParser.Func_declContext ctx){
        String varName = ctx.id().getText();
        //funcTable.put(varName, ctx.any_type().getText());
        Function f = new Function(varName, ctx.any_type().getText());
        funcTable.put(varName,f);
        Scope scope = new Scope("FUNCTION", varName);
        if(printIr == 0){
            tempNo = 1;
            Irnode ir = new Irnode("FUNCTION","none",varName);
            Ircode fnode = new Ircode("LABEL", ir);
            ircode.add(fnode);
            fnode = new Ircode("LINK");
            ircode.add(fnode);
        }
        else{
            Irnode temp = new Irnode("FUNCTION","none",varName);
            irnode.push(temp);
        }
        scopes.push(scope);
        p_scopes.push(scope);
        curFun = f;
    }

    @Override public void exitFunc_decl(MicroParser.Func_declContext ctx){
        Ircode c;
        if(printIr == 0)
        {
            curFun.tn = tempNo;
            /*Irnode left = irnode.pop();
            Irnode result = new Irnode("return", left.dtype, "$R");
            if(left.dtype.equals("INT")){
                c = new Ircode("STOREI", left, none, result);
                ircode.add(c);
            }
            else{
                c = new Ircode("STOREF", left, none, result);   
                ircode.add(c);
            }*/
            if(!ircode.get(ircode.size() - 1).opcode.equals("RET")){
                c =  new Ircode("RET");
                ircode.add(c);
            }
            ircode.get(ircode.size() - 1).endofF = true;

        }
        else{
            Irnode temp = new Irnode("exitFunc", "none", ctx.id().getText());
            irnode.push(temp);
        }
        
    }

    @Override public void enterParam_decl(MicroParser.Param_declContext ctx){
        String varType = ctx.var_type().getText();
        String varName = ctx.id().getText();
        String varValue = "";
        String param;
        Symbol var = new Symbol(varType, varValue);
        Scope scope = scopes.peek();
        //System.out.println(scope.type +"  " + varName);
        checkDeclError(scope.table, varName);
        scope.table.put(varName, var);
        curFun.pn++;
        param = String.format("$P%d", curFun.pn);
        //System.out.println(param);
        var = new Symbol(varType, param);
        curFun.paramTable.put(varName, var);

        //System.out.println(scope.type);
        //scope.keys.add(varName);
        //Acode c = new Acode("var", varName);
        //acode.add(c);
        

    }
    @Override public void exitParam_decl(MicroParser.Param_declContext ctx){

    }


    @Override public void enterIf_stmt(MicroParser.If_stmtContext ctx) {
        enterif = true;
        enterfor = false;

        Scope scope = new Scope("BLOCK");
        scopes.push(scope);
        p_scopes.push(scope);
        String label = "label" + labelnum;
        labels.push(label);
        labelnum++;
        
        label = "label" + labelnum;
        labels.push(label);
        labelnum++;
     }

    @Override public void exitIf_stmt(MicroParser.If_stmtContext ctx) {
        String label = labels.pop();
        Irnode in = new Irnode("none","none",label);
        Ircode c = new Ircode("LABEL", in);
        ircode.add(c);
    }
    @Override public void enterElse_part(MicroParser.Else_partContext ctx) {
        
        Scope scope = new Scope("BLOCK");
        //Irnode in = new Irnode("ELSE", "none", "ELSE");
        //irnode.push(in);
        scopes.push(scope);
        p_scopes.push(scope);
        String label1 = labels.pop();
        Irnode in1 = new Irnode("none","none",label1);
        Ircode c1 = new Ircode("JUMP", in1);
        ircode.add(c1);

        String label2 = labels.pop();
        Irnode in2 = new Irnode("none","none",label2);
        Ircode c2 = new Ircode("LABEL", in2);
        ircode.add(c2);
        labels.push(label1);
        
    }

    @Override public void exitElse_part(MicroParser.Else_partContext ctx) {
    }

    @Override public void enterCond(MicroParser.CondContext ctx){
        Irnode in = new Irnode("comp", "none", ctx.compop().getText());
        irnode.push(in);
    }
    @Override public void exitCond(MicroParser.CondContext ctx) { 
        if(printIr == 0){
            while(!irnode.empty()){
                Irnode temp = irnode.pop();          
                if(temp.gtype.equals("op")){
                    Irnode left = tnode.pop();
                    Irnode right = tnode.pop();
                    Generate3AC(temp, left, right);
                }
                else if(temp.gtype.equals("comp")){
                    Irnode left = tnode.pop();
                    //System.out.print(left.value);
                    Irnode right = tnode.pop();
                    genCond3ac(temp, left, right);
                }
                else{
                    tnode.push(temp);
                    //System.out.println("push");
                }
            }
        }
    }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterExpr(MicroParser.ExprContext ctx) { 
        //System.out.printf("(%s) ", ctx.getText());

    }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitExpr(MicroParser.ExprContext ctx) { }

    @Override public void enterCompop(MicroParser.CompopContext ctx) {
        //Irnode in = new Irnode("comp","none",ctx.getText());
        //irnode.push(in);
        //System.out.printf("(%s) ", ctx.getText());
    }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitCompop(MicroParser.CompopContext ctx) { }


    @Override public void enterFor_stmt(MicroParser.For_stmtContext ctx) {
        enterfor = true;
        enterif = false;
        Scope scope = new Scope("BLOCK");
        scopes.push(scope);
        p_scopes.push(scope);
        String label = "label" + labelnum;
        labels.push(label);
        labelnum++;
        

        label = "label" + labelnum;
        labels.push(label);
        labelnum++;
        

    }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitFor_stmt(MicroParser.For_stmtContext ctx) {
        String label1;
        String label2;
        while(!incr_irnode.empty() && incr_irnode.peek().value != "done"){
            irnode.push(incr_irnode.pop());
        }
        if(!incr_irnode.empty()){
            incr_irnode.pop();
        }
        if(printIr == 0){
            while(!irnode.empty()){
                Irnode temp = irnode.pop();
                //System.out.println(temp.value);            
                if(temp.gtype.equals("op")){
                    Irnode left = tnode.pop();
                    Irnode right = tnode.pop();
                    Generate3AC(temp, left, right);
                }
                else if(temp.gtype.equals(":=")){
                    Irnode left = tnode.pop();
                    //System.out.print(left.value);
                    Irnode right = null;
                    Generate3AC(temp, left, right);
                }
                else{
                    tnode.push(temp);
                }
            }
            label1 = labels.pop();
            label2 = labels.pop();
            Irnode in = new Irnode("none","none",label2);
            Ircode c = new Ircode("JUMP", in);
            ircode.add(c);
            in = new Irnode("none","none",label1);
            c = new Ircode("LABEL", in);
            ircode.add(c);
        }
        else{
            Irnode temp = new Irnode("exitFor", "none", "none");
            irnode.push(temp);
        }
    }
    
    @Override public void enterInit_stmt(MicroParser.Init_stmtContext ctx) {
    }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitInit_stmt(MicroParser.Init_stmtContext ctx) { 
        

        String label1;
        String label2;
        label1 = labels.pop();
        label2 = labels.pop();
        Irnode in = new Irnode("none","none",label2);
        Ircode c = new Ircode("LABEL", in);
        ircode.add(c);
        labels.push(label2);
        labels.push(label1);

    }
    @Override public void enterIncr_stmt(MicroParser.Incr_stmtContext ctx) { 
        in_incr = true;
    }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitIncr_stmt(MicroParser.Incr_stmtContext ctx) { 
        in_incr = false;
    }


    @Override public void enterPrimary(MicroParser.PrimaryContext ctx) {
        Irnode in;
        Symbol var;
        if(ctx.id() != null)
        {
            if(globalTable.get(ctx.id().getText()) != null){
                var = globalTable.get(ctx.id().getText());
                in = new Irnode("id", var.type, ctx.id().getText());
                irnode.push(in);
            }
            else if(curFun.paramTable.get(ctx.id().getText())!= null){
                var = curFun.paramTable.get(ctx.id().getText());
                in = new Irnode("param", var.type, var.value);
                irnode.push(in);
            }
            else{
                var = curFun.localTable.get(ctx.id().getText());
                in = new Irnode("local", var.type, var.value);
                irnode.push(in);
            }
        }
        if(ctx.INTLITERAL() != null)
        {
            in = new Irnode("INTLITERAL", "INT", ctx.INTLITERAL().getText());
            irnode.push(in);
        }
        if(ctx.FLOATLITERAL() != null)
        {
            in = new Irnode("FLOATLITERAL", "FLOAT", ctx.FLOATLITERAL().getText());
            irnode.push(in);
        }

    }
    @Override public void enterExpr_prefix(MicroParser.Expr_prefixContext ctx) {
        if(ctx.addop() != null){
            Irnode in = new Irnode("op", "none", ctx.addop().getText());
            irnode.push(in);
        }     
    }
    @Override public void exitExpr_prefix(MicroParser.Expr_prefixContext ctx) { 
    }
    
    @Override public void enterFactor_prefix(MicroParser.Factor_prefixContext ctx) {
        if(ctx.mulop() != null){
            Irnode in = new Irnode("op", "none", ctx.mulop().getText());
            irnode.push(in);
        } 
    }
    @Override public void exitFactor_prefix(MicroParser.Factor_prefixContext ctx) { }
    
    @Override public void enterAssign_expr(MicroParser.Assign_exprContext ctx) { 
        if(printIr == 1){
            Irnode ir = new Irnode("enterass", "none", "none");
            irnode.push(ir);
        }
        //look up type from the hashtable\
        Irnode in;
        Scope global;
        Symbol var;
        if(ctx.id() != null)
        {
            if(globalTable.get(ctx.id().getText()) != null){
                var = globalTable.get(ctx.id().getText());
                in = new Irnode("id", var.type, ctx.id().getText());
                irnode.push(in);
            }
            else if(curFun.paramTable.get(ctx.id().getText()) != null){
                var = curFun.paramTable.get(ctx.id().getText());
                in = new Irnode("param", var.type, var.value);
                irnode.push(in);
            }
            else{
                var = curFun.localTable.get(ctx.id().getText());
                in = new Irnode("local", var.type, var.value);
                irnode.push(in);
            }
        }
        //System.out.printf("id = %s\n",in.value);
        Irnode ineq = new Irnode(":=", "none", ":=");
        irnode.push(ineq);
    }
    
    @Override public void exitAssign_expr(MicroParser.Assign_exprContext ctx) {
     
        if(in_incr == true){
            incr_irnode.push(done);
            while(!irnode.empty()){
                incr_irnode.push(irnode.pop());
            }
        }
        if(printIr == 0){
            
                while(!irnode.empty()){
                    Irnode temp = irnode.pop();
                    
                    //System.out.println(temp.value);            
                    if(temp.gtype.equals("op")){
                        Irnode left = tnode.pop();
                        Irnode right = tnode.pop();
                        Generate3AC(temp, left, right);
                    }
                    else if(temp.gtype.equals(":=")){
                        Irnode left = tnode.pop();
                        //System.out.print(left.value);
                        Irnode right = null;
                        Generate3AC(temp, left, right);
                    }
                    else{
                        tnode.push(temp);
                        //System.out.println("push");
                    }
                }
        }
        else{
            Irnode temp = new Irnode("exitass", "none" ,"none");
            irnode.push(temp);
        }
        
    }
    @Override public void enterWrite_stmt(MicroParser.Write_stmtContext ctx) {
        String op;
    	String[] ids = ctx.id_list().getText().split(",");
        String name;
        String gtype;
        //System.out.println("I am writing");
        //Scope top = scopes.peek();
    	for(String id: ids){
            Symbol var = globalTable.get(id);
            if(var != null){
                name = id;
                gtype = "id";
            }
            else if(curFun.paramTable.get(id)!= null){
                var = curFun.paramTable.get(id);
                name = var.value;
                gtype = "param";
            }
            else{
                var = curFun.localTable.get(id);
                name = var.value;
                gtype = "local";
            }
            if(var.type.equals("INT")){
                op = "WRITEI";
            }
            else if(var.type.equals("STRING")){
                op = "WRITES";
            }
            else{
                op = "WRITEF";
            }
            Irnode iD = new Irnode(gtype, "none", name); 
    		Ircode n = new Ircode(op, iD);
    		ircode.add(n);
    	}

    }
	
	@Override public void exitWrite_stmt(MicroParser.Write_stmtContext ctx) { }



    @Override public void enterRead_stmt(MicroParser.Read_stmtContext ctx) {
        String op;
        String name;
        String gtype;
        String[] ids = ctx.id_list().getText().split(",");
        //System.out.println("I am writing");
        //Scope top = scopes.peek();
        for(String id: ids){
            Symbol var = globalTable.get(id);
            if(var != null){
                name = id;
                gtype = "id";
            }
            else if(curFun.paramTable.get(id)!= null){
                var = curFun.paramTable.get(id);
                name = var.value;
                gtype = "param";
            }
            else{
                var = curFun.localTable.get(id);
                name = var.value;
                gtype = "local";
            }
            if(var.type.equals("INT")){
                op = "READI";
            }
            else{
                op = "READF";
            }
            Irnode iD = new Irnode(gtype, var.type, name);
            Ircode n = new Ircode(op, iD);
            ircode.add(n);
        }
    }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitRead_stmt(MicroParser.Read_stmtContext ctx) { }



    @Override public void enterCall_expr(MicroParser.Call_exprContext ctx) { 
        
        Irnode ir = new Irnode("enterpush","none","none");
        irnode.push(ir);
    }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitCall_expr(MicroParser.Call_exprContext ctx) {
        callname = ctx.id().getText();
        rettype = ctx.id().getText();
        if(printIr == 0){
            //int numOfop = 0;
            //int numOftn = 0;
            //call_expr = 1;
            //if(call_expr == 1){
                int fpush = 0;
                String type;
                Irnode temp;
                //Stack<Irnode> ri = new Stack<Irnode>();
                while(!irnode.empty() && !irnode.peek().gtype.equals("enterpush")){
                    temp = irnode.pop();
                    //System.out.println(temp.value);            
                    if(temp.gtype.equals("op")){
                        //numOfop++;
                        Irnode left = tnode.pop();
                        Irnode right = tnode.pop();
                        Generate3AC(temp, left, right);
                    }
                    else if(temp.gtype.equals(":=")){
                        Irnode left = tnode.pop();
                        //System.out.print(left.value);
                        Irnode right = null;
                        Generate3AC(temp, left, right);
                    }
                    else{
                        tnode.push(temp);
                        //numOftn++;

                    //System.out.println("push");
                    }
                }
                Ircode c = new Ircode("PUSH");
                ircode.add(c);
                /*
                if(numOfop > 0){
                    while(numOfop > 0){
                        temp = tnode.peek();
                        fpush ++;
                        c = new Ircode("PUSH", temp);
                        ircode.add(c);
                        //System.out.println("tnode " + temp.gtype+ " " + temp.value);
                        numOfop--;
                    }
                }
                else{
                    while(numOftn > 0){
                        temp = tnode.pop();
                        fpush ++;
                        c = new Ircode("PUSH", temp);
                        ircode.add(c);
                        //System.out.println("tnode " + temp.gtype+ " " + temp.value);
                        numOftn--;
                    }
                }*/
                for(int i = 0; i < funcTable.get(callname).pn; i++ ){
                    temp = tnode.pop();
                    c = new Ircode("PUSH", temp);
                    ircode.add(c);
                }
                temp = new Irnode("FUNCTION", "none", callname);
                //System.out.println(callname);
                c = new Ircode("JSR", temp);
                ircode.add(c);
                for(int i = 0; i < funcTable.get(callname).pn; i++ ){
                    c = new Ircode("POP");
                    ircode.add(c);
                }
                /*if(fpush > 0){
                    irnode.pop();
                    while(fpush > 0){
                        fpush--;
                        c = new Ircode("POP");
                        ircode.add(c);
                    }*/
                    String tempr = String.format("$T%d", tempNo);

                    Irnode r = new Irnode("temp", funcTable.get(callname).retType, tempr);
                    irnode.pop();
                    irnode.push(r);
                    tempNo++;
                    c = new Ircode("POP", r);
                    ircode.add(c);
                //}
            //    call_expr = 0;
            //}
            /*
            int fpush = 0;
            String type;
            Irnode temp;
            Stack<Irnode> ri = new Stack<Irnode>();
            Ircode c = new Ircode("PUSH",none,none,none);
            ircode.add(c);
            while(!irnode.empty() && !irnode.peek().gtype.equals("enterpush")){
                ri.push(irnode.pop());
                //System.out.println(temp.value);
                fpush++;
            }
            while(!ri.empty()){
                temp = ri.pop();
                c = new Ircode("PUSH",none, none, temp);
                ircode.add(c);
            }
            if(fpush > 0){
                irnode.pop();
                while(fpush > 0){
                    fpush--;
                    c = new Ircode("POP",none, none, none);
                    ircode.add(c);
                }
                String tempr = String.format("$T%d", tempNo);
                Irnode r = new Irnode("temp", "INT", tempr);
                tempNo++;
                c = new Ircode("POP",none, none, r);
                ircode.add(c);
            }
            temp = new Irnode("FUNCTION", "none", ctx.id().getText());
            c = new Ircode("JSR",none, none, temp);
            ircode.add(c);*/
        }
        else{
            Irnode temp = new Irnode("exitCall","none","none");
            irnode.push(temp);
        }
    }

    @Override public void enterReturn_stmt(MicroParser.Return_stmtContext ctx) { 
        if(printIr == 1){
            Irnode ret = new Irnode("enterret","none", "none");
            irnode.push(ret);
        }
        
    }
    @Override public void exitReturn_stmt(MicroParser.Return_stmtContext ctx) { 
        if(printIr == 1){
            Irnode ret = new Irnode("exitret","none", "none");
            irnode.push(ret);
        }
        if(printIr == 0)
        {
            Ircode c;
            Irnode r;
            while(!irnode.empty()){
                Irnode temp = irnode.pop();   
                if(temp.gtype.equals("op")){
                    Irnode left = tnode.pop();
                    Irnode right = tnode.pop();
                    Generate3AC(temp, left, right);
                }
                else{
                    tnode.push(temp);
                }
            }
            //System.out.println(left.gtype + " " + left.dtype + " " + left.value);
            Irnode ret = tnode.pop();
            String dtype = ret.dtype;
            r = ret;
            if(ret.gtype.equals("INTLITERAL")){
                String tempr = String.format("$T%d", tempNo);
                r = new Irnode("temp", "INT", tempr);
                tempNo++;
                Irnode opr1 = new Irnode("INTLITERAL","INT",ret.value);
                c = new Ircode("STOREI", opr1, r);
                ircode.add(c);
            }
            else if(ret.gtype.equals("FLOATLITERAL")){
                String tempr = String.format("$T%d", tempNo);
                r = new Irnode("temp", "INT", tempr);
                tempNo++;
                Irnode opr1 = new Irnode("INTLITERAL","INT",ret.value);
                c = new Ircode("STOREI", opr1, r);
                ircode.add(c);
            }
            Irnode result = new Irnode("return", dtype, "$R");
            if(dtype.equals("INT")){
                c = new Ircode("STOREI", r, result);
                ircode.add(c);
            }
            else{
                c = new Ircode("STOREF", r, result);   
                ircode.add(c);
            }
            c = new Ircode("RET");
            ircode.add(c);
        }
    }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */

    public void checkDeclError(HashMap symtable, String varName){
        if(symtable.get(varName) != null){
            System.out.println("DECLARATION ERROR " + varName);
            System.exit(1);
        }
    }

    public void checkShadow(HashMap funTable, String varName){
         if(funTable.get(varName) != null){
            System.out.println(";SHADOW WARNING " + varName);
        }
    }    

    public void Generate3AC(Irnode opcode, Irnode left, Irnode right){
        //System.out.println("gen3ac");
        String op;
        Irnode oprand1;
        Irnode opr1;
        Irnode oprand2;
        Irnode result;
        String tempr;
        //Scope top = scopes.peek();
        if(opcode.gtype.equals(":=")){
            if(left.gtype.equals("INTLITERAL")){
                tempr = String.format("$T%d", tempNo);
                Irnode r = new Irnode("temp", "INT", tempr);
                tempNo++;
                op = "STOREI";
                opr1 = new Irnode("INTLITERAL","INT",left.value);
                Ircode c = new Ircode(op, opr1, r);
                ircode.add(c);
                //tnode.push(r);
                oprand1 = r;
            }
            else if(left.gtype.equals("FLOATLITERAL")){
                tempr = String.format("$T%d", tempNo);
                Irnode r = new Irnode("temp", "FLOAT", tempr);
                tempNo++;
                op = "STOREF";
                opr1 = new Irnode("FLOATLITERAL","FLOAT",left.value);
                Ircode c = new Ircode(op, opr1, r);
                ircode.add(c);
                //tnode.push(r);
                oprand1 = r;
            }
            else{// if(left.gtype.equals("id") || left.gtype.equals("param") || left.gtype.equals("local")){
                oprand1 = left;
                //System.out.println(left.gtype);
            }/*
            else{
                oprand1 = new Irnode("temp", left.dtype, left.value);

            }*/

            if(left.dtype.equals("INT")){
                op = "STOREI";
            }
            else{
                op = "STOREF";
            }
            result = irnode.pop();
            //System.out.printf("%s, %s, %s\n", result.value, result.gtype, result.dtype);
            Ircode c = new Ircode(op, oprand1 , result);
            ircode.add(c);
        }
        else{
            if(left.gtype.equals("INTLITERAL")){
                tempr = String.format("$T%d", tempNo);
                Irnode r = new Irnode("temp", "INT", tempr);
                tempNo++;
                op = "STOREI";
                opr1 = new Irnode("INTLITERAL", "INT", left.value);
                Ircode c = new Ircode(op, opr1, r);
                ircode.add(c);
                oprand1 = r;
            }
            else if(left.gtype.equals("FLOATLITERAL")){
                tempr = String.format("$T%d", tempNo);
                Irnode r = new Irnode("temp", "FLOAT", tempr);
                tempNo++;
                op = "STOREF";
                opr1 = new Irnode("FLOATLITERAL", "FLOAT", left.value);
                Ircode c = new Ircode(op, opr1, r);
                ircode.add(c);
                oprand1 = r; 
            }
            else{//if(left.gtype.equals("id") || left.gtype.equals("param") || left.gtype.equals("local")){
                oprand1 = left;
                
            }/*
            else{
                oprand1 = new Irnode("temp", left.dtype, left.value);
                System.out.println(left.gtype);
            }*/

            if(right.gtype.equals("INTLITERAL")){
                //System.out.println(right.value);
                tempr = String.format("$T%d", tempNo);
                Irnode r = new Irnode("temp", "INT", tempr);
                tempNo++;
                op = "STOREI";
                opr1 = new Irnode("INTLITERAL", "INT", right.value);
                Ircode c = new Ircode(op, opr1, r);
                //c.printCode();
                ircode.add(c);
                oprand2 = r;
                //System.out.println(oprand2);
            }
            else if(right.gtype.equals("FLOATLITERAL")){
                tempr = String.format("$T%d", tempNo);
                Irnode r = new Irnode("temp", "FLOAT", tempr);
                tempNo++;
                op = "STOREF";
                opr1 = new Irnode("FLOATLITERAL", "FLOAT", right.value);
                Ircode c = new Ircode(op, opr1, r);
                ircode.add(c);
                oprand2 = r;
            }
            else{// if(right.gtype.equals("id")){
                oprand2 = right;//new Irnode("id", right.dtype, right.value);
            }
            /*else{
                oprand2 = new Irnode("temp", right.dtype, right.value);
            }*/
            if(opcode.value.equals("+")){
                if(left.dtype.equals("INT")){
                    op = "ADDI";
                }
                else{
                    op = "ADDF";
                }
            }
            else if(opcode.value.equals("-")){
                if(left.dtype.equals("INT")){
                    op = "SUBI";
                }
                else{
                    op = "SUBF";
                }
            }
            else if(opcode.value.equals("*")){
                if(left.dtype.equals("INT")){
                    op = "MULTI";
                }
                else{
                    op = "MULTF";
                }
            }
            else{
                if(left.dtype.equals("INT")){
                    op = "DIVI";
                }
                else{
                    op = "DIVF";
                }
            }
            
            tempr = String.format("$T%d", tempNo);
            tempNo++;
            if(left.dtype.equals("INT")){
                Irnode r = new Irnode("temp", "INT", tempr);
                tnode.push(r);
                Ircode c = new Ircode(op, oprand1, oprand2, r);
                ircode.add(c);    
            } 
            else{
                Irnode r = new Irnode("temp", "FLOAT", tempr);
                tnode.push(r);
                Ircode c = new Ircode(op, oprand1, oprand2, r);
                ircode.add(c);
            }    
        }
        
    }

    public void genCond3ac(Irnode opcode, Irnode left, Irnode right){
        //System.out.println("here");
        String op;
        Irnode oprand1;
        Irnode opr1;
        Irnode oprand2;
        Irnode result;
        String tempr;
        //System.out.println(opcode.value);
        
        if(left.gtype.equals("INTLITERAL")){
            tempr = String.format("$T%d", tempNo);
            Irnode r = new Irnode("temp", "INT", tempr);
            tempNo++;
            op = "STOREI";
            opr1 = new Irnode("INTLITERAL", "INT", left.value);
            Ircode c = new Ircode(op, opr1, r);
            ircode.add(c);
            oprand1 = r;
        }
        else if(left.gtype.equals("FLOATLITERAL")){
            tempr = String.format("$T%d", tempNo);
            Irnode r = new Irnode("temp", "FLOAT", tempr);
            tempNo++;
            op = "STOREF";
            opr1 = new Irnode("FLOATLITERAL", "FLOAT", left.value);
            Ircode c = new Ircode(op, opr1, r);
            ircode.add(c);
            oprand1 = r; 
        }
        else {
            oprand1 = left;
        }

        if(right.gtype.equals("INTLITERAL")){
            //System.out.println(right.value);
            tempr = String.format("$T%d", tempNo);
            Irnode r = new Irnode("temp", "INT", tempr);
            tempNo++;
            op = "STOREI";
            opr1 = new Irnode("INTLITERAL", "INT", right.value);
            Ircode c = new Ircode(op, opr1, r);
            //c.printCode();
            ircode.add(c);
            oprand2 = r;
            //System.out.println(oprand2);
        }
        else if(right.gtype.equals("FLOATLITERAL")){
            tempr = String.format("$T%d", tempNo);
            Irnode r = new Irnode("temp", "FLOAT", tempr);
            tempNo++;
            op = "STOREF";
            opr1 = new Irnode("FLOATLITERAL", "FLOAT", right.value);
            Ircode c = new Ircode(op, opr1, r);
            ircode.add(c);
            oprand2 = r;
        }
        else {
            oprand2 = right;
        }
        if(opcode.value.equals("=")){
            op = "NE";
        }
        else if(opcode.value.equals("<")){
            op = "GE";
        }
        else if(opcode.value.equals(">")){
            op = "LE";
        }
        else if(opcode.value.equals(">=")){
            op = "LT";
        }
        else if(opcode.value.equals("<=")){
            op = "GT";
        }
        else{
            op = "EQ";
        }
        if(enterif == true){
            String label1 = labels.pop();
            String label2 = labels.pop();
            Irnode in = new Irnode("none","none",label2);
            Ircode c = new Ircode(op, oprand1, oprand2, in);
            labels.push(label2);
            labels.push(label1);
            ircode.add(c);
        }
        else{
            String label1 = labels.pop();
            Irnode in = new Irnode("none","none",label1);
            Ircode c = new Ircode(op, oprand1, oprand2, in);
            labels.push(label1);
            ircode.add(c);
        }
        
    }
    /******************************************************
    * Code associated with translate from 3AC to assembly
    ******************************************************/ 
    public void genTiny(){
        String op;
        String reg;
        String o1;
        String o2;
        Acode ac;
        int i;
        //call main
        ac = new Acode("push");
        acode.add(ac);
        for(i = 3; i >= 0; i--){
            ac = new Acode("push", rgstrs.get(i).name);
            acode.add(ac);
        }
        ac = new Acode("jsr", "main");
        acode.add(ac);
        ac = new Acode("sys", "halt");
        acode.add(ac);
        for(Ircode c : ircode){
            
            String liveout = c.code; /*+ "     // live var:";
            for(String liveOut: c.out){
                liveout =  liveout + " " +liveOut;
            }*/
            acode.add(new Acode(liveout));
            
            currentIrcode = c;
            if(c.opcode.equals("STOREI")||c.opcode.equals("STOREF")){
                aStoreOp(c);
            }
            else if(c.opcode.equals("ADDI")){
                op = "addi";
                aOp(c, op);
            }
            else if(c.opcode.equals("ADDF")){
                op = "addr";
                aOp(c, op);
            }
            else if(c.opcode.equals("MULTI")){
                op = "muli";
                aOp(c, op);
            }
            else if(c.opcode.equals("MULTF")){
                op = "mulr";
                aOp(c, op);
            }
            else if(c.opcode.equals("DIVI")){
                op = "divi";
                aOp(c, op);
            }
            else if(c.opcode.equals("DIVF")){
                op = "divr";
                aOp(c, op);
            }
            else if(c.opcode.equals("SUBI")){
                op = "subi";
                aOp(c, op);
            }
            else if(c.opcode.equals("SUBF")){
                op = "subr";
                aOp(c, op);
            }
            else if(c.opcode.equals("JUMP")){
                acode.add(new Acode(";end of BLOCK"));
                for(Register rgstr: rgstrs){
                    if(rgstr.var != null){
                        free(rgstr);
                    }
                }
                op = "jmp";
                ac = new Acode(op, c.result.value);
                acode.add(ac);
            }
            else if(c.opcode.equals("LABEL")){
                op = "label";

                ac = new Acode(op, c.result.value);
                acode.add(ac);
                if(funcTable.get(c.result.value) != null){
                    curFun = funcTable.get(c.result.value);
                }
            }
            else if(c.opcode.equals("LINK")){
                op = "link";
                o1 = String.format("%d", curFun.tn + curFun.ln);
                ac = new Acode(op, o1);
                acode.add(ac);
            }
            else if(c.opcode.equals("RET")){
                ac = new Acode("unlnk");
                acode.add(ac);
                ac = new Acode("ret");
                acode.add(ac);
            }
            else if(c.opcode.equals("WRITEI")||c.opcode.equals("WRITEF")||c.opcode.equals("WRITES")){
                writeOp(c);
            }
            else if(c.opcode.equals("READI")||c.opcode.equals("READF")){
                readOp(c);
            }
            
            else if(c.opcode.equals("PUSH")){
                if(c.result == null){
                    ac = new Acode("push");
                    acode.add(ac);
                }
                else{
                    Register r = ensureN(c.result);
                    ac = new Acode("push", r.name);
                    acode.add(ac); 
                }
            }
            else if(c.opcode.equals("POP")){
                if(c.result == null){
                    ac = new Acode("pop");
                    acode.add(ac);
                }
                else{
                    Register r = ensureS(c.result);
                    r.isDirty = true;
                    ac = new Acode("pop", r.name);
                    acode.add(ac);
                }
            }
            else if(c.opcode.equals("JSR")){
                for(i = 0; i <= 3; i++){
                    o1 = String.format("r%d", i);
                    ac = new Acode("push", o1);
                    acode.add(ac);
                }
                ac = new Acode("jsr", c.result.value);
                acode.add(ac);
                for(i = 3; i >= 0; i--){
                    o1 = String.format("r%d", i);
                    ac = new Acode("pop", o1);
                    acode.add(ac);
                }
            }
            else{
                aComp(c);
                //System.out.println(c.oprand1.value + " " + c.oprand2.value);
            }
            updateR();
            //rgstrs.get(0).dirty = false;
            //updateR();
            spill();
            if(c.endofB){
                reset();
            }
        }
    }
    
    public void aOp(Ircode c, String op){
        String opr1;
        String opr2;
        String reg;
        String o1;
        String o2;
        String gtype;
        String value;
        Acode ac;
        Register r1;
        Register r2;
        r1 = ensureN(c.oprand1);
        r2 = ensureN(c.oprand2);
        //ac =  new Acode("move", r2.name, aoprand(c.oprand2));
        //acode.add(ac);
        free(r1);
        ac = new Acode(op,  r2.name, r1.name);
        acode.add(ac);
        
        r1.var = c.result;
        r1.isFree = false;
        r1.isDirty = true;
        /*
        if(c.oprand1.gtype.equals("temp") && c.oprand2.gtype.equals("temp")){
            ac = new Acode(op, opr1, opr2);
            acode.add(ac);
            rtable.put(c.result.value, opr2);
        }
        else if(c.oprand1.gtype.equals("temp") && !c.oprand2.gtype.equals("temp")){
            o1 = aoprand(c.oprand2);
            opr1 = String.format("r%d", rn);
            rn++;
            ac = new Acode("move",o1, opr1);
            acode.add(ac);           
            opr2 = rtable.get(c.oprand1.value);
            ac = new Acode(op, opr1, opr2);
            acode.add(ac);
            rtable.put(c.result.value, opr1);
        }
        else if(!c.oprand1.gtype.equals("temp") && c.oprand2.gtype.equals("temp")){
            o1 = aoprand(c.oprand1);
            opr1 = String.format("r%d", rn);
            rn++;
            ac = new Acode("move",o1, opr1);
            acode.add(ac);           
            opr2 = rtable.get(c.oprand2.value);
            ac = new Acode(op, opr2, opr1);
            acode.add(ac);
            rtable.put(c.result.value, opr1);
        }
        else{
            o1 = aoprand(c.oprand1);
            opr1 = String.format("r%d", rn);
            rn++;
            ac = new Acode("move",o1, opr1);
            acode.add(ac);           
            opr2 = aoprand(c.oprand2);
            ac = new Acode(op, opr2, opr1);
            acode.add(ac);
            rtable.put(c.result.value, opr1);
        }*/
    }

    public void aComp(Ircode c){
        String op;
        Acode ac;
        String reg;
        String OP = c.opcode;
        Register r1 = ensureN(c.oprand1);
        Register r2 = ensureN(c.oprand2);
        if(c.oprand1.dtype.equals("INT")){
            op = "cmpi";
        }
        else{
            op = "cmpr";
        }
        ac = new Acode(op, r1.name, r2.name);
        acode.add(ac);
        reset();
        if(OP == "EQ"){
            op = "jeq";
        }
        else if(OP.equals("NE")){
            op = "jne";
        }
        else if(OP == "GT"){
            op = "jgt";
        }
        else if(OP == "LT"){
            op = "jlt";
        }
        else if(OP == "GE"){
            op = "jge";
        }
        else if(OP.equals("LE")){
            op = "jle";
        }
        ac = new Acode(op,c.result.value);
        acode.add(ac);
    }


    public void aStoreOp(Ircode c){
        String gtype;
        String value;
        String opr1;
        String opr2;
        String reg;
        String o1;
        String o2;
        String op = "move";
        Acode ac;
        Register r;
        //reg = String.format("r%d ir", regNo);
        if(c.oprand1.gtype.equals("INTLITERAL") || c.oprand1.gtype.equals("FLOATLITERAL")){
            opr1 = c.oprand1.value;
            //System.out.println(c.result.value);
            r = ensureS(c.result);
            opr2 = r.name;
            r.isDirty = true;
            ac = new Acode(op, opr1, opr2);
            acode.add(ac);
        }
        else if(c.result.gtype.equals("return")){
            r = ensureN(c.oprand1);
            r.isDirty = true;
            ac = new Acode(op, r.name, aoprand(c.result));
            acode.add(ac);
        }
        else{
            //System.out.println(c.gtype)
            Register r1 = ensureN(c.oprand1);
            r1.isDirty = true;
            Register r2 = ensureS(c.result);
            r2.isDirty = true;
            ac = new Acode(op, r1.name, r2.name);
            acode.add(ac);
        }
    }

    public void writeOp(Ircode c){
        String opr1 = null;
        String op;
        if(c.opcode.equals("WRITEI")){
            Register r = ensureN(c.result);
            opr1 = r.name;
            op = "sys writei";
        }
        else if(c.opcode.equals("WRITEF")){
            Register r = ensureN(c.result);
            opr1 = r.name;
            op = "sys writer";
        }
        else{
            opr1 = c.result.value;
            op = "sys writes";
        }
        Acode ac = new Acode(op, opr1);
        //ac.printCode();
        acode.add(ac);
    }
    public void readOp(Ircode c){
        Register r = ensureS(c.result);
        r.isDirty = true;
        String op;
        if(c.result.dtype.equals("INT")){
            op = "sys readi";
        }
        else{
            op = "sys readr";
        }
        String opr1 = r.name;
        //System.out.println(c.result.gtype + " " + c.result.value);
        Acode ac = new Acode(op, opr1);
        //ac.printCode();
        acode.add(ac);
    }
    public String irPtoaP(String paramn){
        int p;
        String ap;
        ap = paramn.replaceAll("\\D+","");
        p = 6 + curFun.pn - Integer.parseInt(ap);
        //System.out.println(curFun.pn + " " +p);
        ap = String.format("$%d", p);
        return ap;
    }
    public String irLtoaL(String local){
        int l;
        String al;
        al = local.replaceAll("\\D+","");
        l = - Integer.parseInt(al);
        al = String.format("$%d", l);
        return al;
    }
    public String irTtoaT(String temp){
        int t;
        String at;
        at = temp.replaceAll("\\D+","");
        t = - (curFun.ln + Integer.parseInt(at));
        at = String.format("$%d", t);
        return at;
    }
    public String aoprand(Irnode ir){
        String gtype = ir.gtype;
        String value = ir.value;
        String aop;
        if(gtype.equals("id")){
            aop = value;
        }
        else if(gtype.equals("param")){
            aop = irPtoaP(value);
        }
        else if(gtype.equals("local")){
            aop = irLtoaL(value);
        }
        else if(gtype.equals("temp")){
            aop = irTtoaT(value);   
        }
        else{
            aop = String.format("$%d",6 + curFun.pn);
            //System.out.println("I missed something here");
        }
        return aop;
    }


    public Register ensureS(Irnode opr){
        for(Register rgstr : rgstrs){
            if(rgstr.var != null ){
                if(rgstr.var.equal(opr)){
                    return rgstr;    
                }
                //acode.add(new Acode(rgstr.name, rgstr.var.value));
            }
        }
        Register rgstr = allocate(opr);

        //Acode c = new Acode("move", aoprand(opr), rgstr.name);
        //acode.add(c);
        return rgstr;
    }
    public Register ensureN(Irnode opr){
        for(Register rgstr : rgstrs){
            if(rgstr.var != null ){
                if(rgstr.var.equal(opr)){
                    return rgstr;    
                }
                //acode.add(new Acode(rgstr.name, rgstr.var.value));
            }
        }
        Register rgstr = allocate(opr);

        Acode c = new Acode("move", aoprand(opr), rgstr.name);
        acode.add(c);
        return rgstr;
    }
    public void free(Register r){
        acode.add(new Acode(";free",r.var.value));
        if(r.isDirty&& currentIrcode.out.contains(r.var.value)){
            String var = aoprand(r.var);
            Acode c =  new Acode("move", r.name, var);
            acode.add(c);
        }
        r.isFree = true;
        r.isDirty = false;
        r.var = null;
    }
    
    public Register allocate(Irnode opr){
        boolean rfree = false;
        Register r = null;
        for(Register rgstr : rgstrs){
            if(rgstr.isFree){
                rgstr.var = opr;
                rgstr.isFree = false;
                return rgstr;
            }
        }
        for(Register rgstr : rgstrs){
            if(currentIrcode.oprand1 != null && currentIrcode.oprand2 != null){
                if(!rgstr.var.equal(currentIrcode.oprand1) && !rgstr.var.equal(currentIrcode.oprand2)){
                    free(rgstr);
                    rgstr.var = opr;
                    rgstr.isFree = false;
                    r = rgstr;
                    break;
                }
            }
            else if(currentIrcode.oprand1 != null){
                if(!rgstr.var.equal(currentIrcode.oprand1)){
                    free(rgstr);
                    rgstr.var = opr;
                    rgstr.isFree = false;
                    r = rgstr;
                    break; 
                }    
            }
            else{
                free(rgstr);
                rgstr.var = opr;
                rgstr.isFree = false;
                r = rgstr;  
                break;
            }
        }
        if(r == null){
            System.out.println("Shit should not be happenning here");
        }
        return r;
    }
    
    /******************************************************
    * Register Allocation
    ******************************************************/

    /******************************************************
    * 
    ******************************************************/
    
    public void basicBlocks(){
        Ircode c;
        Ircode l;
        ArrayList<Integer> lderIndex = new ArrayList<Integer>();
        ListIterator<Ircode> litr = ircode.listIterator();
        Integer index;
        while(litr.hasNext()){
            c = litr.next();
            if(c.opcode.equals("EQ")){
                l = litr.next();
                litr.previous();
                leaders.add(l);
                index = new Integer(ircode.indexOf(l));
                lderIndex.add(index);
            }
            else if(c.opcode.equals("NE")){
                l = litr.next();
                litr.previous();
                leaders.add(l);
                index = new Integer(ircode.indexOf(l));
                lderIndex.add(index);
            }
            else if(c.opcode.equals("GT")){
                l = litr.next();
                litr.previous();
                leaders.add(l);
                index = new Integer(ircode.indexOf(l));
                lderIndex.add(index);
            }
            else if(c.opcode.equals("LT")){
                l = litr.next();
                litr.previous();
                leaders.add(l);
                index = new Integer(ircode.indexOf(l));
                lderIndex.add(index);
            }
            else if(c.opcode.equals("GE")){
                l = litr.next();
                litr.previous();
                leaders.add(l);
                index = new Integer(ircode.indexOf(l));
                lderIndex.add(index);
            }
            else if(c.opcode.equals("LE")){
                l = litr.next();
                litr.previous();
                leaders.add(l);
                index = new Integer(ircode.indexOf(l));
                lderIndex.add(index);
            }
            else if(ircode.indexOf(c) == 0){
                leaders.add(c);
                index = new Integer(0);
                lderIndex.add(index);
            }
            else if(c.opcode.equals("LABEL")){
                if(!c.result.gtype.equals("FUNCTION")){
                    leaders.add(c);
                    index = new Integer(ircode.indexOf(c));
                    lderIndex.add(index);
                }
            }
            
        }
        
        for(Integer i: lderIndex){
            if(i > 0){
                ircode.get(i-1).endofB = true;
            }
        }
        ircode.get(ircode.size() - 1).endofB = true;
    }
    
    public void ctrFlowGraph(){
        Ircode c;
        ListIterator<Ircode> litr = ircode.listIterator();
        while(litr.hasNext()){
            c = litr.next();
            if(c.opcode.equals("EQ")){
                c.successors.add(litr.next());
                litr.previous();
                litr.next().predecessors.add(c);
                litr.previous();
                for(Ircode l: leaders){
                    if(l.opcode.equals("LABEL")&& l.result.value.equals(c.result.value)){
                        c.successors.add(l);
                        l.predecessors.add(c);
                    }
                }
            }
            else if(c.opcode.equals("NE")){
                c.successors.add(litr.next());
                litr.previous();
                litr.next().predecessors.add(c);
                litr.previous();
                for(Ircode l: leaders){
                    if(l.opcode.equals("LABEL")&& l.result.value.equals(c.result.value)){
                        c.successors.add(l);
                        l.predecessors.add(c);
                    }
                }
            }
            else if(c.opcode.equals("GT")){
                c.successors.add(litr.next());
                litr.previous();
                litr.next().predecessors.add(c);
                litr.previous();
                for(Ircode l: leaders){
                    if(l.opcode.equals("LABEL")&& l.result.value.equals(c.result.value)){
                        c.successors.add(l);
                        l.predecessors.add(c);
                    }
                }  
            }
            else if(c.opcode.equals("LT")){
                c.successors.add(litr.next());
                litr.previous();
                litr.next().predecessors.add(c);
                litr.previous();
                for(Ircode l: leaders){
                    if(l.opcode.equals("LABEL")&& l.result.value.equals(c.result.value)){
                        c.successors.add(l);
                        l.predecessors.add(c);
                    }
                }   
            }
            else if(c.opcode.equals("GE")){
                c.successors.add(litr.next());
                litr.previous();
                litr.next().predecessors.add(c);
                litr.previous();
                for(Ircode l: leaders){
                    if(l.opcode.equals("LABEL")&& l.result.value.equals(c.result.value)){
                        c.successors.add(l);
                        l.predecessors.add(c);
                    }
                }  
            }
            else if(c.opcode.equals("LE")){
                c.successors.add(litr.next());
                litr.previous();
                litr.next().predecessors.add(c);
                litr.previous();
                for(Ircode l: leaders){
                    if(l.opcode.equals("LABEL")&& l.result.value.equals(c.result.value)){
                        c.successors.add(l);
                        l.predecessors.add(c);
                    }
                }                
            }
            else if(c.opcode.equals("JUMP")){
                for(Ircode l: leaders){
                    if(l.opcode.equals("LABEL")&& l.result.value.equals(c.result.value)){
                        c.successors.add(l);
                        l.predecessors.add(c);
                    }
                }
            }
            else if(c.opcode.equals("RET")){
                if(c.endofF != true){
                    //do nothing there is no successor    
                    c.successors.add(litr.next());
                    litr.previous();
                    litr.next().predecessors.add(c);
                    litr.previous();
                }
            }
            else{
                if(litr.hasNext()){
                    c.successors.add(litr.next());
                    litr.previous();
                    litr.next().predecessors.add(c);
                    litr.previous();
                }
            }
        }
    }
    
    public void livenessCheck(){
        //KILL & GEN
        for(Ircode c: ircode){
            if(c.opcode.equals("STOREI") || c.opcode.equals("STOREF")){
                if(!(c.oprand1.gtype.equals("INTLITERAL") || c.oprand1.gtype.equals("FLOATLITERAL"))){
                    c.gen.add(c.oprand1.value);
                }
                c.kill.add(c.result.value);
            }
            else if(c.opcode.equals("EQ")){
                c.gen.add(c.oprand1.value);
                c.gen.add(c.oprand2.value);
            }
            else if(c.opcode.equals("NE")){
                c.gen.add(c.oprand1.value);
                c.gen.add(c.oprand2.value);
            }
            else if(c.opcode.equals("GE")){
                c.gen.add(c.oprand1.value);
                c.gen.add(c.oprand2.value);
            }
            else if(c.opcode.equals("GT")){
                c.gen.add(c.oprand1.value);
                c.gen.add(c.oprand2.value);
            }
            else if(c.opcode.equals("LE")){
                c.gen.add(c.oprand1.value);
                c.gen.add(c.oprand2.value);
            }
            else if(c.opcode.equals("LT")){
                c.gen.add(c.oprand1.value);
                c.gen.add(c.oprand2.value);
            }
            else if(c.opcode.equals("ADDI") || c.opcode.equals("ADDF")){
                c.gen.add(c.oprand1.value);
                c.gen.add(c.oprand2.value);
                c.kill.add(c.result.value);
            }
            else if(c.opcode.equals("SUBI") || c.opcode.equals("SUBF")){
                c.gen.add(c.oprand1.value);
                c.gen.add(c.oprand2.value);
                c.kill.add(c.result.value);
            }
            else if(c.opcode.equals("MULTI") || c.opcode.equals("MULTF")){
                c.gen.add(c.oprand1.value);
                c.gen.add(c.oprand2.value);
                c.kill.add(c.result.value);
            }
            else if(c.opcode.equals("DIVI") || c.opcode.equals("DIVF")){
                c.gen.add(c.oprand1.value);
                c.gen.add(c.oprand2.value);
                c.kill.add(c.result.value);
            }
            else if(c.opcode.equals("PUSH") && c.result != null){
                c.gen.add(c.result.value);
            }
            else if(c.opcode.equals("POP") && c.result != null){
                c.kill.add(c.result.value);
            }
            else if((c.opcode.equals("WRITEI") || c.opcode.equals("WRITEF") || c.opcode.equals("WRITES"))&& c.result != null){
                    c.gen.add(c.result.value);
                
            }
            else if((c.opcode.equals("READI") || c.opcode.equals("READF")) && c.result != null){
                    c.kill.add(c.result.value);
            }
            else if(c.opcode.equals("JSR")){
                /*for(String s: globalTable.keySet()){
                    Symbol var = globalTable.get(s);
                    if(!var.type.equals("STRING")){
                        c.gen.add(s);
                    }
                }*/
                //c.gen.addAll(globalTable.keySet());
            }
            else if(c.opcode.equals("RET")){
                /* for(String s: globalTable.keySet()){
                    Symbol var = globalTable.get(s);
                    if(!var.type.equals("STRING")){
                        c.out.add(s);
                    }
                }*/
                //c.out.addAll(globalTable.keySet());
                    //System.out.println(s.value);
            }
        }
        
        Stack<Ircode> ircodeStack = new Stack<Ircode>();
        for(Ircode c: ircode){
            ircodeStack.push(c);
        }
        while(!ircodeStack.empty()){
            boolean update = false;
            Ircode c = ircodeStack.pop();
            LinkedHashSet<String> cloneIn = new LinkedHashSet<String>();
            cloneIn.addAll(c.in);//(LinkedHashSet<String>)c.in.clone();
            
            for(Ircode s: c.successors){
                if(!c.out.containsAll(s.in)){
                    c.out.addAll(s.in);
                }
            }
            if(!c.in.containsAll(c.out)){
                c.in.addAll(c.out);
            }
            for(String vkill: c.kill){
                if(c.in.contains(vkill)){
                    c.in.remove(vkill);
                }
            }
            if(!c.in.containsAll( c.gen)){
                c.in.addAll(c.gen);
            }
            
            if(c.in.size()!= cloneIn.size()){
                update = true;
            }
            else{
                for(String livein: c.in){
                    if(!cloneIn.contains(livein)){
                        update = true;
                    }
                }    
            }
            if(update == true){
                //System.out.println("wo zhge ge ge ge g e ge ");
                for(Ircode p: c.predecessors){
                    ircodeStack.push(p);
                }
            }
        }
    }
    public void updateR(){
        String r0;
        String r1;
        String r2;
        String r3;
        String dirty = "";
        if(rgstrs.get(0).isDirty){
                dirty = " :dirty";
            }
        if(rgstrs.get(0).var!=null){
            r3 = ";r3: " + rgstrs.get(0).var.value + dirty;
        }
        else{
            r3 = ";r3: null" + dirty;
        }
        dirty = "";
        if(rgstrs.get(1).isDirty){
                dirty = " :dirty";
            }
        if(rgstrs.get(1).var!=null){
            r2 = ";r2: " + rgstrs.get(1).var.value + dirty;
        }
        else{
            r2 = ";r2: null" + dirty;
        }
        dirty = "";
        if(rgstrs.get(2).isDirty == true){
                dirty = " :dirty";
            }
        if(rgstrs.get(2).var!=null){
            r1 = ";r1: " + rgstrs.get(2).var.value + dirty;
        }
        else{
            r1 = ";r1: null" + dirty;
        }
        dirty = "";
        if(rgstrs.get(3).isDirty == true){
                dirty = " :dirty";
            }
        if(rgstrs.get(3).var!=null){
            r0 = ";r0: " + rgstrs.get(3).var.value + dirty;
        }
        else{
            r0 = ";r0: null" + dirty;
        }
        Acode c = new Acode(r0, r1, r2, r3);
        acode.add(c);
    }
    
    public void spill(){
        for(Register rgstr: rgstrs){
            if(rgstr.var != null){
                if(!currentIrcode.out.contains(rgstr.var.value)){   
                    free(rgstr);
                }
            }
        }
    }
    public void reset(){
        acode.add(new Acode(";end of BLOCK"));
        for(Register rgstr: rgstrs){
            if(rgstr.var != null){
                free(rgstr);
            }
        }
    }
}
