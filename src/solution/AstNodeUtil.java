package solution;

import ast.*;
import solution.symbol_table.symbol_table_types.SymbolTable;
import solution.symbol_table.symbol_table_types.SymbolTable4Class;
import solution.symbol_table.symbol_types.Symbol;

import java.util.*;
import java.util.stream.Collectors;

public class AstNodeUtil {

    private SymbolTablesManager symbolTablesManager;

    public AstNodeUtil(SymbolTablesManager symbolTablesManager) {
        this.symbolTablesManager = symbolTablesManager;
    }

    // region General

    public MethodDecl getMethod(AstNode astNode) {
        SymbolTable symbolTable = symbolTablesManager.getEnclosingScope(astNode);
        AstNode node = symbolTable.symbolTableScope;
        if (!(node instanceof MethodDecl)) {
            throw new RuntimeException("table.symbolTableScope expected to be of type MethodDecl but was of type : " + node.getClass());
        }
        return (MethodDecl) node;
    }

    public ClassDecl getClassDeclaration(AstNode astNode) {
        SymbolTable symbolTable = symbolTablesManager.getEnclosingScope(astNode);
        while (!(symbolTable.symbolTableScope instanceof ClassDecl)) {
            symbolTable = symbolTable.parentSymbolTable;
            if (symbolTable == null) {
                throw new RuntimeException("Couln't find symbolTableScope of ClassDecl for node of type: " + astNode.getClass());
            }
        }

        return (ClassDecl) symbolTable.symbolTableScope;
    }

    public ClassDecl getSuperClassDeclarationOfMethod(MethodDecl methodDecl) {
        ClassDecl classDecl = getClassDeclaration(methodDecl);
        SymbolTable symbolTable = getEnclosingScope(classDecl);
        AstNode scope = symbolTable.symbolTableScope;
        ClassDecl last = null;

        while (scope instanceof ClassDecl) {
            ClassDecl scopeClass = (ClassDecl) scope;
            if (hasMethod(scopeClass, methodDecl)) {
                last = scopeClass;
            }
            symbolTable = symbolTable.parentSymbolTable;
            scope = symbolTable.symbolTableScope;
        }
        return last;
    }

    /** return all classes which inhering @method.
     *  explicitly: find the super class which declare the method and return all classes which
     *             inheriting him **/

    public List<ClassDecl> getFamilyOfMethod(MethodDecl method){
        List<ClassDecl> family = new ArrayList<>();
        ClassDecl superClass = getSuperClassDeclarationOfMethod(method);
        family.add(superClass);
        family.addAll(getExtendingClasses(superClass));
        return family;
    }

    public boolean hasMethod(ClassDecl clazz, MethodDecl methodDecl) {
        return clazz.methoddecls().stream().anyMatch(method -> method.name().equals(methodDecl.name()));
    }

    public SymbolTable getEnclosingScope(AstNode astNode) {
        return symbolTablesManager.getEnclosingScope(astNode);
    }

    // endregion

    // region Classes

    private List<ClassDecl> getExtendingClassesHelper(ClassDecl classDecl) {
        List<ClassDecl> extendingClasses = new ArrayList<>();
        extendingClasses.add(classDecl);
        SymbolTable4Class rootSymbolTable = (SymbolTable4Class) getEnclosingScope(classDecl);
        for (SymbolTable symbolTable : rootSymbolTable.childrenSymbolTables) {
            extendingClasses.addAll(getExtendingClassesHelper((ClassDecl) symbolTable.symbolTableScope));
        }
        return extendingClasses;
    }

    public List<ClassDecl> getExtendingClasses(ClassDecl classDecl) {
        List<ClassDecl> extendingClasses = getExtendingClassesHelper(classDecl);
        extendingClasses.remove(classDecl);
        return extendingClasses;
    }

    // endregion

    // region Variables

    public VariableType findVariableType(VariableIntroduction var) throws Exception {
        if (isLocal(var))
            return VariableType.LOCAL;
        if (isParameter(var))
            return VariableType.PARAMETER;
        if (isField(var))
            return VariableType.FIELD;
        throw new Exception("Can't determine variable type");
    }

