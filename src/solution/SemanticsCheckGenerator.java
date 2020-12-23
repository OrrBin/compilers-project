package solution;

import ast.Program;
import solution.utils.AstNodeUtil;
import solution.utils.LLVMUtil;
import solution.visitors.LLVMVisitor;
import solution.visitors.SemanticsCheckVisitor;
import solution.visitors.VTableVisitor;

import java.io.OutputStream;

public class SemanticsCheckGenerator {

    private Program program;
    private AstNodeUtil util;

    public SemanticsCheckGenerator(Program program, AstNodeUtil util) {
        this.program = program;
        this.util = util;
    }

    public void generate(OutputStream outputStream) {

        SemanticsCheckVisitor semanticsCheckVisitor = new SemanticsCheckVisitor(outputStream, util);
        program.accept(semanticsCheckVisitor);
    }
}
