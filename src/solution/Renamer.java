package solution;

import ast.MethodDecl;
import ast.Program;
import solution.visitors.RenameLocalVariableVisitor;

public class Renamer {

    private Program prog;
    private ProgramCrawler crawler;

    public Renamer(Program prog) {
        this.prog = prog;
        this.crawler = new ProgramCrawler(prog);
    }

    public void rename(RenameOp op) {
        if(op.isMethod) {
            renameMethod(op);
        } else {
            renameVariable(op);
        }
    }

    public void renameVariable(RenameOp op) {
        VariableType variableType = findVariableType(op.originalName, op.originalLine);
        switch (variableType) {
            case FIELD: renameField(op); break;
            case LOCAL: renameLocal(op); break;
            case PARAMETER: renameParameter(op); break;
        }
    }

    private void renameLocal(RenameOp op) {
        MethodDecl method = crawler.findAncestor(op.originalLine, MethodDecl.class);
        RenameLocalVariableVisitor visitor = new RenameLocalVariableVisitor(op);
        method.vardecls().forEach((var -> var.accept(visitor)) );
        method.body().forEach((statement -> statement.accept(visitor)) );
    }

    private void renameParameter(RenameOp op) {
    }

    private void renameField(RenameOp op) {
    }



    public void renameMethod(RenameOp op) {

    }

    private VariableType findVariableType(String originalName, int originalLine) {
        return VariableType.LOCAL;
    }
}
