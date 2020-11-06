package solution.visitors;

import ast.*;
import solution.actions.RenameOp;
import solution.RenameOpParams;

import java.util.List;

public class RenameLocalVariableVisitor implements Visitor {

    private RenameOpParams op;
    private List<RenameOp<?>> renameOps;

    public RenameLocalVariableVisitor(RenameOpParams op, List<RenameOp<?>> renameOps) {
        this.op = op;
        this.renameOps = renameOps;
    }

    @Override
    public void visit(Program program) {
        // No need for action
    }

    @Override
    public void visit(ClassDecl classDecl) {
        // No need for action
    }

    @Override
    public void visit(MainClass mainClass) {
        // No need for action
    }

    @Override
    public void visit(MethodDecl methodDecl) {
        methodDecl.vardecls().forEach((var -> var.accept(this)) );
        methodDecl.body().forEach((statement -> statement.accept(this)) );
    }

    @Override
    public void visit(FormalArg formalArg) {
        // No need for action
    }

    @Override
    public void visit(VarDecl varDecl) {
        if (varDecl.name().equals(op.originalName)) {
            varDecl.setName(op.newName);
        }
    }

    @Override
    public void visit(BlockStatement blockStatement) {
        if (blockStatement.lineNumber < op.originalLine)
            return;

        blockStatement.statements().forEach((statement -> statement.accept(this)));
    }

    @Override
    public void visit(IfStatement ifStatement) {
        if (ifStatement.lineNumber < op.originalLine)
            return;

        ifStatement.cond().accept(this);
        ifStatement.thencase().accept(this);
        ifStatement.elsecase().accept(this);
    }

    @Override
    public void visit(WhileStatement whileStatement) {
        if (whileStatement.lineNumber < op.originalLine)
            return;

        whileStatement.cond().accept(this);
        whileStatement.body().accept(this);
    }

    @Override
    public void visit(SysoutStatement sysoutStatement) {
        //TODO: Or Habat
    }

    @Override
    public void visit(AssignStatement assignStatement) {
        if (assignStatement.lineNumber < op.originalLine)
            return;

        var lv = assignStatement.lv();
        if (lv.equals(op.originalName)) {
            assignStatement.setLv(op.newName);
        }

        var rv = assignStatement.rv();
        rv.accept(this);
    }

    @Override
    public void visit(AssignArrayStatement assignArrayStatement) {
        //TODO: Oz
    }

    @Override
    public void visit(AndExpr e) {
        e.e1().accept(this);
        e.e2().accept(this);
    }

    @Override
    public void visit(LtExpr e) {
        e.e1().accept(this);
        e.e2().accept(this);
    }

    @Override
    public void visit(AddExpr e) {
        e.e1().accept(this);
        e.e2().accept(this);
    }

    @Override
    public void visit(SubtractExpr e) {
        e.e1().accept(this);
        e.e2().accept(this);
    }

    @Override
    public void visit(MultExpr e) {
        e.e1().accept(this);
        e.e2().accept(this);
    }

    @Override
    public void visit(ArrayAccessExpr e) {
        //TODO: Oz
    }

    @Override
    public void visit(ArrayLengthExpr e) {
        //TODO: Oz
    }

    @Override
    public void visit(MethodCallExpr e) {
        // No need for action
    }

    @Override
    public void visit(IntegerLiteralExpr e) {
        // No need for action
    }

    @Override
    public void visit(TrueExpr e) {
        // No need for action
    }

    @Override
    public void visit(FalseExpr e) {
        // No need for action
    }

    @Override
    public void visit(IdentifierExpr e) {
        if (e.id().equals(op.originalName))
            e.setId(op.newName);
    }

    @Override
    public void visit(ThisExpr e) {
        // No need for action
    }

    @Override
    public void visit(NewIntArrayExpr e) {
        //TODO: Oz
    }

    @Override
    public void visit(NewObjectExpr e) {
        //TODO: Oz
    }

    @Override
    public void visit(NotExpr e) {
        e.e().accept(this);
    }

    @Override
    public void visit(IntAstType t) {
        // No need for action
    }

    @Override
    public void visit(BoolAstType t) {
        // No need for action
    }

    @Override
    public void visit(IntArrayAstType t) {
        // No need for action
    }

    @Override
    public void visit(RefType t) {
        // No need for action
    }
}
