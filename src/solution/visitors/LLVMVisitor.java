package solution.visitors;

import ast.*;
import solution.llvm_builders.MethodLLVMBuilder;
import solution.utils.AstNodeUtil;
import solution.utils.LLVMUtil;
import solution.utils.LabelCounter;
import solution.utils.RegisterCounter;

import java.io.IOException;
import java.io.OutputStream;

import static solution.utils.LLVMUtil.getTypeName;

public class LLVMVisitor implements Visitor {

    private OutputStream outputStream;
    private LLVMUtil llvmUtil;
    private AstNodeUtil astNodeUtil;
    private MethodLLVMBuilder methodBuilder = new MethodLLVMBuilder();
    private RegisterCounter registerCounter = new RegisterCounter();
    private LabelCounter labelCounter = new LabelCounter();

    public LLVMVisitor(OutputStream outputStream, LLVMUtil llvmUtil, AstNodeUtil astNodeUtil) {
        this.outputStream = outputStream;
        this.llvmUtil = llvmUtil;
        this.astNodeUtil = astNodeUtil;
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
        String retRegister = registerCounter.getLastRegister();
        methodBuilder.appendBodyLine(String.format("ret %s %s", retType, retRegister));

        methodBuilder.appendBody("}\n");

        try {
            outputStream.write(methodBuilder.toString().getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }

        // clean up
        methodBuilder.clear();
        registerCounter.resetRegisterCounter();
        labelCounter.resetRegisterCounter();;
    }

    @Override
    public void visit(FormalArg formalArg) {
        //Example: i32 %.x
        String name = formalArg.name();
        methodBuilder.appendDeclaration(getTypeName(formalArg.type()))
                .appendDeclaration(String.format(" %%.%s", name));

        //Example:  %x = alloca i32
        //          store i32 %.x, i32* %x
        String type = getTypeName(formalArg.type());
        methodBuilder.appendBodyLine(llvmUtil.alloca(name, type));
        methodBuilder.appendBodyLine(String.format("store %s %%.%s, %s* %s", type, name, type, name));

    }

    @Override
    public void visit(VarDecl varDecl) {
        methodBuilder.appendBodyLine(llvmUtil.alloca(varDecl.name(), getTypeName(varDecl.type())));
    }

    @Override
    public void visit(BlockStatement blockStatement) {
        blockStatement.statements().forEach(statement -> statement.accept(this));
    }

    @Override
    public void visit(IfStatement ifStatement) {
        String register = registerCounter.allocateRegister();
        String if0 = "if" + labelCounter.allocateLabelNumber();
        String if1 = "if" + labelCounter.allocateLabelNumber();
        String if2 = "if" + labelCounter.allocateLabelNumber();


        // condition
        methodBuilder.appendPartialBodyLine(String.format("%s = ", register));
        ifStatement.cond().accept(this);
        methodBuilder.appendBodyLine(llvmUtil.br(register, if0, if1));

        // labels
        methodBuilder.appendLabel(if0);
        ifStatement.thencase().accept(this);
        methodBuilder.appendBodyLine(llvmUtil.br(if2));
        methodBuilder.appendLabel(if1);
        ifStatement.elsecase().accept(this);
        methodBuilder.appendBodyLine(llvmUtil.br(if2));
        methodBuilder.appendLabel(if2);

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
        e.e1().accept(this);
        e.e2().accept(this);

        String e1Register = registerCounter.getRegister(2);
        String e2Register = registerCounter.getRegister(1);
        String resultRegister = registerCounter.allocateRegister();

        methodBuilder.appendBodyLine(String.format("%s = add i32 %s, %s", resultRegister, e2Register, e1Register));
    }

    @Override
    public void visit(SubtractExpr e) {
        e.e1().accept(this);
        e.e2().accept(this);

        String e1Register = registerCounter.getRegister(2);
        String e2Register = registerCounter.getRegister(1);
        String resultRegister = registerCounter.allocateRegister();

        methodBuilder.appendBodyLine(String.format("%s = sub i32 %s, %s", resultRegister, e2Register, e1Register));
    }

    @Override
    public void visit(MultExpr e) {
        e.e1().accept(this);
        e.e2().accept(this);

        String e1Register = registerCounter.getRegister(2);
        String e2Register = registerCounter.getRegister(1);
        String resultRegister = registerCounter.allocateRegister();

        methodBuilder.appendBodyLine(String.format("%s = mul i32 %s, %s", resultRegister, e2Register, e1Register));
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
        methodBuilder.appendBodyLine(llvmUtil.add(registerCounter.allocateRegister(), e.num(),0));
    }

    @Override
    public void visit(TrueExpr e) {
        methodBuilder.appendBodyLine(llvmUtil.add(registerCounter.allocateRegister(),1,0));
    }

    @Override
    public void visit(FalseExpr e) {
        methodBuilder.appendBodyLine(llvmUtil.add(registerCounter.allocateRegister(),0,0));
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
