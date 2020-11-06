package solution.actions;

import ast.AssignStatement;
import ast.VarDecl;
import solution.RenameOpParams;

public class LvRenameOp extends RenameOp<AssignStatement> {

    public LvRenameOp(RenameOpParams params, AssignStatement node) {
        super(params, node);
    }

    @Override
    public void rename() {
        node.setLv(params.newName);
    }
}
