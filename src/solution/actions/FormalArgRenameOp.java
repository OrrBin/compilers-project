package solution.actions;

import ast.FormalArg;
import ast.VarDecl;
import solution.RenameOpParams;

public class FormalArgRenameOp extends RenameOp<FormalArg> {

    public FormalArgRenameOp(RenameOpParams params, FormalArg node) {
        super(params, node);
    }

    @Override
    public void rename() {
        node.setName(params.newName);
    }
}
