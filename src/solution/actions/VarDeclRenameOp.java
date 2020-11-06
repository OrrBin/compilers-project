package solution.actions;

import ast.VarDecl;
import solution.RenameOpParams;

public class VarDeclRenameOp extends RenameOp<VarDecl> {

    public VarDeclRenameOp(RenameOpParams params, VarDecl node) {
        super(params, node);
    }

    @Override
    public void rename() {
        node.setName(params.newName);
    }
}
