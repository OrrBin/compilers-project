package solution.visitors;

import ast.MethodDecl;
import ast.VarDecl;
import solution.RenameOpParams;
import solution.actions.RenameOp;
import solution.actions.VarDeclRenameOp;

import java.util.List;

public class RenameLocalVariableVisitor extends RenameVariableVisitor {

    public RenameLocalVariableVisitor(RenameOpParams op, List<RenameOp<?>> renameOps) {
        super(op, renameOps);
    }

    @Override
    public void visit(VarDecl varDecl) {
        if (varDecl.name().equals(op.originalName)) {
            renameOps.add(new VarDeclRenameOp(op, varDecl));
        }
    }

    @Override
    public void visit(MethodDecl methodDecl) {
        methodDecl.vardecls().forEach((var -> var.accept(this)));
        methodDecl.body().forEach((statement -> statement.accept(this)));
        methodDecl.ret().accept(this);
    }
}
