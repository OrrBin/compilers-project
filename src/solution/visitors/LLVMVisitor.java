package solution.visitors;

import ast.*;
import solution.llvm_builders.MethodLLVMBuilder;
import solution.symbol_table.symbol_types.SymbolKeyType;
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
        methodDecl.ret().accept(this);
        String retType = getTypeName(methodDecl.returnType());
        String retRegister = registerCounter.getLastRegister();
        methodBuilder.appendBodyLine(llvmUtil.ret(retType, retRegister));

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
        String loop0 = "loop" + labelCounter.allocateLabelNumber();
        String loop1 = "loop" + labelCounter.allocateLabelNumber();
        String loop2 = "loop" + labelCounter.allocateLabelNumber();

        // condition
        methodBuilder.appendBodyLine(llvmUtil.br(loop0));
        methodBuilder.appendLabel(loop0);
        whileStatement.cond().accept(this);
        methodBuilder.appendBodyLine(llvmUtil.br(registerCounter.getLastRegister(), loop1, loop2));

        // body
        methodBuilder.appendLabel(loop1);
        whileStatement.body().accept(this);
        methodBuilder.appendBodyLine(llvmUtil.br(loop0));

        // exit
        methodBuilder.appendLabel(loop2);
    }

    @Override
    public void visit(SysoutStatement sysoutStatement) {
        String llvmLine;
        Expr arg = sysoutStatement.arg();
        if (arg instanceof IntegerLiteralExpr){
            llvmLine = llvmUtil.print(((IntegerLiteralExpr) arg).num());
        } else {
            arg.accept(this);
            llvmLine = llvmUtil.print(registerCounter.getLastRegister());
        }
        methodBuilder.appendBodyLine(llvmLine);
    }

    @Override
    public void visit(AssignStatement assignStatement) {
        String res;
        Expr rv = assignStatement.rv();
        String lv = assignStatement.lv();

        VarDecl var = (VarDecl) astNodeUtil.getDeclFromName(SymbolKeyType.VAR, lv, assignStatement);
        String type = getTypeName(var.type());

        if (llvmUtil.isSimpleType(rv)){
            res = llvmUtil.store(type, llvmUtil.simpleTypeToInt(rv), lv);
        } else {
            rv.accept(this);
            res = llvmUtil.store(type, registerCounter.getLastRegister(), lv);
        }
        methodBuilder.appendBodyLine(res);
    }

    @Override
    public void visit(AssignArrayStatement assignArrayStatement) {

        Expr rv = assignArrayStatement.rv();
        Expr index = assignArrayStatement.index();
        String arr_alloc2 = "arr_alloc" + labelCounter.allocateLabelNumber();
        String arr_alloc3 = "arr_alloc" + labelCounter.allocateLabelNumber();
        String arr_alloc4 = "arr_alloc" + labelCounter.allocateLabelNumber();
        String arr_alloc5 = "arr_alloc" + labelCounter.allocateLabelNumber();

        // Load the address of the x array
        String arrRegister = registerCounter.allocateRegister();
        methodBuilder.appendBodyLine(llvmUtil.load(arrRegister, "i32*", assignArrayStatement.lv()));
        // Check that the index is greater than zero
        // TODO consult with Ors if have to separate cases according to index type (is it stupid to calculate in register a IntegerLiteral?)
        index.accept(this);
        String indexRegister = registerCounter.getLastRegister();
        methodBuilder.appendBodyLine(llvmUtil.op(SLT, registerCounter.allocateRegister(), indexRegister, 0));
        methodBuilder.appendBodyLine(llvmUtil.br(registerCounter.getLastRegister(), arr_alloc2, arr_alloc3));
        // Else throw out of bounds exception
        methodBuilder.appendLabel(arr_alloc2);
        methodBuilder.appendBodyLine(llvmUtil.throw_oob());
        methodBuilder.appendBodyLine(llvmUtil.br(arr_alloc3));
        // Load the size of the array (first integer of the array)
        methodBuilder.appendLabel(arr_alloc3);
        methodBuilder.appendBodyLine(llvmUtil.getElementPtr(registerCounter.allocateRegister(), "i32", arrRegister, 0));
        methodBuilder.appendBodyLine(llvmUtil.load(registerCounter.allocateRegister(), "i32", registerCounter.getRegister(2)));
        // Check that the index is less than the size of the array
        methodBuilder.appendBodyLine(llvmUtil.op(SLT, registerCounter.allocateRegister(), registerCounter.getRegister(2), indexRegister));
        methodBuilder.appendBodyLine(llvmUtil.br(registerCounter.getLastRegister(), arr_alloc4, arr_alloc5));
        // Else throw out of bounds exception
        methodBuilder.appendLabel(arr_alloc4);
        methodBuilder.appendBodyLine(llvmUtil.throw_oob());
        methodBuilder.appendBodyLine(llvmUtil.br(arr_alloc5));
        // All ok, we can safely index the array now
        methodBuilder.appendLabel(arr_alloc5);
        methodBuilder.appendBodyLine(llvmUtil.op(ADD, registerCounter.allocateRegister(), indexRegister, 1)); // We'll be accessing our array at index + 1, since the first element holds the size
        String ptrElement = registerCounter.allocateRegister();
        methodBuilder.appendBodyLine(llvmUtil.getElementPtr(ptrElement, "i32", arrRegister, registerCounter.getRegister(2)));
        rv.accept(this);
        methodBuilder.appendBodyLine(llvmUtil.store("i32", registerCounter.getLastRegister(), ptrElement));
    }

    @Override
    public void visit(AndExpr e) {
        // TODO Oz
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

        //case1: both are int-literals
        if (e1 instanceof IntegerLiteralExpr && e2 instanceof IntegerLiteralExpr) {
            twoIntLiteral(op, (IntegerLiteralExpr) e1, (IntegerLiteralExpr) e2);
        }
        //case2: neither is int-literal
        if (e1 instanceof IntegerLiteralExpr && !(e2 instanceof IntegerLiteralExpr)) {
            zeroIntLiteral(e, op);
        }
        //case3: e1 is int-literal
        else if (e1 instanceof IntegerLiteralExpr) {
            litOnRight = false;
            Expr tmp = e1;
            e1 = e2;
            e2 = tmp;
        }

        //case4: e2 is int-literal
        oneIntLiteral(op, litOnRight, e1, (IntegerLiteralExpr) e2);
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
        // TODO Oz
    }

    @Override
    public void visit(ArrayLengthExpr e) {
        // TODO Oz
    }

    @Override
    public void visit(MethodCallExpr e) {
        // TODO Oz
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
        var id = e.id();
        var declNode = (VariableIntroduction) astNodeUtil.getDeclFromName(SymbolKeyType.VAR, id, e);
        String type = getTypeName(declNode.type());
        methodBuilder.appendBodyLine(String.format("%s = load %s, %s* %%%s", registerCounter.allocateRegister(), type, type, id));
    }

    @Override
    public void visit(ThisExpr e) {
        // TODO Or
    }

    @Override
    public void visit(NewIntArrayExpr e) {
        // TODO Or
    }

    @Override
    public void visit(NewObjectExpr e) {
        // TODO Or
    }

    @Override
    public void visit(NotExpr e) {
        // TODO Or
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
