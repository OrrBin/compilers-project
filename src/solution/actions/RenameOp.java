package solution.actions;

import ast.AstNode;
import solution.RenameOpParams;

public abstract class RenameOp <T extends AstNode> {
    protected final RenameOpParams params;
    protected final T node;

    public RenameOp(RenameOpParams params, T node) {
        this.params = params;
        this.node = node;
    }

    public RenameOpParams getParams() {
        return params;
    }

    public T getNode() {
        return node;
    }

    public abstract void rename();
}
