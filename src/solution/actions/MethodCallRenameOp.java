package solution.actions;

import ast.MethodCallExpr;
import solution.RenameOpParams;

public class MethodCallRenameOp extends RenameOp<MethodCallExpr> {

    public MethodCallRenameOp(RenameOpParams params, MethodCallExpr node) {
        super(params, node);
    }

    @Override
    public void rename() {
        node.setMethodId(params.newName);
    }

}
