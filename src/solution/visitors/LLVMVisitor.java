package solution.visitors;

import ast.*;
import solution.llvm_builders.MethodLLVMBuilder;
import solution.utils.AstNodeUtil;
import solution.utils.LLVMUtil;
import solution.utils.LabelCounter;
import solution.utils.RegisterCounter;

import java.io.IOException;
import java.io.OutputStream;

import static solution.utils.LLVMUtil.ArithmeticOp.*;
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
        String if0 = "if" + labelCounter.allocateLabelNumber();
        String if1 = "if" + labelCounter.allocateLabelNumber();
        String if2 = "if" + labelCounter.allocateLabelNumber();

        // condition
        ifStatement.cond().accept(this);
        methodBuilder.appendBodyLine(llvmUtil.br(registerCounter.getLastRegister(), if0, if1));

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
        mathOp2LLVM(e, SLT);
    }

 //region math-op expressions

    private void zeroIntLiteral(BinaryExpr e, LLVMUtil.ArithmeticOp op) {
        e.e1().accept(this);
        String e1Reg = registerCounter.getLastRegister();
        e.e2().accept(this);
        String e2Reg = registerCounter.getLastRegister();
        String newReg = registerCounter.allocateRegister();

        methodBuilder.appendBodyLine(String.format(llvmUtil.op(op, newReg, e1Reg, e2Reg)));
    }

    private void oneIntLiteral(LLVMUtil.ArithmeticOp op, boolean litOnRight, Expr e2, IntegerLiteralExpr e1) {

        e2.accept(this);
        String e2Reg = registerCounter.getLastRegister();

        String newReg = registerCounter.allocateRegister();

        int e1Number = e1.num();

        //i.e. e1 is the right side of the expression
        if (litOnRight) {
            methodBuilder.appendBodyLine(llvmUtil.op(op, newReg, e2Reg, e1Number));
        } else {
            methodBuilder.appendBodyLine(llvmUtil.op(op, newReg, e1Number, e2Reg));
        }
    }

    private void twoIntLiteral(LLVMUtil.ArithmeticOp op, IntegerLiteralExpr e1, IntegerLiteralExpr e2) {
        String newReg = registerCounter.allocateRegister();
        int e1Number = e1.num();
        int e2Number =e2.num();
        methodBuilder.appendBodyLine(llvmUtil.op(op, newReg, e1Number, e2Number));
    }

    private void mathOp2LLVM(BinaryExpr e, LLVMUtil.ArithmeticOp op) {
        boolean litOnRight = true;

        var e1 = e.e1();
        var e2 = e.e2();
        var e1Type = e.e1().getClass();
        var e2Type = e.e2().getClass();

        //case1: both are int-literals
        if(e1 instanceof IntegerLiteralExpr && e2 instanceof IntegerLiteralExpr){
            twoIntLiteral(op, (IntegerLiteralExpr) e1, (IntegerLiteralExpr) e2);
        }
        //case2: neither is int-literal
        if(e1Type != IntegerLiteralExpr.class && e2Type != IntegerLiteralExpr.class){
            zeroIntLiteral(e, op);
        }
        //case3: e1 is int-literal
        else if(e1Type == IntegerLiteralExpr.class){
            litOnRight = false;
            Expr tmp = e1;
            e1 = e2;
            e2 = tmp;
        }

        //case4: e2 is int-literal
        oneIntLiteral(op, litOnRight, e2, (IntegerLiteralExpr) e1);
    }

    @Override
    public void visit(AddExpr e) {
        mathOp2LLVM(e, ADD);
    }


    @Override
    public void visit(SubtractExpr e) {
        mathOp2LLVM(e, SUB);
    }

    @Override
    public void visit(MultExpr e) {
        mathOp2LLVM(e, MUL);
    }

    //endregion

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
        methodBuilder.appendBodyLine(llvmUtil.op(ADD, registerCounter.allocateRegister(), e.num(), 0));
    }

    @Override
    public void visit(TrueExpr e) {
        methodBuilder.appendBodyLine(llvmUtil.op(ADD, registerCounter.allocateRegister(), 1, 0));
    }

    @Override
    public void visit(FalseExpr e) {
        methodBuilder.appendBodyLine(llvmUtil.op(ADD, registerCounter.allocateRegister(), 0, 0));
    }

    @Override
    public void visit(IdentifierExpr e) {
//        var symbolTableScope = astNodeUtil.getEnclosingScope(e);
//        var id = e.id();
//        var declNode = (VariableIntroduction) astNodeUtil.getDeclFromCurUse(SymbolKeyType.VAR, id, e);
//        AstType type = declNode.type();
//        methodBuilder.appendBodyLine(String.format("%s = load %s, %s* %%%s", allocateRegister(), type, type, id));
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
