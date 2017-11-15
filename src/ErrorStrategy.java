import org.antlr.v4.runtime.*;

public class CustomErrorStrategy extends DefaultErrorStrategy {
        //public Boolean errorOccur = false;
        public void reportError(Parser recognizer, RecognitionException e)
        {
                //errorOccur = true;
        System.out.println("Not Accepted");
        System.exit(1);
        }/*
    public void recover(Parser recognizer,RecognitionException e) throws RecognitionException {

    }
    public void reportMatch(Parser recognizer){

    }
    public void sync(Parser recognizer) throws RecognitionException{

    }*/
}