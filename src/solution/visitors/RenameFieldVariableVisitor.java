package solution.visitors;

import ast.*;
import solution.RenameOpParams;
import solution.actions.*;
import solution.utils.AstNodeUtil;

import java.util.List;

// Oz

public class RenameFieldVariableVisitor extends RenameVariableVisitor {

    private AstNodeUtil astNodeUtil;

    public RenameFieldVariableVisitor(RenameOpParams op, List<RenameOp<?>> renameOps, AstNodeUtil astNodeUtil) {
        super(op, renameOps);
        this.astNodeUtil = astNodeUtil;
    }

    @Override
    public void visit(ClassDecl classDecl) {
        classDecl.fields().forEach(field -> field.accept(this));
        classDecl.methoddecls().forEach(method -> method.accept(this));
    }


    @Override
    public void visit(MethodDecl methodDecl) {
       methodDecl.body().forEach(statement -> statement.accept(this));
       methodDecl.ret().accept(this);
    }

    @Override
    public void visit(VarDecl varDecl) {
        if (varDecl.name().equals(op.originalName) && astNodeUtil.isField(varDecl)){
            renameOps.add(new VarDeclRenameOp(op, varDecl));
        }
    }

    @Override
    public void visit(AssignStatement assignStatement) {
        boolean isField = true;
        var lv = assignStatement.lv();
        var name = op.originalName;
        if (lv.equals(name)) {
            MethodDecl method = (MethodDecl) astNodeUtil.getEnclosingScope(assignStatement).symbolTableScope;
            for (VarDecl var : method.vardecls()) {
                if (var.name().equals(name)) {
                    /* enclosing method contains local variable with same name,
                      since var declarations are at the beginning of the method - the local var overrides the field */
                    isField = false;
                }
            }
            for (FormalArg arg : method.formals()) {
                if (arg.name().equals(name)) {
                    /* enclosing method contains formal arg with same name */
                    isField = false;
                }
            }
            if (isField) renameOps.add(new AssignmentLvRenameOp(op,assignStatement));
        }

        var rv = assignStatement.rv();
        rv.accept(this);
    }

    @Override
    public void visit(AssignArrayStatement assignArrayStatement) {
        boolean isField = true;
        var lv = assignArrayStatement.lv();
        var name = op.originalName;
        if (lv.equals(name)) {
            MethodDecl method = astNodeUtil.getMethod(assignArrayStatement);
            for (VarDecl var : method.vardecls()) {
                if (var.name().equals(name)) {
                    /* enclosing method contains local variable with same name,
                      since var declarations are at the beginning of the method - the local var overrides the field */
                    isField = false;
                }
            }
            for (FormalArg arg : method.formals()) {
                if (arg.name().equals(name)) {
                    /* enclosing method contains formal arg with same name */
                    isField = false;
                }
            }
            if (isField) renameOps.add(new AssignArrayLvRenameOp(op,assignArrayStatement));
        }

        assignArrayStatement.index().accept(this);

        var rv = assignArrayStatement.rv();
        rv.accept(this);

    }

    @Override
    public void visit(IdentifierExpr e) {
        if (e.id().equals(op.originalName) && astNodeUtil.isField(e)) {
            renameOps.add(new IdentifierRenameOp(op, e));
        }
    }

}
