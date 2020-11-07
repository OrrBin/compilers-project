package solution.visitors;

import ast.*;
import solution.AstNodeUtil;
import solution.RenameOp;

import java.util.List;

//TODO: Oz
public class RenameFieldVisitor implements Visitor {

    private AstNodeUtil astNodeUtil;
    private RenameOp op;

    public RenameFieldVisitor(RenameOp op) {
        this.op = op;
    }

    public RenameFieldVisitor(AstNodeUtil astNodeUtil) {
        this.astNodeUtil = astNodeUtil;
    }

    @Override
    public void visit(Program program) {
        // No need for action
    }

    @Override
    public void visit(ClassDecl classDecl) {

        // visit here after visiting the relevant field,
        // therefore the field name was already changed.
        // need to visit:
        // 1. method declarations
        // 2. extending classes

        for (MethodDecl methodDecl: classDecl.methoddecls()) {
            methodDecl.accept(this);
        }
        List<ClassDecl> classes = astNodeUtil.getExtendingClasses(classDecl);
        for (ClassDecl extendingClass : classes) {
            extendingClass.accept(this);
        }
    }

    @Override
    public void visit(MainClass mainClass) {
        // No need for action
    }

    @Override
    public void visit(MethodDecl methodDecl) {
       methodDecl.body().forEach(statement -> statement.accept(this));
       methodDecl.ret().accept(this);
    }

    @Override
    public void visit(FormalArg formalArg) {
        // No need for action
    }

    @Override
    public void visit(VarDecl varDecl) {
        if (varDecl.name().equals(op.originalName) && astNodeUtil.isField(varDecl)){
            varDecl.setName(op.newName);
            astNodeUtil.getClassDeclaration(varDecl).accept(this);
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
        boolean isLocal = false;
        var lv = assignStatement.lv();
        if (lv.equals(op.originalName)) {
            MethodDecl method = astNodeUtil.getMethod(assignStatement);
            for (VarDecl var : method.vardecls()) {
                if (var.name().equals(op.originalName)) {
                    /* enclosing method contains local variable with same name,
                      since var declarations are at the beginning of the method - the local var overrides the field */
                    isLocal = true;
                }
            }
        }

        if (!isLocal) assignStatement.setLv(op.newName);
        var rv = assignStatement.rv();
        rv.accept(this);
    }

    @Override
    public void visit(AssignArrayStatement assignArrayStatement) {
        boolean isLocal = false;
        var lv = assignArrayStatement.lv();
        if (lv.equals(op.originalName)) {
            MethodDecl method = astNodeUtil.getMethod(assignArrayStatement);
            for (VarDecl var : method.vardecls()) {
                if (var.name().equals(op.originalName)) {
                    /* enclosing method contains local variable with same name,
                      since var declarations are at the beginning of the method - the local var overrides the field */
                    isLocal = true;
                }
            }
        }

        if (!isLocal) assignArrayStatement.setLv(op.newName);
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
        e.arrayExpr().accept(this); // TODO not sure
    }

    @Override
    public void visit(ArrayLengthExpr e) {
        e.arrayExpr().accept(this); // TODO not sure
    }

    @Override
    public void visit(MethodCallExpr e) {
        // actuals are the parameters for the method arguments
        e.actuals().forEach(actual -> actual.accept(this));
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
        if (e.id().equals(op.originalName) && astNodeUtil.isField(e)) {
            e.setId(op.newName);
        }
    }

    @Override
    public void visit(ThisExpr e) {
        // No need for action
    }

    @Override
    public void visit(NewIntArrayExpr e) {
        // No need for action
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
        // only classes - No need for action
    }
}
