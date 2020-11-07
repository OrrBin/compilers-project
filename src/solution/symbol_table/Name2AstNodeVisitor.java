package solution.symbol_table;
import ast.*;

import java.util.HashMap;
import java.util.Map;

public class Name2AstNodeVisitor implements Visitor{
    Map<String, AstNode> name2AstNode;

    public Name2AstNodeVisitor(){
        name2AstNode = new HashMap<>();
    }

    @Override
    public void visit(Program program) {
        for(ClassDecl classDecl: program.classDecls()){
            classDecl.accept(this);
        }
        MainClass mainClass = program.mainClass();
        mainClass.accept(this);

    }

    @Override
    public void visit(ClassDecl classDecl) {

        name2AstNode.put(classDecl.name(), classDecl);

        for(VarDecl field : classDecl.fields()){
            field.accept(this);
        }

        for(MethodDecl methodDecl: classDecl.methoddecls()){
            methodDecl.accept(this);
        }
    }

    @Override
    public void visit(MainClass mainClass) {
        name2AstNode.put(mainClass.name(), mainClass);
    }

    @Override
    public void visit(MethodDecl methodDecl) {
        name2AstNode.put(methodDecl.name(), methodDecl);

        for(FormalArg formalArg: methodDecl.formals()){
            formalArg.accept(this);
        }

        for(VarDecl local: methodDecl.vardecls()){
            local.accept(this);
        }

//        for(Statement statement: methodDecl.body()){
//            statement.accept(this);
//        }
    }

    @Override
    public void visit(FormalArg formalArg) {
        name2AstNode.put(formalArg.name(), formalArg);
    }

    @Override
    public void visit(VarDecl varDecl) {
        name2AstNode.put(varDecl.name(), varDecl);
    }

    @Override
    public void visit(BlockStatement blockStatement) {
    }

    @Override
    public void visit(IfStatement ifStatement) {

    }

    @Override
    public void visit(WhileStatement whileStatement) {

    }

    @Override
    public void visit(SysoutStatement sysoutStatement) {

    }

    @Override
    public void visit(AssignStatement assignStatement) {
    }

    @Override
    public void visit(AssignArrayStatement assignArrayStatement) {
    }

    @Override
    public void visit(AndExpr e) {
    }

    @Override
    public void visit(LtExpr e) {
    }

    @Override
    public void visit(AddExpr e) {
    }

    @Override
    public void visit(SubtractExpr e) {
    }

    @Override
    public void visit(MultExpr e) {
    }

    @Override
    public void visit(ArrayAccessExpr e) {
    }

    @Override
    public void visit(ArrayLengthExpr e) {
    }

    @Override
    public void visit(MethodCallExpr e) {
    }

    @Override
    public void visit(IntegerLiteralExpr e) {
    }

    @Override
    public void visit(TrueExpr e) {
    }

    @Override
    public void visit(FalseExpr e) {
    }

    @Override
    public void visit(IdentifierExpr e) {

    }

    @Override
    public void visit(ThisExpr e) {

    }

    @Override
    public void visit(NewIntArrayExpr e) {

    }

    @Override
    public void visit(NewObjectExpr e) {

    }

    @Override
    public void visit(NotExpr e) {

    }

    @Override
    public void visit(IntAstType t) {

    }

    @Override
    public void visit(BoolAstType t) {

    }

    @Override
    public void visit(IntArrayAstType t) {

    }

    @Override
    public void visit(RefType t) {

    }
}
