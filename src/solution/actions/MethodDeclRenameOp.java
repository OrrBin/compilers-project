package solution.actions;

import ast.MethodDecl;
import solution.RenameOpParams;

public class MethodDeclRenameOp extends RenameOp<MethodDecl>{

    public MethodDeclRenameOp(RenameOpParams params, MethodDecl node) {
        super(params, node);
    }

    @Override
    public void rename() {
        node.setName(params.newName);
    }
}
