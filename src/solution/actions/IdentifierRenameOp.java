package solution.actions;

import ast.IdentifierExpr;
import ast.VarDecl;
import solution.RenameOpParams;

public class IdentifierRenameOp extends RenameOp<IdentifierExpr> {

    public IdentifierRenameOp(RenameOpParams params, IdentifierExpr node) {
        super(params, node);
    }

    @Override
    public void rename() {
        node.setId(params.newName);
    }
}
