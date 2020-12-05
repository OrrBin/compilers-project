package solution.utils;

import ast.*;

public class LLVMUtil {
    public String toLLVM(AstNode astNode){
        String res;
        String className = astNode.getClass().getName();
        switch (className) {
            case "AstType":
                res = getTypeName((AstType)astNode);
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

    public String add(String registerRes, String register1, String register2){
        return String.format("%s = add i32 %s, %s", registerRes, register1, register2);
    }

    public String add(String registerRes, String register, int num){
        return String.format("%s = add i32 %s, %d", registerRes, register, num);
    }

    public String add(String registerRes, int num, String register){
        return String.format("%s = add i32 %d, %s", registerRes, num, register);
    }

    public String add(String registerRes, int num1, int num2){
        return String.format("%s = add i32 %d, %d", registerRes, num1, num2);
    }

    // endregion

}
