import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
//import java.util.HashMap;
public class Micro {
        public static void main(String[] args) throws Exception{
                CharStream inputdata = new ANTLRFileStream(args[0]);
                MicroLexer lexer = new MicroLexer(inputdata);
                /*while(lexer._hitEOF == false){
                        Token tk = lexer.nextToken();
                        if(tk.getType() !=-1 && lexer.ruleNames[tk.getType()-1] != "COMMENT" ){
                                System.out.printf("Token Type: %s\n",lexer.ruleNames[tk.getType()-1]);
                                System.out.printf("Value: %s\n",tk.getText());
                        }
                }*/
                CommonTokenStream tkStream = new CommonTokenStream(lexer);
                MicroParser parser = new MicroParser(tkStream); 
                CustomErrorStrategy es = new CustomErrorStrategy();
                parser.setErrorHandler(es);
                parser.setBuildParseTree(true);
                ParserRuleContext ctxTree = parser.program();

                ParseTreeWalker.DEFAULT.walk(new CustomListener(), ctxTree);


                //System.out.println("Accepted");

        }
}