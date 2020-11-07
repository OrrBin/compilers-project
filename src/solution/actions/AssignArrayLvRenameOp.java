package solution.actions;

import ast.AssignArrayStatement;
import ast.AssignStatement;
import solution.RenameOpParams;

public class AssignArrayLvRenameOp extends RenameOp<AssignArrayStatement> {

    public AssignArrayLvRenameOp(RenameOpParams params, AssignArrayStatement node) {
        super(params, node);
    }

    @Override
    public void rename() {
        node.setLv(params.newName);
    }
}
