package solution.visitors;

import ast.AddExpr;
import ast.AndExpr;
import ast.ArrayAccessExpr;
import ast.ArrayLengthExpr;
import ast.AssignArrayStatement;
import ast.AssignStatement;
import ast.BlockStatement;
import ast.BoolAstType;
import ast.ClassDecl;
import ast.FalseExpr;
import ast.FormalArg;
import ast.IdentifierExpr;
import ast.IfStatement;
import ast.IntArrayAstType;
import ast.IntAstType;
import ast.IntegerLiteralExpr;
import ast.LtExpr;
import ast.MainClass;
import ast.MethodCallExpr;
import ast.MultExpr;
import ast.NewIntArrayExpr;
import ast.NewObjectExpr;
import ast.NotExpr;
import ast.Program;
import ast.RefType;
import ast.SubtractExpr;
import ast.SysoutStatement;
import ast.ThisExpr;
import ast.TrueExpr;
import ast.VarDecl;
import ast.Visitor;
import ast.WhileStatement;
import solution.RenameOpParams;
import solution.actions.AssignArrayLvRenameOp;
import solution.actions.AssignmentLvRenameOp;
import solution.actions.IdentifierRenameOp;
import solution.actions.RenameOp;
import solution.actions.VarDeclRenameOp;

import java.util.List;

public abstract class RenameVariableVisitor implements Visitor {
    protected RenameOpParams op;
    protected List<RenameOp<?>> renameOps;

    public RenameVariableVisitor(RenameOpParams op, List<RenameOp<?>> renameOps) {
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
    public void visit(FormalArg formalArg) {
        // No need for action
    }

    @Override
    public void visit(VarDecl varDecl) {
        if (varDecl.name().equals(op.originalName)) {
            renameOps.add(new VarDeclRenameOp(op, varDecl));
        }
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
        var lv = assignStatement.lv();
        if (lv.equals(op.originalName)) {
            renameOps.add(new AssignmentLvRenameOp(op, assignStatement));
        }

        var rv = assignStatement.rv();
        rv.accept(this);
    }

    @Override
    public void visit(AssignArrayStatement assignArrayStatement) {
        assignArrayStatement.index().accept(this);

        var lv = assignArrayStatement.lv();
        if (lv.equals(op.originalName)) {
            renameOps.add(new AssignArrayLvRenameOp(op, assignArrayStatement));
        }

        assignArrayStatement.rv().accept(this);
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
        e.arrayExpr().accept(this);
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
            renameOps.add(new IdentifierRenameOp(op, e));
    }

    @Override
    public void visit(ThisExpr e) {
        // No need for action
    }

    @Override
    public void visit(NewIntArrayExpr e) {
        e.lengthExpr().accept(this);
    }

    @Override
    public void visit(NewObjectExpr e) {
        // No need for action
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
