package solution;

import ast.MethodDecl;
import ast.Program;
import solution.actions.RenameOp;
import solution.visitors.RenameLocalVariableVisitor;
import solution.visitors.RenameParameterVisitor;

import java.util.List;

public class Renamer {

    private Program prog;
    private ProgramCrawler crawler;
    private List<RenameOp<?>> renameOps;

    public Renamer(Program prog) {
        this.prog = prog;
        this.crawler = new ProgramCrawler(prog);
    }

    public void rename(RenameOpParams op) {
        if(op.isMethod) {
            renameMethod(op);
        } else {
            renameVariable(op);
        }
    }

    public void renameVariable(RenameOpParams op) {
        VariableType variableType = findVariableType(op.originalName, op.originalLine);
        switch (variableType) {
            case FIELD: renameField(op); break;
            case LOCAL: renameLocal(op); break;
            case PARAMETER: renameParameter(op); break;
        }
    }

    private void renameLocal(RenameOpParams op) {
        MethodDecl method = crawler.findAncestor(op.originalLine, MethodDecl.class);
        RenameLocalVariableVisitor visitor = new RenameLocalVariableVisitor(op, renameOps);
        method.accept(visitor);
    }

    private void renameParameter(RenameOpParams op) {
        MethodDecl method = crawler.findAncestor(op.originalLine, MethodDecl.class);
        RenameParameterVisitor visitor = new RenameParameterVisitor(op, renameOps);
        method.accept(visitor);
    }

    private void renameField(RenameOpParams op) {
    }

    private void executeRenameOps(List<RenameOp<?>> renameOps) {
        renameOps.forEach(RenameOp::rename);
    }

    public void renameMethod(RenameOpParams op) {

    }

    private VariableType findVariableType(String originalName, int originalLine) {
        return VariableType.LOCAL;
    }
}
