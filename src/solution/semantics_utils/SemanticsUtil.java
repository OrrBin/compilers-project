package solution.semantics_utils;

import ast.*;
import jflex.base.Pair;
import solution.symbol_table.symbol_table_types.SymbolTable;
import solution.utils.AstNodeUtil;
import solution.utils.LLVMUtil;

import java.util.*;

public class SemanticsUtil {
    AstNodeUtil astNodeUtil;

    public SemanticsUtil(AstNodeUtil astNodeUtil){
        this.astNodeUtil = astNodeUtil;
    }

    public List<ClassDecl> checkAndGetClassHierarchy(AstNode astNode) {
        List<ClassDecl> hierarchy = new ArrayList<>();
        ClassDecl classDecl = astNodeUtil.getClassDeclaration(astNode);
        SymbolTable symbolTable = astNodeUtil.getEnclosingScope(classDecl);
        AstNode scope = symbolTable.symbolTableScope;
        ClassDecl clazz;
        do {
            clazz = (ClassDecl) scope;
            hierarchy.add(clazz);
            symbolTable = symbolTable.parentSymbolTable;
            scope = symbolTable.symbolTableScope;
        }  while (scope instanceof ClassDecl  && (hierarchy.size() >1 && !clazz.name().equals(classDecl.name())));

        //a class inherit itself
        if( hierarchy.size() > 1 && clazz.name().equals(classDecl.name())){
            return null;
        }
        Collections.reverse(hierarchy);
        return hierarchy;
    }

    //region overload
    public boolean hasOverloadingMethod(ClassDecl classDecl) {
        Map<String, Pair<List<FormalArg>, AstType>> methodName2ParamsMap = new HashMap<>();
        Stack<ClassDecl> ancestorClassPath = astNodeUtil.getAncestorStack(classDecl);
        ClassDecl curClass;
        //now curClass is the first super class
        //descending the path and collecting methods

            while (!ancestorClassPath.empty()) {
                curClass = ancestorClassPath.pop();
                var methods = curClass.methoddecls();

                if(overloadInTheSameCLass(curClass)){
                    return true;
                }

                for (var method : methods) {
                    if (methodName2ParamsMap.get(method.name()) != null) {
                        var origFormals = methodName2ParamsMap.get(method.name()).fst;
                        var newFormals = method.formals();
                        if(!areFormalsEqual(origFormals, newFormals)) {
                            return true;
                        }
                        var origRetType = methodName2ParamsMap.get(method.name()).snd;
                        var newRetType = method.returnType();

                        //in case of identical arguments, check for identical return type
                        if(!origRetType.getClass().equals(newRetType.getClass())){
                            return true;
                        }
                    }
                   else{
                       methodName2ParamsMap.put(method.name(), new Pair(method.formals(), method.returnType()));
                    }
                }
            }

        return false;
    }

    private boolean overloadInTheSameCLass(ClassDecl classDecl) {
        Set<String> methodNames = new HashSet<>();
        var methods = classDecl.methoddecls();
        for(var method: methods){
            if(methodNames.contains(method.name())){
                return true;
            }
            methodNames.add(method.name());
        }
        return false;
    }

    private boolean areFormalsEqual(List<FormalArg> origFormals, List<FormalArg> newFormals) {
        int origSize = origFormals.size();
        int newSize = newFormals.size();
        if(origSize != newSize) {return false;}
        for(int i=0; i<origSize; i++){
            var origType = origFormals.get(i).type();
            var newType = newFormals.get(i).type();
            if(!origType.getClass().equals(newType.getClass())) {return false;}
        }
        return true;
    }
    //endregion

    public boolean hasOverridingField(ClassDecl classDecl) {
        Set<String> fieldNames = new HashSet<>();

        Stack<ClassDecl> ancestorClassPath = astNodeUtil.getAncestorStack(classDecl);
        ClassDecl curClass;
        //now curClass is the first super class
        //descending the path and collecting fields

            while (!ancestorClassPath.empty()) {
                curClass = ancestorClassPath.pop();
                var fields = curClass.fields();
                for (var field : fields) {
                    if (fieldNames.contains(field.name())) {
                        return true;
                    }
                    fieldNames.add(field.name());
                }
            }
        return false;
    }
    public Map<String, VarDecl> getFields(ClassDecl classDecl){
        Map<String, VarDecl> fields = new HashMap<>();
        Stack<ClassDecl> ancestorClassPath = astNodeUtil.getAncestorStack(classDecl);
        ClassDecl curClass;
        while (!ancestorClassPath.empty()) {
            curClass = ancestorClassPath.pop();
            var curFields = curClass.fields();
            for (var field : curFields) {
                    fields.put(field.name(), field);
            }
        }
        return fields;
    }

}
