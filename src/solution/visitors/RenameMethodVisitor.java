package solution.visitors;

import ast.*;
import solution.RenameOpParams;
import solution.actions.MethodCallRenameOp;
import solution.actions.MethodDeclRenameOp;
import solution.actions.RenameOp;

import java.util.List;

//TODO: Oz
public class RenameMethodVisitor implements Visitor {

    protected RenameOpParams op;
    protected List<RenameOp<?>> renameOps;

    public RenameMethodVisitor(RenameOpParams op, List<RenameOp<?>> renameOps) {
        this.op = op;
        this.renameOps = renameOps;
    }

    @Override
    public void visit(Program program) {
        // No need to implement
    }

    @Override
    public void visit(ClassDecl classDecl) {
        classDecl.methoddecls().forEach(method -> method.accept(this));
    }

    @Override
    public void visit(MainClass mainClass) {
        // No need to implement
    }

    @Override
    public void visit(MethodDecl methodDecl) {
        renameOps.add(new MethodDeclRenameOp(op,methodDecl));
    }

    @Override
    public void visit(FormalArg formalArg) {
        // No need to implement
    }

    @Override
    public void visit(VarDecl varDecl) {
        // No need to implement
    }

    @Override
    public void visit(BlockStatement blockStatement) {
        blockStatement.statements().forEach((statement -> statement.accept(this)));
    }

    @Override
    public void visit(IfStatement ifStatement) {
        ifStatement.cond().accept(this);
        ifStatement.thencase().accept(this);
        ifStatement.elsecase().accept(this);
    }

    @Override
    public void visit(WhileStatement whileStatement) {
        whileStatement.cond().accept(this);
        whileStatement.body().accept(this);
    }

    @Override
    public void visit(SysoutStatement sysoutStatement) {
        sysoutStatement.arg().accept(this);
    }


    @Override
    public void visit(AssignStatement assignStatement) {
        var rv = assignStatement.rv();
        rv.accept(this);
    }

    @Override
    public void visit(AssignArrayStatement assignArrayStatement) {
        var rv = assignArrayStatement.rv();
        rv.accept(this);
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
        e.arrayExpr().accept(this);
        e.indexExpr().accept(this);
    }

    @Override
    public void visit(ArrayLengthExpr e) {
        // No need to implement
    }

    @Override
    public void visit(MethodCallExpr e) {
        renameOps.add(new MethodCallRenameOp(op,e));
    }

    @Override
    public void visit(IntegerLiteralExpr e) {
        // No need to implement
    }

    @Override
    public void visit(TrueExpr e) {
        // No need to implement
    }

    @Override
    public void visit(FalseExpr e) {
        // No need to implement
    }

    @Override
    public void visit(IdentifierExpr e) {
        // No need to implement
    }

    @Override
    public void visit(ThisExpr e) {
        // No need to implement
    }

    @Override
    public void visit(NewIntArrayExpr e) {
        // No need to implement
    }

    @Override
    public void visit(NewObjectExpr e) {
        // No need to implement
    }

    @Override
    public void visit(NotExpr e) {
        e.e().accept(this);
    }

    @Override
    public void visit(IntAstType t) {
        // No need to implement
    }

    @Override
    public void visit(BoolAstType t) {
        // No need to implement
    }

    @Override
    public void visit(IntArrayAstType t) {
        // No need to implement
    }

    @Override
    public void visit(RefType t) {
        // No need to implement
    }
}
