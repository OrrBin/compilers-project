package solution.utils;

import ast.*;

public class LLVMUtil {

    public String toLLVM(AstNode astNode) {
        String res;
        String className = astNode.getClass().getName();
        switch (className) {
            case "AstType":
                res = getTypeName((AstType) astNode);
                break;
            case "MethodDecl":
                res = "";
                break;
            default:
                res = "";
        }
        return res;
    }

    public static String getTypeName(AstType type) {
        if (type == null) {
            return "void";
        }
        if (type instanceof IntAstType) {
            return "i32";
        }
        if (type instanceof BoolAstType) {
            return "i1";
        }
        if (type instanceof IntArrayAstType) {
            return "i32*";
        }
        if (type instanceof RefType) {
            return "i8*";
        }

        throw new IllegalArgumentException("Unknown type");
    }

    public int getTypeSize(AstType type) {
        if (type == null) {
            return 0;
        }
        if (type instanceof IntAstType) {
            return 4;
        }
        if (type instanceof BoolAstType) {
            return 1;
        }
        if (type instanceof IntArrayAstType) {
            return 8;
        }
        if (type instanceof RefType) {
            return 8;
        }

        throw new IllegalArgumentException("Unknown type");
    }

    public boolean isSimpleType(Expr expr){
        return expr instanceof IntegerLiteralExpr || expr instanceof TrueExpr || expr instanceof FalseExpr;
    }

    public int simpleTypeToInt(Expr expr){
        if (expr instanceof IntegerLiteralExpr){
            return ((IntegerLiteralExpr) expr).num();
        }
        if (expr instanceof TrueExpr){
            return 1;
        }
        if (expr instanceof FalseExpr){
            return 0;
        }
        throw new IllegalArgumentException(expr.getClass().getName() + " isn't a simple type (IntegerLiteralExpr, TrueExpr, FalseExpr)");
    }

    // region create llvm commands

    public String br(String register, String label1, String label2) {
        return String.format("br i1 %s, label %%%s, label %%%s", register, label1, label2);
    }

    public String br(String label) {
        return String.format("br label %%%s", label);
    }

    public String alloca(String name, String type) {
        return String.format("%%%s = alloca %s", name, type);
    }

    public String calloc(String targetReg, int numOfObjects, int size){
        return String.format("%s = call i8* @calloc(i32 %d, i32 %d)", targetReg, numOfObjects, size);
    }

    public String calloc(String targetReg, String numOfObjectsReg, int size){
        return String.format("%s = call i8* @calloc(i32 %s, i32 %d)", targetReg, numOfObjectsReg, size);
    }

    public String bitcast(String targetReg, String fromType, String reg2Cast, String toType){
        return String.format("%s = bitcast %s %s to %s", targetReg, fromType, reg2Cast, toType);
    }

    public String getElementPtr(String targetReg, String eType, String ePtr, String eName, int startIdx, int endIdx){
        return String.format("%s = getelementptr %s, %s @.%s, i32 %d, i32 %d",
                targetReg, eType, ePtr, eName, startIdx, endIdx);
    }

    public String getElementPtr(String targetReg, String type, String registerBase, int offset){
        return String.format("%s = getelementptr %s, %s* %s, %s %d",
                targetReg, type, type, registerBase, type, offset);
    }

    public String getElementPtr(String targetReg, String type, String registerBase, String offsetReg){
        return String.format("%s = getelementptr %s, %s* %s, %s %s",
                targetReg, type, type, registerBase, type, offsetReg);
    }

    public String getElementPtr(String targetReg, String eType, String ptrType, String addressReg, int idxEntry) {
        return String.format( "%s = getelementptr %s, %s %s, i32 %d", targetReg, eType, ptrType, addressReg, idxEntry);
    }

    public String load(String registerRes, String type, String register) {
        return String.format("%s = load %s, %s* %s", registerRes, type, type, register);
    }

    public String op(ArithmeticOp arithmeticOp, String registerRes, String type, String register1, String register2) {
        return String.format("%s = %s %s %s, %s", registerRes, arithmeticOp, type, register1, register2);
    }

    public String op(ArithmeticOp arithmeticOp, String registerRes, String type, String register, int num) {
        return String.format("%s = %s %s %s, %d", registerRes, arithmeticOp, type, register, num);
    }

    public String op(ArithmeticOp arithmeticOp, String registerRes, String type, int num, String register) {
        return String.format("%s = %s %s %d, %s", registerRes, arithmeticOp, type, num, register);
    }

    public String op(ArithmeticOp arithmeticOp, String registerRes, String type, int num1, int num2) {
        return String.format("%s = %s %s %d, %d", registerRes, arithmeticOp, type, num1, num2);
    }

    public String print(String register) {
        return String.format("call void (i32) @print_int(i32 %s)", register);
    }

    public String print(int num) {
        return String.format("call void (i32) @print_int(i32 %d)", num);
    }

    public String store(String type, int value, String register) {
        return String.format("store %s %d, %s* %s", type, value, type, register);
    }

    public String store(String type, String valueRegister, String register) {
        return String.format("store %s %s, %s* %s", type, valueRegister, type, register);
    }

//    public String store(String eType, String fromReg, String addressType, String toReg){
//        return String.format("store %s %s, %s %s", eType, fromReg, addressType, toReg);
//    }

    public String throw_oob() {
        return "call void @throw_oob()";
    }

    public String ret(String retType, String retRegister) {
        return String.format("ret %s %s", retType, retRegister);
    }

    public String ret(String retType, int value) {
        return String.format("ret %s %d", retType, value);
    }

    public String callMethod(String targetReg, String methodSignature){
        return String.format("%s = call %s", targetReg, methodSignature);
    }

    public String phi(String allocateRegister, String type, String val1Reg, String cond1, String val2Reg, String cond2) {
        return String.format("%s = phi %s [%s, %%%s], [%s, %%%s]", allocateRegister, type, val1Reg, cond1, val2Reg, cond2);
    }

    public String phi(String allocateRegister, String type, int val1, String cond1, String val2Reg, String cond2) {
        return String.format("%s = phi %s [%d, %%%s], [%s, %%%s]", allocateRegister, type, val1, cond1, val2Reg, cond2);
    }

    public String phi(String allocateRegister, String type, String val1Reg, String cond1, int val2, String cond2) {
        return String.format("%s = phi %s [%s, %%%s], [%d, %%%s]", allocateRegister, type, val1Reg, cond1, val2, cond2);
    }

    public String phi(String allocateRegister, String type, int val1, String cond1, int val2, String cond2) {
        return String.format("%s = phi %s [%d, %%%s], [%d, %%%s]", allocateRegister, type, val1, cond1, val2, cond2);
    }
    // endregion

    public enum ArithmeticOp {
        ADD("add"), MUL("mul"), SUB("sub"), SLT("icmp slt");

        public final String op;

        ArithmeticOp(String op) {
            this.op = op;
        }

        @Override
        public String toString() {
            return op;
        }
    }
}
