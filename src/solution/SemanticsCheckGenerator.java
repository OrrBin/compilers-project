package solution;

import ast.Program;
import solution.exceptions.InitializationException;
import solution.exceptions.SemanticException;
import solution.utils.AstNodeUtil;
import solution.utils.LLVMUtil;
import solution.visitors.InitializationCheckVisitor;
import solution.visitors.LLVMVisitor;
import solution.visitors.SemanticsCheckVisitor;
import solution.visitors.VTableVisitor;

import java.io.IOException;
import java.io.OutputStream;

public class SemanticsCheckGenerator {

    private Program program;
    private AstNodeUtil util;
    private static final String OK = "OK\n";
    private static final String ERROR = "ERROR\n";

    public SemanticsCheckGenerator(Program program, AstNodeUtil util) {
        this.program = program;
        this.util = util;
    }


    public void generate(OutputStream outputStream) throws IOException {
        InitializationCheckVisitor initializationCheckVisitor = new InitializationCheckVisitor(util);
        SemanticsCheckVisitor semanticsCheckVisitor = new SemanticsCheckVisitor(outputStream, util);
        try{
            program.accept(semanticsCheckVisitor);
            program.accept(initializationCheckVisitor);
        }
        catch (InitializationException | SemanticException e){
            outputStream.write(ERROR.getBytes());
            return;
        }
        outputStream.write(OK.getBytes());
    }
}
