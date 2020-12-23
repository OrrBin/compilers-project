package solution.visitors;

import ast.*;
import solution.utils.AstNodeUtil;

import java.util.*;

public class InitializationCheckVisitor implements Visitor {

    private AstNodeUtil astNodeUtil;
    private Stack<Map<String,Boolean>> variablesStatusStack = new Stack<>();

    private boolean isLegal = true;
    private List<String> errors = new ArrayList<>();

    public InitializationCheckVisitor(AstNodeUtil astNodeUtil) {
        this.astNodeUtil = astNodeUtil;
        variablesStatusStack.push(new HashMap<>());
    }

    public boolean isLegal() {
        return isLegal;
    }

    public String getErrors() {
        String errors = "uninitialized local variables: ";
        this.errors.forEach(error -> errors.concat(error + ","));
        return errors;
    }


    // region PRIVATE METHODS

    private void setLegal(boolean legal) {
        isLegal = legal;
    }

    private void addError(String variableName){
        errors.add(variableName);
    }

    private void updateVariablesStatusAfterBranch() {
        for (String variable : getMainVariableStatusMap().keySet()) {
            var map1 = variablesStatusStack.pop();
            var map2 = variablesStatusStack.pop();
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
        program.mainClass().accept(this);
        program.classDecls().forEach(clazz -> clazz.accept(this));
    }

    @Override
    public void visit(ClassDecl classDecl) {
        classDecl.fields().forEach(field -> field.accept(this));
        classDecl.methoddecls().forEach(method -> method.accept(this));
    }

    @Override
    public void visit(MainClass mainClass) {
        mainClass.mainStatement().accept(this);
    }

    @Override
    public void visit(MethodDecl methodDecl) {
        methodDecl.vardecls().forEach((var -> var.accept(this)));
        methodDecl.body().forEach((statement -> statement.accept(this)));
        methodDecl.ret().accept(this);

        variablesStatusStack.pop();

        // for debug
        if (!variablesStatusStack.empty()){
            throw new RuntimeException("variables status stack should be empty - contact Oz");
        }
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
        if (astNodeUtil.isField(varDecl)){
            // field is always initialized
            variablesStatusStack.peek().put(varDecl.name(), true);
        } else {
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

//        boolean outerIf = this.branchStatuses.size() == 0;

        ifStatement.cond().accept(this);

        variablesStatusStack.push(cloneVariableStatusMap());
        ifStatement.thencase().accept(this);

        variablesStatusStack.push(cloneVariableStatusMap());
        ifStatement.elsecase().accept(this);

        updateVariablesStatusAfterBranch();
    }

    @Override
    public void visit(WhileStatement whileStatement) {
        // no need to implement
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
        var map = variablesStatusStack.peek();
        assignArrayStatement.rv().accept(this);
        map.put(assignArrayStatement.lv(), true);
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
        if (!map.get(id)){
            setLegal(false);
            addError(id);
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