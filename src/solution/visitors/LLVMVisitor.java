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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import static solution.symbol_table.symbol_types.SymbolKeyType.METHOD;
import static solution.symbol_table.symbol_types.SymbolKeyType.VAR;
import static solution.utils.LLVMUtil.ArithmeticOp.*;
import static solution.utils.LLVMUtil.getTypeName;

public class LLVMVisitor implements Visitor {

    public static final int VTABLEBYTES_P = 8;

    public static final String THIS_REG = "%this";
    public static final String I_8 = "i8";
    public static final String I_8_P = "i8*";

    public static final String I_32_P = "i32*";
    public static final String I_32 = "i32";

    public static final String I_1 = "i1";
    public static final String I_1_P = "i1*";

    private OutputStream outputStream;
    private LLVMUtil llvmUtil;
    private AstNodeUtil astNodeUtil;
    private MethodLLVMBuilder methodBuilder = new MethodLLVMBuilder();
    private RegisterCounter registerCounter = new RegisterCounter();
    private LabelCounter labelCounter = new LabelCounter();
    private LabelCounter spareLabels4Tmp = new LabelCounter();

    public LLVMVisitor(OutputStream outputStream, LLVMUtil llvmUtil, AstNodeUtil astNodeUtil) {
        this.outputStream = outputStream;
        this.llvmUtil = llvmUtil;
        this.astNodeUtil = astNodeUtil;
    }

    @Override
    public void visit(Program program) {
        try {
            methodBuilder.appendBody("\n");
            methodBuilder.appendBody(Files.readString(Path.of("/Users/ozzafar/IdeaProjects/compilers-project/src/solution/prog_set_up")));
        } catch (IOException e) {
            e.printStackTrace();
        }

        program.mainClass().accept(this);
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
        methodBuilder.appendBody("define i32 @main() {\n");
        mainClass.mainStatement().accept(this);
        methodBuilder.appendBodyLine(llvmUtil.ret(I_32, 0));
        methodBuilder.appendBody("}\n\n");

        writeAndClean();
    }

