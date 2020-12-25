package solution.visitors;

import ast.*;
import solution.exceptions.SemanticException;
import solution.symbol_table.symbol_types.SymbolKeyType;
import solution.utils.AstNodeUtil;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class SemanticsCheckVisitor implements Visitor {

    private OutputStream outputStream;
    private AstNodeUtil util;
    private boolean isOk = true;
    private AstType lastType = new IntAstType();

    //region constants
    private static final String OK = "OK";
    private static final String ERROR = "ERROR";
    //endregion

    public SemanticsCheckVisitor(OutputStream outputStream, AstNodeUtil util){
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
        for(var clazz : classes){
            if(name2TimesSet.contains(clazz.name())) {
                isOk = false;
                write2File();
                return;
            }
            name2TimesSet.add(clazz.name());
        }

        // visitor calls
        program.mainClass().accept(this);

        for(var classDecl : classes) {
            classDecl.accept(this);
            if(!isOk) {return;}
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
        if(classHierarchy == null){
            isOk = false;
            write2File();
            return;
        }
        classDecl.methoddecls().forEach(method-> method.accept(this));
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
    }

    @Override
    public void visit(FormalArg formalArg) {

    }

    @Override
    public void visit(VarDecl varDecl) {

    }

    @Override
    public void visit(BlockStatement blockStatement) {

    }

    @Override
    public void visit(IfStatement ifStatement) {
        ifStatement.cond().accept(this);
        if(!(lastType instanceof BoolAstType)) {
            isOk = false;
            write2File();
        }

    }

    @Override
    public void visit(WhileStatement whileStatement) {
        whileStatement.cond().accept(this);
        if(!(lastType instanceof BoolAstType)) {
            isOk = false;
            write2File();
        }
    }

    @Override
    public void visit(SysoutStatement sysoutStatement) {
        sysoutStatement.arg().accept(this);

        if(!(lastType instanceof IntAstType)) {
            isOk = false;
            write2File();
        }
    }

    @Override
    public void visit(AssignStatement assignStatement) {

    }

    @Override
    public void visit(AssignArrayStatement assignArrayStatement) {

        // Check that lv is int[]
        VariableIntroduction var = (VariableIntroduction) util.getDeclFromName(SymbolKeyType.VAR, assignArrayStatement.lv(), assignArrayStatement);
        if(! (var.type() instanceof IntArrayAstType)) {
            isOk = false;
            write2File();
        }

        // Check that rv is int
        assignArrayStatement.rv().accept(this);
        if(! (lastType instanceof IntAstType)) {
            isOk = false;
            write2File();
        }

        // Check that index is int
        assignArrayStatement.index().accept(this);
        if(! (lastType instanceof IntAstType)) {
            isOk = false;
            write2File();
        }
    }

    @Override
    public void visit(AndExpr e) {
        e.e1().accept(this);
        if(! (lastType instanceof BoolAstType)) {
            isOk = false;
            write2File();
        }

        e.e2().accept(this);
        if(! (lastType instanceof BoolAstType)) {
            isOk = false;
            write2File();
        }

        lastType = new BoolAstType();
    }

    @Override
    public void visit(LtExpr e) {
        e.e1().accept(this);
        if(! (lastType instanceof IntAstType)) {
            isOk = false;
            write2File();
        }

        e.e2().accept(this);
        if(! (lastType instanceof IntAstType)) {
            isOk = false;
            write2File();
        }

        lastType = new BoolAstType();
    }

    @Override
    public void visit(AddExpr e) {
        e.e1().accept(this);
        if(! (lastType instanceof IntAstType)) {
            isOk = false;
            write2File();
        }

        e.e2().accept(this);
        if(! (lastType instanceof IntAstType)) {
            isOk = false;
            write2File();
        }

        lastType = new IntAstType();
    }

    @Override
    public void visit(SubtractExpr e) {
        e.e1().accept(this);
        if(! (lastType instanceof IntAstType)) {
            isOk = false;
            write2File();
        }

        e.e2().accept(this);
        if(! (lastType instanceof IntAstType)) {
            isOk = false;
            write2File();
        }

        lastType = new IntAstType();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
                                        //TODO: Male Orr
    @Override
    public void visit(MultExpr e) {
        e.e1().accept(this);
        if(! (lastType instanceof IntAstType)) {
            isOk = false;
            write2File();
        }

        e.e2().accept(this);
        if(! (lastType instanceof IntAstType)) {
            isOk = false;
            write2File();
        }

        lastType = new IntAstType();
    }

    @Override
    public void visit(ArrayAccessExpr e) {
        // Check that array expression is int[]
        e.arrayExpr().accept(this);
        if(! (lastType instanceof IntArrayAstType)) {
            isOk = false;
            write2File();
        }

        // Check that index is int
        e.indexExpr().accept(this);
        if(! (lastType instanceof IntAstType)) {
            isOk = false;
            write2File();
        }

        lastType = new IntAstType();
    }

    @Override
    public void visit(ArrayLengthExpr e) {
        e.arrayExpr().accept(this);
        if(!(lastType instanceof IntArrayAstType)) {
            isOk = false;
            write2File();
        }

        lastType = new IntAstType();
    }

    @Override
    public void visit(MethodCallExpr e) {

    }

    @Override
    public void visit(IntegerLiteralExpr e) {

    }

    @Override
    public void visit(TrueExpr e) {
        lastType = new BoolAstType();

    }

    @Override
    public void visit(FalseExpr e) {
        lastType = new BoolAstType();
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
        if(! (lastType instanceof IntAstType)) {
            isOk = false;
            write2File();
        }

        lastType = new IntArrayAstType();
    }

    @Override
    public void visit(NewObjectExpr e) {

    }

    @Override
    public void visit(NotExpr e) {
        e.e().accept(this);
        if(! (lastType instanceof BoolAstType)) {
            isOk = false;
            write2File();
        }

        lastType = new BoolAstType();
    }

    @Override
    public void visit(IntAstType t) {
        lastType = new IntAstType();
    }

    @Override
    public void visit(BoolAstType t) {
        lastType = new BoolAstType();
    }

    @Override
    public void visit(IntArrayAstType t) {
        lastType = new IntArrayAstType();
    }

    @Override
    public void visit(RefType t) {

    }

    // region private method

    private boolean hasDuplicates(List<String> names) {
        Set<String> namesSet = new HashSet<>(names);
        return namesSet.size() != names.size();
    }

    //endregion

}
