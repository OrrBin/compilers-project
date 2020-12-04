package solution;

import ast.Program;
import solution.visitors.VTableVisitor;

import java.io.OutputStream;

public class LLVMGenerator {

    private Program program;
    private AstNodeUtil util;

    public LLVMGenerator(Program program, AstNodeUtil util) {
        this.program = program;
        this.util = util;
    }

    public void generate(OutputStream outputStream) {
        VTableVisitor vtableVisitor = new VTableVisitor(outputStream, util);
        program.accept(vtableVisitor);
    }
}
