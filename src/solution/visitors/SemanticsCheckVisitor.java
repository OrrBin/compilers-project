package solution.visitors;

import ast.*;
import solution.exceptions.SemanticException;
import solution.symbol_table.symbol_table_types.SymbolTable;
import solution.symbol_table.symbol_types.SymbolKeyType;
import solution.utils.AstNodeUtil;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class SemanticsCheckVisitor implements Visitor {

    private OutputStream outputStream;
    private AstNodeUtil util;
    private boolean isOk = true;
    private AstType lastType = new IntAstType();
    String lastClassName = null;

    //region constants
    private static final String OK = "OK";
    private static final String ERROR = "ERROR";
    //endregion

    public SemanticsCheckVisitor(OutputStream outputStream, AstNodeUtil util) {
        this.outputStream = outputStream;
        this.util = util;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //TODO: Female Or

    @Override
    public void visit(Program program) {

        // need2Check - #3 (no two classes with the same name)
        Set<String> name2TimesSet = new HashSet<>();
        var classes = program.classDecls();
        for (var clazz : classes) {
            if (name2TimesSet.contains(clazz.name())) {
                isOk = false;
                write2File();
                return;
            }
            name2TimesSet.add(clazz.name());
        }

        // visitor calls
        program.mainClass().accept(this);

        for (var classDecl : classes) {
            classDecl.accept(this);
            if (!isOk) {
                return;
            }
        }
        write2File();

    }

    private void write2File() {
        try {
            if (isOk) {
                outputStream.write(OK.getBytes());
            } else {
                outputStream.write(ERROR.getBytes());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void visit(ClassDecl classDecl) {
        // need2Check - #1 (a class doesn't inherit itself)
        var classHierarchy = util.getClassHierarchy(classDecl);
        if (classHierarchy == null) {
            isOk = false;
            write2File();
            return;
        }
        classDecl.methoddecls().forEach(method -> method.accept(this));
    }

    @Override
    public void visit(MainClass mainClass) {

    }

    @Override
    public void visit(MethodDecl methodDecl) {
        List<String> localNames = methodDecl.vardecls().stream().map(VariableIntroduction::name).collect(Collectors.toList());
        if (hasDuplicates(localNames)){
            throw new SemanticException("Found redeclaration of a local variable");
        }

        List<String> formalNames = methodDecl.formals().stream().map(VariableIntroduction::name).collect(Collectors.toList());
        if (hasDuplicates(formalNames)){
            throw new SemanticException("Found redeclaration of a formal variable");
        }

        List<String> localAndFormals = new ArrayList<>();
        localAndFormals.addAll(localNames);
        localAndFormals.addAll(formalNames);
        if (hasDuplicates(localAndFormals)){
            throw new SemanticException("Found local and formal with the same name");
        }

        // Check 18
        methodDecl.ret().accept(this);

        // If returned expression is not RefType check that it has the same type as method return type
        if (!(lastType instanceof RefType)) {
            if (!methodDecl.returnType().getClass().equals(lastType.getClass())) {
                throw new SemanticException("Returned expression type and method return type are not the same");
            }
        } else {
            // If returned expression is RefType , check that method return type is refType and of super class
            if (!(methodDecl.returnType() instanceof RefType)) {
                throw new SemanticException("Returned expression type and method return type are not the same");
            }

            RefType varType = (RefType) (methodDecl.returnType());
            if (!util.isSubClass(varType.id(), lastClassName)) {
                throw new SemanticException("MethodDecl return expression type and method return type are refType but don't have the correct extending class. return expression type class: " + lastClassName + " , method return type: " + varType.id() );
            }
        }


    }

    @Override
    public void visit(FormalArg formalArg) {

    }

    @Override
    public void visit(VarDecl varDecl) {

    }

    @Override
    public void visit(BlockStatement blockStatement) {
        blockStatement.statements().forEach(statement -> statement.accept(this));
    }

    @Override
    public void visit(IfStatement ifStatement) {
        ifStatement.cond().accept(this);
        if (!(lastType instanceof BoolAstType)) {
            throw new SemanticException("If condition expression is not of type boolean");
        }

    }

    @Override
    public void visit(WhileStatement whileStatement) {
        whileStatement.cond().accept(this);
        if (!(lastType instanceof BoolAstType)) {
            throw new SemanticException("While condition expression is not of type boolean");

        }
    }

    @Override
    public void visit(SysoutStatement sysoutStatement) {
        sysoutStatement.arg().accept(this);

        if (!(lastType instanceof IntAstType)) {
            throw new SemanticException("Sysout arg is not of type int");

        }
    }

    @Override
    public void visit(AssignStatement assignStatement) {

        // Check 16
        assignStatement.rv().accept(this);
        VariableIntroduction var = (VariableIntroduction) util.getDeclFromName(SymbolKeyType.VAR, assignStatement.lv(), assignStatement);

        // If last type is not refType check that that lv is of the same type
        if (!(lastType instanceof RefType)) {
            if (!lastType.getClass().equals(var.type().getClass())) {
                throw new SemanticException("Assignment statement lv and rv are not of the same type");
            }
        } else {
            // If last type is RefType , check that lv is refType and of super class
            if (!(var.type() instanceof RefType)) {
                throw new SemanticException("Assignment statement lv and rv are not of the same type");
            }

            RefType varType = (RefType) (var.type());
            if (!util.isSubClass(varType.id(), lastClassName)) {
                throw new SemanticException("AssignStatement lv,rv are refType but don't have the correct extending class. rv class: " + lastClassName + " , lv: " + varType.id() );
            }
        }
    }

    @Override
    public void visit(AssignArrayStatement assignArrayStatement) {

        // Check that lv is int[]
        VariableIntroduction var = (VariableIntroduction) util.getDeclFromName(SymbolKeyType.VAR, assignArrayStatement.lv(), assignArrayStatement);
        if (!(var.type() instanceof IntArrayAstType)) {
            throw new SemanticException("Array Assignment statement lv is not of type IntArray");
        }

        // Check that rv is int
        assignArrayStatement.rv().accept(this);
        if (!(lastType instanceof IntAstType)) {
            throw new SemanticException("Array Assignment statement rv is not of type IntArray");
        }

        // Check that index is int
        assignArrayStatement.index().accept(this);
        if (!(lastType instanceof IntAstType)) {
            throw new SemanticException("Array Assignment statement index is not of type int");

        }
    }

    @Override
    public void visit(AndExpr e) {
        e.e1().accept(this);
        if (!(lastType instanceof BoolAstType)) {
            throw new SemanticException("And statement e1 is not of type boolean");
        }

        e.e2().accept(this);
        if (!(lastType instanceof BoolAstType)) {
            throw new SemanticException("And statement e2 is not of type boolean");
        }

        lastType = new BoolAstType();
    }

    @Override
    public void visit(LtExpr e) {
        e.e1().accept(this);
        if (!(lastType instanceof IntAstType)) {
            throw new SemanticException("Lt statement e1 is not of type int");
        }

        e.e2().accept(this);
        if (!(lastType instanceof IntAstType)) {
            throw new SemanticException("Lt statement e2 is not of type int");
        }

        lastType = new BoolAstType();
    }

    @Override
    public void visit(AddExpr e) {
        e.e1().accept(this);
        if (!(lastType instanceof IntAstType)) {
            throw new SemanticException("Add statement e1 is not of type int");
        }

        e.e2().accept(this);
        if (!(lastType instanceof IntAstType)) {
            throw new SemanticException("Add statement e2 is not of type int");
        }

        lastType = new IntAstType();
    }

    @Override
    public void visit(SubtractExpr e) {
        e.e1().accept(this);
        if (!(lastType instanceof IntAstType)) {
            throw new SemanticException("Subtract statement e1 is not of type int");
        }

        e.e2().accept(this);
        if (!(lastType instanceof IntAstType)) {
            throw new SemanticException("Subtract statement e2 is not of type int");
        }

        lastType = new IntAstType();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //TODO: Male Orr
    @Override
    public void visit(MultExpr e) {
        e.e1().accept(this);
        if (!(lastType instanceof IntAstType)) {
            throw new SemanticException("Mult statement e1 is not of type int");
        }

        e.e2().accept(this);
        if (!(lastType instanceof IntAstType)) {
            throw new SemanticException("Mult statement e2 is not of type int");
        }

        lastType = new IntAstType();
    }

    @Override
    public void visit(ArrayAccessExpr e) {
        // Check that array expression is int[]
        e.arrayExpr().accept(this);
        if (!(lastType instanceof IntArrayAstType)) {
            throw new SemanticException("ArrayAccessExpr array expression is not of type int[]");
        }

        // Check that index is int
        e.indexExpr().accept(this);
        if (!(lastType instanceof IntAstType)) {
            throw new SemanticException("ArrayAccessExpr index expression is not of type int");
        }

        lastType = new IntAstType();
    }

    @Override
    public void visit(ArrayLengthExpr e) {
        e.arrayExpr().accept(this);
        if (!(lastType instanceof IntArrayAstType)) {
            throw new SemanticException("ArrayLengthExpr array expression is not of type int[]");
        }

        lastType = new IntAstType();
    }

    @Override
    public void visit(MethodCallExpr e) {

        // Check 12: owner expression is either this, new or identifier expression
        if (!(e.ownerExpr() instanceof ThisExpr) &&
                !(e.ownerExpr() instanceof ThisExpr) &&
                !(e.ownerExpr() instanceof IdentifierExpr)
        ) {
            throw new SemanticException("MethodCallExpr owner expression is neither this, new or identifier expression, it is: " + e.ownerExpr().getClass());
        }

        // Check 10: The static type of the object is reference type
        e.ownerExpr().accept(this);
        if (!(lastType instanceof RefType)) {
            throw new SemanticException("MethodCallExpr owner expression is static type is not RefType, but it is: " + lastType.getClass());
        }

        SymbolTable scope = util.getEnclosingScope(e);
        ClassDecl clazz = (ClassDecl) scope.symbolTableScope;

        Optional<MethodDecl> declOptional = clazz.methoddecls().stream().filter(methodDecl -> methodDecl.name().equals(e.methodId())).findFirst();
        // Check 11.a: If no method with given name exist in the owner type, then there is an error
        if (declOptional.isEmpty()) {
            throw new SemanticException("MethodCallExpr Could not find method with given name in owner type. owner type: " + clazz.name() + " , method name: " + e.methodId());
        }

        MethodDecl methodDecl = declOptional.get();
        // Check 11.b: The methodDecl and method call has same number of parameters
        if (methodDecl.formals().size() != e.actuals().size()) {
            throw new SemanticException("MethodCallExpr The methodDecl and method call doesn't have same number of parameters");
        }

        // Check 11.c: each formalArg and actual have corresponding type
        int numOfParams = e.actuals().size();
        for (int i = 0; i < numOfParams; i++) {
            var actual = e.actuals().get(i);
            var param = methodDecl.formals().get(i);
            var paramType = param.type();
            actual.accept(this);
            if (!(lastType instanceof RefType)) {
                if (!lastType.getClass().equals(paramType.getClass())) {
                    throw new SemanticException("MethodCallExpr The " + i + "th parameter doesn't have the correct type. actual type: " + lastType.getClass() + " , paramType: " + paramType.getClass() );
                }
            } else {
                if (!(paramType instanceof RefType)) {
                    throw new SemanticException("MethodCallExpr The " + i + "th parameter doesn't have the correct type. actual type: " + lastType.getClass() + " , paramType: " + paramType.getClass() );
                }

                RefType paramRefType = (RefType) paramType;
                if (!util.isSubClass(paramRefType.id(), lastClassName)) {
                    throw new SemanticException("MethodCallExpr The " + i + "th parameter is reftype but doesn't have the correct extending class. actual class: " + lastClassName + " , param class: " + paramRefType.id() );
                }
            }

            lastType = methodDecl.returnType();
            lastClassName = lastType instanceof RefType ? ((RefType) lastType).id() : null;
        }
    }

    @Override
    public void visit(IntegerLiteralExpr e) {
        lastType = new IntAstType();
        lastClassName = null;
    }

    @Override
    public void visit(TrueExpr e) {
        lastType = new BoolAstType();
        lastClassName = null;

    }

    @Override
    public void visit(FalseExpr e) {
        lastType = new BoolAstType();
        lastClassName = null;
    }

    @Override
    public void visit(IdentifierExpr e) {

    }

    @Override
    public void visit(ThisExpr e) {

    }

    @Override
    public void visit(NewIntArrayExpr e) {
        e.lengthExpr().accept(this);
        if (!(lastType instanceof IntAstType)) {
            throw new SemanticException("NewIntArray length expression is not int, but: " + lastType.getClass());
        }

        lastType = new IntArrayAstType();
        lastClassName = null;
    }

    @Override
    public void visit(NewObjectExpr e) {

    }

    @Override
    public void visit(NotExpr e) {
        e.e().accept(this);
        if (!(lastType instanceof BoolAstType)) {
            throw new SemanticException("Not statement e is not of type boolean");

        }

        lastType = new BoolAstType();
        lastClassName = null;
    }

    @Override
    public void visit(IntAstType t) {
        lastType = new IntAstType();
        lastClassName = null;
    }

    @Override
    public void visit(BoolAstType t) {
        lastType = new BoolAstType();
        lastClassName = null;
    }

    @Override
    public void visit(IntArrayAstType t) {
        lastType = new IntArrayAstType();
        lastClassName = null;
    }

    @Override
    public void visit(RefType t) {
        lastType = new RefType();
        lastClassName = t.id();
    }

    // region private method

    private boolean hasDuplicates(List<String> names) {
        Set<String> namesSet = new HashSet<>(names);
        return namesSet.size() != names.size();
    }

    //endregion

}