    public VariableType findVariableType(IdentifierExpr var) throws Exception {
        if (isLocal(var))
            return VariableType.LOCAL;
        if (isParameter(var))
            return VariableType.PARAMETER;
        if (isField(var))
            return VariableType.FIELD;
        throw new Exception("Can't determine variable type");
    }

    public boolean isLocal(VariableIntroduction var) {
        SymbolTable symbolTable = getEnclosingScope(var);
        if (symbolTable.symbolTableScope instanceof MethodDecl) {
            return ((MethodDecl) symbolTable.symbolTableScope).vardecls().contains(var);
        } else {
            return false;
        }
    }

    public boolean isLocal(IdentifierExpr var) {
        MethodDecl method = (MethodDecl) getEnclosingScope(var).symbolTableScope;
        for (VarDecl localVar : method.vardecls()) {
            if (localVar.name().equals(var.id())) {
                    /* enclosing method contains local variable with same name,
                      since var declarations are at the beginning of the method - the local var overrides the field */
                return true;
            }
        }
        return false;
    }

    public boolean isParameter(VariableIntroduction var) {
        SymbolTable symbolTable = getEnclosingScope(var);
        if (symbolTable.symbolTableScope instanceof MethodDecl) {
            return ((MethodDecl) symbolTable.symbolTableScope).formals().contains(var);
        } else {
            return false;
        }
    }

    public boolean isParameter(IdentifierExpr var) {
        MethodDecl method = (MethodDecl) getEnclosingScope(var).symbolTableScope;
        for (FormalArg formalVar : method.formals()) {
            if (formalVar.name().equals(var.id())) {
                    /* enclosing method contains local variable with same name,
                      since var declarations are at the beginning of the method - the local var overrides the field */
                return true;
            }
        }
        return false;
    }

    public boolean isField(VariableIntroduction var) {
        return !isLocal(var) && !isParameter(var);
    }


    public boolean isField(IdentifierExpr var) {
        return !isLocal(var) && !isParameter(var);
    }

    // endregion

    // region Methods
    // endregion


    // region findByLineNum

    boolean nodeFoundByLineNum(AstNode nodeToCheck, int lineNumber, boolean isMethod) {
        var nodeLineNum = nodeToCheck.lineNumber;
        boolean nodeTypeVerified = (isMethod && nodeToCheck instanceof MethodDecl)  || (!isMethod && !(nodeToCheck instanceof MethodDecl));
        return (nodeLineNum != null) && (nodeLineNum == lineNumber) && nodeTypeVerified;
    }

    //if retVal == null then program input is incorrect
    public AstNode findByLineNumber(Program program, int lineNumber, boolean isMethod) {
        List<ClassDecl> classes = program.classDecls();

        //go over all program classes' symbol tables
        for (var c : classes) {

            SymbolTable cSymbolTable = symbolTablesManager.getEnclosingScope(c);
            var cSymTableEntries = cSymbolTable.entries;
            var cSymbolsKeySet = cSymTableEntries.keySet();

            // go over all symbols in class symbol table
            for (var symbol : cSymbolsKeySet) {
                AstNode cSymbolNode = cSymTableEntries.get(symbol).node;

                //check for field or method
                if (nodeFoundByLineNum(cSymbolNode, lineNumber, isMethod)) {
                    return cSymbolNode;
                }

                //check for formal arg/ local inside a method
                else if (cSymbolNode.getClass() == MethodDecl.class) {

                    SymbolTable mSymbolTable = symbolTablesManager.getEnclosingScope(cSymbolNode);
                    var mSymTableEntries = mSymbolTable.entries;
                    var mSymbolsKeySet = mSymTableEntries.keySet();

                    //go over all symbols in method symbol table
                    for (var symbolInMethod : mSymbolsKeySet) {
                        AstNode mSymbolNode = mSymTableEntries.get(symbolInMethod).node;
                        if (nodeFoundByLineNum(mSymbolNode, lineNumber, isMethod)) {
                            return mSymbolNode;
                        }
                    }
                }

            }

        }

        //if we're here then the program input is incorrect
        throw new IllegalArgumentException("Probably no line number");
    }
}

