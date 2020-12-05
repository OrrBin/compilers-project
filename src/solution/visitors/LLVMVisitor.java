package solution.visitors;

import ast.*;
import solution.AstNodeUtil;
import solution.MethodLLVMBuilder;
import solution.LLVMUtil;

import java.io.IOException;
import java.io.OutputStream;

import static solution.LLVMUtil.getTypeName;

public class LLVMVisitor implements Visitor {

    private OutputStream outputStream;
    private LLVMUtil llvmUtil;
    private AstNodeUtil astNodeUtil;
    private MethodLLVMBuilder methodBuilder = new MethodLLVMBuilder();
    private int registerCounter = 0;

    public LLVMVisitor(OutputStream outputStream, LLVMUtil llvmUtil, AstNodeUtil astNodeUtil) {
        this.outputStream = outputStream;
        this.llvmUtil = llvmUtil;
        this.astNodeUtil = astNodeUtil;
    }

    private String allocateRegister() {
        String registerName = "%_" + registerCounter;
        registerCounter += 1;
        return registerName;
    }

    private String getLastRegister() {
        return registerCounter > 0 ? "%_" + (registerCounter - 1) :  "%_"+0;
    }

    @Override
    public void visit(Program program) {
        for (var classDecl : program.classDecls()) {
            classDecl.accept(this);
        }
    }

    @Override
    public void visit(ClassDecl classDecl) {
        for (var methodDecl : classDecl.methoddecls()) {
            methodDecl.accept(this);
        }
    }

    @Override
    public void visit(MainClass mainClass) {

    }

    @Override
    public void visit(MethodDecl methodDecl) {

        // declaration
        String declaration = String.format("define %s @%s.%s(i8* %%this",
                getTypeName(methodDecl.returnType()),
                astNodeUtil.getClassDeclaration(methodDecl).name(),
                methodDecl.name());

        methodBuilder.appendDeclaration(declaration);
        for (FormalArg arg : methodDecl.formals()) {
            methodBuilder.appendDeclaration(", ");
            arg.accept(this);
        }
        methodBuilder.appendDeclaration(") {\n");

        // variables declarations
        for (VarDecl varDecl : methodDecl.vardecls()) {
            varDecl.accept(this);
        }

        //body
        for (Statement statement : methodDecl.body()) {
            statement.accept(this);
        }

        // return statement
        methodDecl.ret().accept(this); // TODO check how to decide which register
        String retType = getTypeName(methodDecl.returnType());
        String retRegister = getLastRegister();
        methodBuilder.appendBodyLine(String.format("ret %s %s", retType, retRegister));

        methodBuilder.appendBody("}\n");

        try {
            outputStream.write(methodBuilder.toString().getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }

        methodBuilder.clear();
        registerCounter = 0;
    }

    @Override
    public void visit(FormalArg formalArg) {
        //Example: i32 %.x
        methodBuilder.appendDeclaration(getTypeName(formalArg.type()))
                .appendDeclaration(String.format(" %%.%s", formalArg.name()));

        //Example:  %x = alloca i32
        //          store i32 %.x, i32* %x
        String register = allocateRegister();
        String type = getTypeName(formalArg.type());
        methodBuilder.appendBodyLine(String.format("%%%s = alloca %s", type, register));
        methodBuilder.appendBodyLine(String.format("store %s %%.%s, %s* %s", type, formalArg.name(), type, register));

    }

    @Override
    public void visit(VarDecl varDecl) {
        methodBuilder.appendBodyLine(String.format("%%%s = alloca %s", varDecl.name(), getTypeName(varDecl.type())));
    }

    @Override
    public void visit(BlockStatement blockStatement) {
        blockStatement.statements().forEach(statement -> statement.accept(this));
    }

    @Override
    public void visit(IfStatement ifStatement) {
        // TODO OZ
    }

    @Override
    public void visit(WhileStatement whileStatement) {
        // TODO OZ
    }

    @Override
    public void visit(SysoutStatement sysoutStatement) {
        // TODO OZ
    }

    @Override
    public void visit(AssignStatement assignStatement) {
        // TODO OZ
    }

    @Override
    public void visit(AssignArrayStatement assignArrayStatement) {
        // TODO OZ
    }

    @Override
    public void visit(AndExpr e) {
        // TODO OR
    }

    @Override
    public void visit(LtExpr e) {
        // TODO OR
    }

    @Override
    public void visit(AddExpr e) {
        // TODO OR
    }

    @Override
    public void visit(SubtractExpr e) {
        // TODO OR
    }

    @Override
    public void visit(MultExpr e) {
        // TODO OR
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
        methodBuilder.appendBodyLine(String.format("%s = add i32 %d, 0", allocateRegister(), e.num()));
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


    // TODO move the implementation of llvmUtil.toLLVM() into the Type visitors

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
