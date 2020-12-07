package solution.utils;

import ast.*;

import static solution.utils.LLVMUtil.ArithmeticOp.ADD;

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
        return String.format("%%%s = call i8* @calloc i32 %d, i32 %d", targetReg, numOfObjects, size);
    }

    public String bitcast(String targetReg, String fromType, String reg2Cast, String toType){
        return String.format("%%%s = bitcast %%%s %s to %s", targetReg, fromType, reg2Cast, toType);
    }

    public String getElementPtr(String targetReg, String eType, String ePtr, String eName, int startIdx, int endIdx){
        return String.format("%%%s = getelementptr %s , %s @%s, i32 %d, i32 %d",
                targetReg, eType, ePtr, eName, startIdx, endIdx);
    }

    public String store(String eType, String fromReg, String addressType, String toReg){
        return String.format("store %s %%%s, %s %%%s", eType, fromReg, addressType, toReg);
    }

    public String load(String targetReg, String eType, String addressType, String fromReg){
        return String.format("%%%s = load %s, %s %s", targetReg, eType, addressType, fromReg);
    }

    public String op(ArithmeticOp arithmeticOp, String registerRes, String register1, String register2) {
        return String.format("%s = %s i32 %s, %s", registerRes, arithmeticOp, register1, register2);
    }

    public String op(ArithmeticOp arithmeticOp, String registerRes, String register, int num) {
        return String.format("%s = %s i32 %s, %d", registerRes, arithmeticOp, register, num);
    }

    public String op(ArithmeticOp arithmeticOp, String registerRes, int num, String register) {
        return String.format("%s = %s i32 %d, %s", registerRes, arithmeticOp, num, register);
    }

    public String op(ArithmeticOp arithmeticOp, String registerRes, int num1, int num2) {
        return String.format("%s = %s i32 %d, %d", registerRes, arithmeticOp, num1, num2);
    }

    // endregion

    public enum ArithmeticOp {
        ADD("add"), MUL("mul"), SUB("sub"), SLT("icmp slt");

        public final String op;

        private ArithmeticOp(String op) {
            this.op = op;
        }

        @Override
        public String toString() {
            return op;
        }
    }
}
