package solution;

import ast.AstNode;
import ast.AstType;
import ast.BoolAstType;
import ast.IntArrayAstType;
import ast.IntAstType;
import ast.RefType;

public class LLVMUtil {
    public String toLLVM(AstNode astNode){
        String res;
        String className = astNode.getClass().getName();
        switch (className) {
            case "AstType":
                res = astTypeToLLVM((AstType)astNode);
                break;
            case "MethodDecl":
                res = "";
                break;
            default:
                res = "";
        }
        return res;
    }

    private String astTypeToLLVM(AstType astType) {
        String res;
        String type = astType.getClass().getName();
        switch (type) {
            case "BoolAstType":
                res = "i1";
                break;
            case "IntArrayAstType":
                res = "i32*";
                break;
            case "IntAstType":
                res = "i32";
                break;
            case "RefType":
                res = "i8*";
                break;
            default:
                res = "void";
        }
        return res;
    }

    public static String getTypeName(AstType type) {
        if(type instanceof IntAstType) {
            return "i32";
        }
        if(type instanceof BoolAstType) {
            return "i1";
        }
        if(type instanceof IntArrayAstType) {
            return "i32*";
        }
        if(type instanceof RefType) {
            return "i8*";
        }

        throw new IllegalArgumentException("Unknown type");
    }
}
