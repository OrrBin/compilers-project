package solution.visitors;

import ast.MethodDecl;
import solution.RenameOpParams;
import solution.actions.RenameOp;

import java.util.List;

public class RenameParameterVisitor extends RenameVariableVisitor {

    public RenameParameterVisitor(RenameOpParams op, List<RenameOp<?>> renameOps) {
        super(op, renameOps);
    }

    @Override
    public void visit(MethodDecl methodDecl) {
        methodDecl.formals().forEach((formal -> formal.accept(this)));
        methodDecl.body().forEach((statement -> statement.accept(this)));
    }
}
