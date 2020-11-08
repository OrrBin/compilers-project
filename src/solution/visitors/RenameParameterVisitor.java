package solution.visitors;

import ast.FormalArg;
import ast.MethodDecl;
import solution.RenameOpParams;
import solution.actions.FormalArgRenameOp;
import solution.actions.RenameOp;

import java.util.List;

public class RenameParameterVisitor extends RenameVariableVisitor {

    public RenameParameterVisitor(RenameOpParams op, List<RenameOp<?>> renameOps) {
        super(op, renameOps);
    }

    @Override
    public void visit(FormalArg formalArg) {
        if (formalArg.name().equals(op.originalName)) {
            renameOps.add(new FormalArgRenameOp(op, formalArg));
        }
    }

    @Override
    public void visit(MethodDecl methodDecl) {
        methodDecl.formals().forEach((formal -> formal.accept(this)));
        methodDecl.body().forEach((statement -> statement.accept(this)));
        methodDecl.ret().accept(this);
    }
}
