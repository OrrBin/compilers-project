package solution.visitors;

import ast.*;
import solution.utils.AstNodeUtil;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SemanticsCheckVisitor implements Visitor {

    OutputStream outputStream;
    AstNodeUtil util;
    boolean isOk = true;
    AstType lastType = new IntAstType();

    //region constants
    String OK = "OK";
    String ERROR = "ERROR";
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

    ////////////////////////////////////////////////////////////////////////////////////////////////
                                        //TODO: Male Orr
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
