package solution.visitors;

import ast.*;
import solution.AstNodeUtil;
import solution.LLVMUtil;

import java.io.OutputStream;

public class LLVMVisitor implements Visitor {

    private OutputStream outputStream;
    private LLVMUtil llvmUtil;
    private AstNodeUtil astNodeUtil;
    private StringBuilder builder = new StringBuilder();

    private static int registerCounter = 0;

    public LLVMVisitor(OutputStream outputStream, LLVMUtil llvmUtil, AstNodeUtil astNodeUtil) {
        this.outputStream = outputStream;
        this.llvmUtil = llvmUtil;
        this.astNodeUtil = astNodeUtil;
    }

    public OutputStream getOutputStream() {
        return outputStream;
    }

    public StringBuilder getBuilder() {
        return builder;
    }

    private static String allocateRegister() {
        String registerName = "%_" + registerCounter;
        registerCounter += 1;
        return registerName;
    }

    @Override
    public void visit(Program program) {
    }

    @Override
    public void visit(ClassDecl classDecl) {

    }

    @Override
    public void visit(MainClass mainClass) {

    }

    @Override
    public void visit(MethodDecl methodDecl) {

        // declaration
        String declaration = String.format("define %s @%s.%s(", llvmUtil.toLLVM(methodDecl.returnType()), astNodeUtil.getClassDeclaration(methodDecl) , methodDecl.name());
        builder.append(declaration);
        for (FormalArg arg : methodDecl.formals()){
            arg.accept(this);
            builder.append(", ");
        }

        builder.deleteCharAt(builder.length()-1); // remove spare ", "
        builder.deleteCharAt(builder.length()-1);
        builder.append(") {\n");

        // variables declarations
        for (VarDecl varDecl : methodDecl.vardecls()){
            varDecl.accept(this);
            builder.append("\n");
        }

        //  body
        for (Statement statement : methodDecl.body()) {
            statement.accept(this);
            builder.append("\n");
        }
        builder.deleteCharAt(builder.length()-1);

        // return statement
        builder.append("ret ");
        methodDecl.ret().accept(this); // TODO check how to decide which register

        builder.append("\n}");
    }

    @Override
    public void visit(FormalArg formalArg) {
        // EXAMPLE: store i32 %.x, i32* %x
        String register = allocateRegister();
        String type = llvmUtil.toLLVM(formalArg.type());
        builder.append(String.format("%%s = alloca %s\n", type, register));
        builder.append(String.format("store %s %%s, %s* %s\n", type, formalArg.name(), type, register));

    }

    @Override
    public void visit(VarDecl varDecl) {
        builder.append(String.format("%%s = alloca %s\n",llvmUtil.toLLVM(varDecl.type())));
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
