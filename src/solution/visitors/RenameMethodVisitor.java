package solution.visitors;

import ast.*;
import solution.AstNodeUtil;
import solution.RenameOpParams;
import solution.actions.MethodCallRenameOp;
import solution.actions.MethodDeclRenameOp;
import solution.actions.RenameOp;
import solution.symbol_table.symbol_table_types.SymbolTable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

//TODO: Oz
public class RenameMethodVisitor implements Visitor {

    private RenameOpParams op;
    private List<RenameOp<?>> renameOps;
    //private ClassDecl methodClassScope;
    private AstNodeUtil util;
    private Set<ClassDecl> family;

    public RenameMethodVisitor(RenameOpParams op,MethodDecl method, List<RenameOp<?>> renameOps, AstNodeUtil util) {
        this.op = op;
        this.renameOps = renameOps;
        //this.methodClassScope = scope;
        this.util = util;
        this.family = new HashSet<>();
        family.addAll(util.getFamilyOfMethod(method));
    }

    @Override
    public void visit(Program program) {
        program.classDecls().forEach(clazz -> clazz.accept(this));
    }

    @Override
    public void visit(ClassDecl classDecl) {
        classDecl.methoddecls().forEach(method -> method.accept(this));
    }

    @Override
    public void visit(MainClass mainClass) {
        mainClass.mainStatement().accept(this);
    }

    @Override
    public void visit(MethodDecl methodDecl) {
        if(family.contains(util.getClassDeclaration(methodDecl)) && methodDecl.name().equals(op.originalName)) {
            renameOps.add(new MethodDeclRenameOp(op, methodDecl));
        }

        methodDecl.body().forEach(statement -> statement.accept(this));
        methodDecl.ret().accept(this);
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
        SymbolTable table = util.getEnclosingScope(e);
        //if (table==null) return;
        ClassDecl scope = (ClassDecl) table.symbolTableScope;
        if(family.contains(scope) && e.methodId().equals(op.originalName)) {
            renameOps.add(new MethodCallRenameOp(op, e));
        }
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
