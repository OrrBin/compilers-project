package solution.visitors;

import ast.*;
import solution.exceptions.InitializationException;
import solution.utils.AstNodeUtil;

import java.util.*;

public class InitializationCheckVisitor implements Visitor {

    private AstNodeUtil astNodeUtil;
    private Stack<Map<String,Boolean>> variablesStatusStack = new Stack<>();


    public InitializationCheckVisitor(AstNodeUtil astNodeUtil) {
        this.astNodeUtil = astNodeUtil;
    }

    public String createErrorMsg(String variable) {
        return "Found uninitialized local variable: " + variable;
    }


    // region PRIVATE METHODS


    private void updateVariablesStatusAfterBranch() {
        var map1 = variablesStatusStack.pop();
        var map2 = variablesStatusStack.pop();
        for (String variable : getMainVariableStatusMap().keySet()) {
            if (map1.get(variable) && map2.get(variable)) {
                variablesStatusStack.peek().put(variable, true);
            }
        }
    }

    private Map<String,Boolean> cloneVariableStatusMap() {
        Map<String,Boolean> newMap = new HashMap<>();
        newMap.putAll(getMainVariableStatusMap());
        return newMap;
    }

    // endregion

    @Override
    public void visit(Program program) {
        // program.mainClass().accept(this);
        program.classDecls().forEach(clazz -> clazz.accept(this));
    }

    @Override
    public void visit(ClassDecl classDecl) {
        // classDecl.fields().forEach(field -> field.accept(this));  fields are always initialized
        classDecl.methoddecls().forEach(method -> method.accept(this));
        variablesStatusStack.clear();
    }

    @Override
    public void visit(MainClass mainClass) {
        // mainClass.mainStatement().accept(this);
    }

    @Override
    public void visit(MethodDecl methodDecl) {
        variablesStatusStack.push(new HashMap<>());
        methodDecl.vardecls().forEach((var -> var.accept(this)));
        methodDecl.body().forEach((statement -> statement.accept(this)));
        methodDecl.ret().accept(this);
        variablesStatusStack.pop();
    }

    @Override
    public void visit(FormalArg formalArg) {
        getMainVariableStatusMap().put(formalArg.name(), true);
    }

    private Map<String, Boolean> getMainVariableStatusMap() {
        return variablesStatusStack.get(0);
    }

    @Override
    public void visit(VarDecl varDecl) {
        // in MiniJava can't initialize variable in declaration
        if (!astNodeUtil.isField(varDecl)){
            // field is always initialized
            // local variable is never initialized in declaration
            variablesStatusStack.peek().put(varDecl.name(), false);
        }
    }

    @Override
    public void visit(BlockStatement blockStatement) {
        blockStatement.statements().forEach(statement -> statement.accept(this));
    }

    @Override
    public void visit(IfStatement ifStatement) {

        ifStatement.cond().accept(this);

        variablesStatusStack.push(cloneVariableStatusMap());
        ifStatement.thencase().accept(this);

        variablesStatusStack.push(cloneVariableStatusMap());
        ifStatement.elsecase().accept(this);

        updateVariablesStatusAfterBranch();
    }

    @Override
    public void visit(WhileStatement whileStatement) {
        whileStatement.cond().accept(this);
//        Map<String, Boolean> clone = new HashMap<>(variablesStatusStack.peek());
//        variablesStatusStack.push(clone);
        whileStatement.body().accept(this);
//        variablesStatusStack.pop();
    }

    @Override
    public void visit(SysoutStatement sysoutStatement) {
        sysoutStatement.arg().accept(this);
    }

    @Override
    public void visit(AssignStatement assignStatement){
        var map = variablesStatusStack.peek();
        assignStatement.rv().accept(this);
        map.put(assignStatement.lv(), true);
    }

    @Override
    public void visit(AssignArrayStatement assignArrayStatement) {
        var map  = variablesStatusStack.peek();
        if (map.containsKey(assignArrayStatement.lv()) && !map.get(assignArrayStatement.lv())) {
            throw new InitializationException(createErrorMsg(assignArrayStatement.lv()));
        }
        assignArrayStatement.index().accept(this);
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
        e.ownerExpr().accept(this);
        e.actuals().forEach(actual -> actual.accept(this));
    }

    @Override
    public void visit(IntegerLiteralExpr e) {
        // no need to implement
    }

    @Override
    public void visit(TrueExpr e) {
        // no need to implement
    }

    @Override
    public void visit(FalseExpr e) {
        // no need to implement
    }

    @Override
    public void visit(IdentifierExpr e) {
        var map = variablesStatusStack.peek();
        var id = e.id();
        if (map.containsKey(id) && !map.get(id)){
            throw new InitializationException(createErrorMsg(id));
        }
    }

    @Override
    public void visit(ThisExpr e) {
        // no need to implement
    }

    @Override
    public void visit(NewIntArrayExpr e) {
        // no need to implement
    }

    @Override
    public void visit(NewObjectExpr e) {
        // no need to implement
    }

    @Override
    public void visit(NotExpr e) {
        e.e().accept(this);
    }

    @Override
    public void visit(IntAstType t) {
        // no need to implement
    }

    @Override
    public void visit(BoolAstType t) {
        // no need to implement
    }

    @Override
    public void visit(IntArrayAstType t) {
        // no need to implement
    }

    @Override
    public void visit(RefType t) {
        // no need to implement
    }

}