    @Override
    public void visit(MethodDecl methodDecl) {

        // declaration
        String declaration = String.format("define %s @%s.%s(" + I_8_P + " %%this",
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

        methodBuilder.appendBody("}\n\n");

        // clean up
        writeAndClean();
    }

    private void writeAndClean() {
        try {
            outputStream.write(methodBuilder.toString().getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }

        methodBuilder.clear();
        registerCounter.resetRegisterCounter();
        labelCounter.resetLabelCounter();
    }

    @Override
    public void visit(FormalArg formalArg) {
        //Example: i32 %.x
        String name = formalArg.name();
        methodBuilder.appendDeclaration(getTypeName(formalArg.type()))
                .appendDeclaration(String.format(" %%.%s", name));

        //Example:  %x = alloca i32
        //          store i32 %.x, i32* %x
        var formalName = "." + name;
        var nameReg = "%" + name;
        var formalNameReg = "%" + formalName;
        String type = getTypeName(formalArg.type());
        methodBuilder.appendBodyLine(llvmUtil.alloca(name, type));
        methodBuilder.appendBodyLine(llvmUtil.store(type, formalNameReg, nameReg));

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
        // condition
        ifStatement.cond().accept(this);

        // labels
        String if0 = "if" + labelCounter.allocateLabelNumber();
        String if1 = "if" + labelCounter.allocateLabelNumber();
        String if2 = "if" + labelCounter.allocateLabelNumber();

        methodBuilder.appendBodyLine(llvmUtil.br(registerCounter.getLastRegister(), if0, if1));
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

        var var = (VariableIntroduction) astNodeUtil.getDeclFromName(VAR, lv, assignStatement);
        String varType = getTypeName(var.type());
        String lvReg = "%" + lv;

        if(astNodeUtil.isField(var)) {
            lvReg = getFieldLocFromHeap(var, varType, assignStatement);
        }
        rv.accept(this);
        String rvReg = rv instanceof ThisExpr ? THIS_REG : registerCounter.getLastRegister();
        res = llvmUtil.store(varType, rvReg, lvReg);

        methodBuilder.appendBodyLine(res);
    }

    @Override
    public void visit(AssignArrayStatement assignArrayStatement) {

        Expr rv = assignArrayStatement.rv();
        Expr index = assignArrayStatement.index();

        var lvIdReg = "%" + assignArrayStatement.lv();
        VariableIntroduction var = (VariableIntroduction) astNodeUtil.getDeclFromName(VAR, assignArrayStatement.lv(), assignArrayStatement);
        if(astNodeUtil.isField(var)) {
            lvIdReg = getFieldLocFromHeap(var, getTypeName(var.type()), assignArrayStatement);
        }

        // Load the address of the x array
        String arrRegister = registerCounter.allocateRegister();
        methodBuilder.appendBodyLine(llvmUtil.load(arrRegister, I_32_P, lvIdReg));

        // continue the accessing procedure
        accessArray2LLVM(index);
        var ptrElementReg = registerCounter.getLastRegister();
        rv.accept(this);
        methodBuilder.appendBodyLine(llvmUtil.store(I_32, registerCounter.getLastRegister(), ptrElementReg));
    }


    public void accessArray2LLVM(Expr index) {
        String arr_alloc2 = "arr_alloc" + labelCounter.allocateLabelNumber();
        String arr_alloc3 = "arr_alloc" + labelCounter.allocateLabelNumber();
        String arr_alloc4 = "arr_alloc" + labelCounter.allocateLabelNumber();
        String arr_alloc5 = "arr_alloc" + labelCounter.allocateLabelNumber();

        String arrRegister = registerCounter.getLastRegister();

        // Check that the index is greater than zero
        index.accept(this);
        String indexRegister = registerCounter.getLastRegister();
        methodBuilder.appendBodyLine(llvmUtil.op(SLT, registerCounter.allocateRegister(),I_32, indexRegister, 0));
        methodBuilder.appendBodyLine(llvmUtil.br(registerCounter.getLastRegister(), arr_alloc2, arr_alloc3));
        // Else throw out of bounds exception
        methodBuilder.appendLabel(arr_alloc2);
        methodBuilder.appendBodyLine(llvmUtil.throw_oob());
        methodBuilder.appendBodyLine(llvmUtil.br(arr_alloc3));
        // Load the size of the array (first integer of the array)
        methodBuilder.appendLabel(arr_alloc3);
        methodBuilder.appendBodyLine(llvmUtil.getElementPtr(registerCounter.allocateRegister(), I_32, arrRegister, 0));
        methodBuilder.appendBodyLine(llvmUtil.load(registerCounter.allocateRegister(), I_32, registerCounter.getRegister(2)));
        // Check that the index is less than the size of the array
        methodBuilder.appendBodyLine(llvmUtil.op(SLE, registerCounter.allocateRegister(),I_32, registerCounter.getRegister(2), indexRegister));
        methodBuilder.appendBodyLine(llvmUtil.br(registerCounter.getLastRegister(), arr_alloc4, arr_alloc5));
        // Else throw out of bounds exception
        methodBuilder.appendLabel(arr_alloc4);
        methodBuilder.appendBodyLine(llvmUtil.throw_oob());
        methodBuilder.appendBodyLine(llvmUtil.br(arr_alloc5));
        // All ok, we can safely index the array now
        methodBuilder.appendLabel(arr_alloc5);
        methodBuilder.appendBodyLine(llvmUtil.op(ADD, registerCounter.allocateRegister(), I_32, indexRegister, 1)); // We'll be accessing our array at index + 1, since the first element holds the size
        String ptrElement = registerCounter.allocateRegister();
        methodBuilder.appendBodyLine(llvmUtil.getElementPtr(ptrElement, I_32, arrRegister, registerCounter.getRegister(2)));

    }

    @Override
    public void visit(AndExpr e) {
        // labels
        String andcond0 = "andcond" + labelCounter.allocateLabelNumber();
        String andcond1 = "andcond" + labelCounter.allocateLabelNumber();
        String andcond2 = "andcond" + labelCounter.allocateLabelNumber();
        String andcond3 = "andcond" + labelCounter.allocateLabelNumber();

        e.e1().accept(this);
        methodBuilder.appendBodyLine(llvmUtil.br(andcond0));
        methodBuilder.appendLabel(andcond0);
        methodBuilder.appendBodyLine(llvmUtil.br(registerCounter.getLastRegister(), andcond1, andcond3));
        methodBuilder.appendLabel(andcond1);
        e.e2().accept(this);
        methodBuilder.appendBodyLine(llvmUtil.br(andcond2));
        methodBuilder.appendLabel(andcond2);
        methodBuilder.appendBodyLine(llvmUtil.br(andcond3));
        methodBuilder.appendLabel(andcond3);
        methodBuilder.appendBodyLine(llvmUtil.phi(registerCounter.allocateRegister(), I_1, 0, andcond0, registerCounter.getRegister(2), andcond2));
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

        methodBuilder.appendBodyLine(llvmUtil.op(op, newReg, I_32, e1Reg, e2Reg));
    }

    private void oneIntLiteral(LLVMUtil.ArithmeticOp op, boolean litOnRight, Expr e2, IntegerLiteralExpr e1) {

        e2.accept(this);
        String e2Reg = registerCounter.getLastRegister();

        String newReg = registerCounter.allocateRegister();

        int e1Number = e1.num();

        //i.e. e1 is the right side of the expression
        if (litOnRight) {
            methodBuilder.appendBodyLine(llvmUtil.op(op, newReg, I_32, e2Reg, e1Number));
        } else {
            methodBuilder.appendBodyLine(llvmUtil.op(op, newReg, I_32, e1Number, e2Reg));
        }
    }

    private void twoIntLiteral(LLVMUtil.ArithmeticOp op, IntegerLiteralExpr e1, IntegerLiteralExpr e2) {
        String newReg = registerCounter.allocateRegister();
        int e1Number = e1.num();
        int e2Number = e2.num();
        methodBuilder.appendBodyLine(llvmUtil.op(op, newReg, I_32, e1Number, e2Number));
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
        if (!(e1 instanceof IntegerLiteralExpr) && !(e2 instanceof IntegerLiteralExpr)) {
            zeroIntLiteral(e, op);
        }
        else {
            //case3: e1 is int-literal
         if (e1 instanceof IntegerLiteralExpr) {
                litOnRight = false;
                Expr tmp = e1;
                e1 = e2;
                e2 = tmp;
            }

            //case4: e2 is int-literal
            oneIntLiteral(op, litOnRight, e1, (IntegerLiteralExpr) e2);
        }
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
        e.arrayExpr().accept(this);
        accessArray2LLVM(e.indexExpr());
        methodBuilder.appendBodyLine(llvmUtil.load(registerCounter.allocateRegister(), I_32, registerCounter.getRegister(2)));
    }

    @Override
    public void visit(ArrayLengthExpr e) {
        e.arrayExpr().accept(this);

        //array length is stored at array[0]
        //getting pointer to array[0]
        methodBuilder.appendBodyLine(llvmUtil.getElementPtr(registerCounter.allocateRegister(),
                I_32, I_32_P, registerCounter.getRegister(2), 0));

        //accessing array[0] using the pointer
        methodBuilder.appendBodyLine(llvmUtil.load(registerCounter.allocateRegister(), I_32, registerCounter.getRegister(2)));
    }

    @Override
    public void visit(MethodCallExpr e) {
        // First load the object pointer from the stack variable for b
        // e.g. %_6 = load i8*, i8** %b

        // call the owner expression's visitor
        var owner = e.ownerExpr();
        owner.accept(this);

        // Do the required bitcasts, so that we can access the vtable pointer - we're holding a pointer to i8**
        // e.g. %_7 = bitcast i8* %_6 to i8***

        // Load vtable_ptr
        // e.g. %_8 = load i8**, i8*** %_7

        // bitcast & load to access the vtable ptr

        String ownerReg = owner instanceof ThisExpr ? THIS_REG : registerCounter.getLastRegister();

        methodBuilder.appendBodyLine(llvmUtil.bitcast(registerCounter.allocateRegister(),
                I_8_P, ownerReg, I_8_P + "**"));
        methodBuilder.appendBodyLine(llvmUtil.load(registerCounter.allocateRegister(),
                I_8_P + "*", registerCounter.getRegister(2)));

        // Get a pointer to the 0-th entry in the vtable.
        // The index here is exactly the offset corresponding to Base::set.
        // e.g. %_9 = getelementptr i8*, i8** %_8, i32 0

        // find method index in vtable & get a ptr to its entry
        int methodIdxInVtable = astNodeUtil.getMethodIdxInVtable(e);
        methodBuilder.appendBodyLine(llvmUtil.getElementPtr(registerCounter.allocateRegister(),
                I_8_P, I_8_P + "*", registerCounter.getRegister(2), methodIdxInVtable));

        // Read into the array to get the actual function pointer
        // e.g. %_10 = load i8*, i8** %_9
        methodBuilder.appendBodyLine(llvmUtil.load(registerCounter.allocateRegister(), I_8_P, registerCounter.getRegister(2)));

        // Cast the function pointer from i8* to a function ptr type that matches the function's signature,
        // so that we can call it.
        // e.g. %_11 = bitcast i8* %_10 to i32 (i8*, i32)*
        String methodTypesPtr = getMethodTypesLLVM(e);
        String methodReg = registerCounter.allocateRegister();
        methodBuilder.appendBodyLine(llvmUtil.bitcast(methodReg,
                I_8_P, registerCounter.getRegister(2), methodTypesPtr));

        // Perform the call on the function pointer. Note that the first argument is the receiver object ("this").
        // e.g.  %_12 = call i32 %_11(i8* %_6, i32 1)
        var methodSignature = callMethodLLVM(e, ownerReg, methodReg);
        methodBuilder.appendBodyLine(llvmUtil.callMethod(registerCounter.allocateRegister(), methodSignature));

    }

    //region method call helper functions

    private String getMethodTypesLLVM(MethodCallExpr e) {
        return callMethodLLVM(e, null, null);
    }

    private String callMethodLLVM(MethodCallExpr e, String thisRegister, String methodPtrReg){

        var methodDecl = (MethodDecl) astNodeUtil.getDeclFromName(METHOD, e.methodId(), e);
        var retType = getTypeName(methodDecl.returnType());
        StringBuilder methodArgsTypes = new StringBuilder();
        var formals = methodDecl.formals();
        var actuals = e.actuals();
        int size = actuals.size();
        for (int i = 0; i < size; i++) {
            FormalArg arg = formals.get(i);
            methodArgsTypes.append(getTypeName(arg.type()));
            if (methodPtrReg != null) {
                Expr expr = actuals.get(i);
                expr.accept(this);
                String actualReg = expr instanceof ThisExpr ? THIS_REG : registerCounter.getLastRegister();
                methodArgsTypes.append(" ").append(actualReg);
            }
            methodArgsTypes.append(", ");
        }
        //remove last comma
        if (methodArgsTypes.length() > 0) {
            methodArgsTypes.deleteCharAt(methodArgsTypes.length() - 1);
            methodArgsTypes.deleteCharAt(methodArgsTypes.length() - 1);
        }

        String methodTypes = methodArgsTypes.length() > 0 ? ", " + methodArgsTypes : "";
        return methodPtrReg != null ?
                String.format("%s %s(" + I_8_P + " %s%s)", retType, methodPtrReg, thisRegister, methodTypes)
                : String.format("%s (" + I_8_P + "%s)*", retType, methodTypes);
    }

    //endregion

    @Override
    public void visit(IntegerLiteralExpr e) {
        methodBuilder.appendBodyLine(llvmUtil.op(ADD, registerCounter.allocateRegister(), I_32, e.num(), 0));
    }

    @Override
    public void visit(TrueExpr e) {
        methodBuilder.appendBodyLine(llvmUtil.op(ADD, registerCounter.allocateRegister(), I_1, 1, 0));
    }

    @Override
    public void visit(FalseExpr e) {
        methodBuilder.appendBodyLine(llvmUtil.op(ADD, registerCounter.allocateRegister(), I_1, 0, 0));
    }

    @Override
    public void visit(IdentifierExpr e) {
        var id = e.id();
        var idReg = "%" + id;
        var declNode = (VariableIntroduction) astNodeUtil.getDeclFromName(VAR, id, e);
        String varType = getTypeName(declNode.type());
        if(astNodeUtil.isField(declNode)) {
            idReg = getFieldLocFromHeap(declNode, varType, e);
        }
        methodBuilder.appendBodyLine(llvmUtil.load(registerCounter.allocateRegister(), varType, idReg));
    }

    private String getFieldLocFromHeap( VariableIntroduction declNode, String varType, AstNode scope) {

            String varTypePtr = varType + "*";
            int fieldPos = astNodeUtil.getFieldIdxInObjAlloc(scope, declNode.name()) + VTABLEBYTES_P;
            methodBuilder.appendBodyLine(llvmUtil.getElementPtr(registerCounter.allocateRegister(),
                    I_8, I_8_P, THIS_REG, fieldPos));

            methodBuilder.appendBodyLine(llvmUtil.bitcast(registerCounter.allocateRegister(),
                    I_8_P, registerCounter.getRegister(2), varTypePtr));

        return registerCounter.getLastRegister();
    }

    @Override
    public void visit(ThisExpr e) {
//        methodBuilder.appendBodyLine(llvmUtil.load(registerCounter.allocateRegister(), I_8_P, THIS_REG));
    }

    @Override
    public void visit(NewIntArrayExpr e) {

        labelCounter.resetLabelCounter();

        // Compute array size
        e.lengthExpr().accept(this);
        String arrSizeReg = registerCounter.getLastRegister();

        // Check that the size of the array is not negative
        String arrAlloc0 = "arr_alloc" + labelCounter.allocateLabelNumber();
        String arrAlloc1 = "arr_alloc" + labelCounter.allocateLabelNumber();

        String cmpResReg = registerCounter.allocateRegister();
        methodBuilder.appendBodyLine(llvmUtil.op(SLT, cmpResReg, I_32, arrSizeReg, 0));
        methodBuilder.appendBodyLine(llvmUtil.br(cmpResReg, arrAlloc0, arrAlloc1));

        //arr_alloc0:
        // Size was negative, throw negative size exception
        methodBuilder.appendLabel(arrAlloc0);
        methodBuilder.appendBodyLine(llvmUtil.throw_oob());
        methodBuilder.appendBodyLine(llvmUtil.br(arrAlloc1));

        //arr_alloc1:
        // Calculate size bytes to be allocated for the array (new arr[sz] -> add i32 1, sz)
        // We need an additional int worth of space, to store the size of the array.
        methodBuilder.appendLabel(arrAlloc1);
        String arrSizeWithSpace4SizeReg = registerCounter.allocateRegister();
        methodBuilder.appendBodyLine(llvmUtil.op(ADD, arrSizeWithSpace4SizeReg, I_32, arrSizeReg, 1));

        //Allocate sz + 1 integers (4 bytes each)
        String arrHeapLocReg = registerCounter.allocateRegister();
        int sizeOfInt = 4;
        methodBuilder.appendBodyLine(llvmUtil.calloc(arrHeapLocReg, arrSizeWithSpace4SizeReg, sizeOfInt));

        //Cast the returned pointer
        String castedPtrReg = registerCounter.allocateRegister();
        methodBuilder.appendBodyLine(llvmUtil.bitcast(castedPtrReg, I_8_P, arrHeapLocReg, I_32_P));

        //Store the size of the array in the first position of the array
        methodBuilder.appendBodyLine(llvmUtil.store(I_32, arrSizeReg, castedPtrReg));

    }

    @Override
    public void visit(NewObjectExpr e) {
        String heapLocReg = registerCounter.allocateRegister();

        //Get class declaration astNode
        ClassDecl classDecl = astNodeUtil.getClassDeclFromId(e, e.classId());

        //Calculate size of object
        int objSize = calculateObjectSize(classDecl);
        int obj2Create = 1;

        // Allocate the required memory on heap for our object
        methodBuilder.appendBodyLine(llvmUtil.calloc(heapLocReg, obj2Create, objSize));


        // Set the vtable pointer to point to the correct vtable
        // e.g.  %_1 = bitcast i8* %_0 to i8***
        String vTablePtrReg = registerCounter.allocateRegister();
        methodBuilder.appendBodyLine(llvmUtil.bitcast(vTablePtrReg, I_8_P, heapLocReg, I_8_P + "**"));

        //Get the address of the first element of the Base_vtable
        //e.g. %_2 = getelementptr [2 x i8*], [2 x i8*]* @.Base_vtable, i32 0, i32 0
        String firstEVTable = registerCounter.allocateRegister();
        int numOfMethods = astNodeUtil.getNumOfMethods(classDecl);
        String vTableElements = String.format("[%s x " + I_8_P + "]",numOfMethods);
        String vTableElementsPtr = vTableElements +"*";
        String classNameVtable = classDecl.name() + "_vtable";
        methodBuilder.appendBodyLine(llvmUtil.getElementPtr(firstEVTable,vTableElements,
                vTableElementsPtr, classNameVtable, 0, 0));

        // Set the vtable to the correct address.
        // e.g. store i8** %_2, i8*** %_1
        methodBuilder.appendBodyLine(llvmUtil.store(I_8_P + "*", firstEVTable, vTablePtrReg));

        // enforce last register to point the allocated object

        String tmp = "tmp" + spareLabels4Tmp.allocateLabelNumber();
        String tmpRegister = "%" + tmp ;
        methodBuilder.appendBodyLine(llvmUtil.alloca(tmp, I_8_P));
        methodBuilder.appendBodyLine(llvmUtil.store(I_8_P, heapLocReg, tmpRegister));
        methodBuilder.appendBodyLine(llvmUtil.load(registerCounter.allocateRegister(), I_8_P, tmpRegister));
    }

    //region new object space allocation

    private int calculateObjectSize(ClassDecl classDecl) {
        int vTableAddress = 8, space4Fields = 0, typeSize;
        //find ClassDecl node that matches classId
        space4Fields = getSizeOfFields(classDecl);

        return vTableAddress + space4Fields;
    }

    private int getSizeOfFields(ClassDecl classDecl){
        int sizeOfFields = 0;
        Map<String, AstType> fieldsNameType = new HashMap<>();
        Stack<ClassDecl> ancestorClassPath = new Stack<>();
        ancestorClassPath.push(classDecl);

        ClassDecl curClass;
        var parentClass = astNodeUtil.getEnclosingScope(classDecl).parentSymbolTable.symbolTableScope;

        //climb up the ancestor tree till reaching root
        while(parentClass != null){
            if(parentClass instanceof Program) {break;}

            curClass = (ClassDecl)parentClass;
            ancestorClassPath.push(curClass);
            parentClass = astNodeUtil.getEnclosingScope(curClass).parentSymbolTable.symbolTableScope;
        }
        //now curClass is the first super class
        //descending the path and collecting methods

        while(!ancestorClassPath.empty()){
            curClass = ancestorClassPath.pop();
            curClass.fields().forEach(field->fieldsNameType.put(field.name(), field.type()));
        }

        var keySet = fieldsNameType.keySet();
        for(var field : keySet){
            sizeOfFields += LLVMUtil.getTypeSize(fieldsNameType.get(field));
        }
        return sizeOfFields;
    }
    //endregion

    @Override
    public void visit(NotExpr e) {
        e.e().accept(this);
        methodBuilder.appendBodyLine(llvmUtil.op(SUB, registerCounter.allocateRegister(), I_1,
                1, registerCounter.getRegister(2)));
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
