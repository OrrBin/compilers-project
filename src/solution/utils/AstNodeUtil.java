package solution.utils;

import ast.*;
import solution.RenameOpParams;
import solution.SymbolTablesManager;
import solution.VariableType;
import solution.symbol_table.symbol_table_types.SymbolTable;
import solution.symbol_table.symbol_table_types.SymbolTable4Class;
import solution.symbol_table.symbol_table_types.SymbolTable4Prog;
import solution.symbol_table.symbol_types.SymbolKey;
import solution.symbol_table.symbol_types.SymbolKeyType;

import java.util.*;

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

    //isMethod == true if we want to get methods by heir order
    //isMethod == false if we want to get fields by heir order

    public Stack<ClassDecl> getAncestorStack(ClassDecl classDecl) {
        Stack<ClassDecl> ancestorClassPath = new Stack<>();
        ancestorClassPath.push(classDecl);
        ClassDecl curClass;
        var parentClass = getEnclosingScope(classDecl).parentSymbolTable.symbolTableScope;

        //climb up the ancestor tree till reaching root
        while(parentClass != null){
            if(parentClass instanceof Program) {break;}

            curClass = (ClassDecl)parentClass;
            ancestorClassPath.push(curClass);
            parentClass = getEnclosingScope(curClass).parentSymbolTable.symbolTableScope;
        }
        return ancestorClassPath;
    }

    public Map<Integer, String> getByHeirOrder(ClassDecl classDecl, boolean isMethodCalling) {
        Map<Integer, String> Order2Names = new HashMap<>();
        Stack<ClassDecl> ancestorClassPath =  getAncestorStack(classDecl);
        ClassDecl curClass;
        //now curClass is the first super class
        //descending the path and collecting methods


        if(isMethodCalling) {
            int methodCnt = -1;
            while (!ancestorClassPath.empty()) {
                curClass = ancestorClassPath.pop();
                var methods = curClass.methoddecls();
                for (var method : methods) {
                    if (Order2Names.containsValue(method.name())) {
                        continue;
                    }
                    methodCnt++;
                    Order2Names.put(methodCnt, method.name());
                }
            }
        }
        else{
            int fieldCnt = 0;
            while (!ancestorClassPath.empty()) {
                curClass = ancestorClassPath.pop();
                var fields = curClass.fields();
                for (var field : fields) {
                    if (Order2Names.containsValue(field.name())) {
                        continue;
                    }
                    Order2Names.put(fieldCnt, field.name());
                    fieldCnt += LLVMUtil.getTypeSize(field.type());
                }
            }
        }
        return Order2Names;
    }

    public int getMethodIdxInVtable(MethodCallExpr methodCall) {
        var classDecl = getClassDeclaration(methodCall);
        var methodsByHeirOrder = getByHeirOrder(classDecl, true);
        var methodsKeySet = methodsByHeirOrder.keySet();
        for (var methodIdx : methodsKeySet) {
            if (methodsByHeirOrder.get(methodIdx).equals(methodCall.methodId())) {
                return methodIdx;
            }
        }
        return 0;
    }

    public int getNumOfMethods(ClassDecl classDecl) {
        Map<Integer, String> methodNames = getByHeirOrder(classDecl, true);
        return methodNames.size();
    }

    public int getFieldIdxInObjAlloc(AstNode curScope, String fieldName) {
        var classDecl = getClassDeclaration(curScope);
        var fieldsByHeirOrder = getByHeirOrder(classDecl, false);
        var fieldKeySet = fieldsByHeirOrder.keySet();
        for (var fieldIdx : fieldKeySet) {
            if (fieldsByHeirOrder.get(fieldIdx).equals(fieldName)) {
                return fieldIdx;
            }
        }
        return 0;
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

    boolean nodeFoundByLineNum(MethodDecl nodeToCheck, int lineNumber, String name) {
        var nodeLineNum = nodeToCheck.lineNumber;
        return (nodeLineNum != null) && (nodeLineNum == lineNumber) && nodeToCheck.name().equals(name);
    }
    boolean nodeFoundByLineNum(VariableIntroduction nodeToCheck, int lineNumber, String name) {
        var nodeLineNum = nodeToCheck.lineNumber;
        return (nodeLineNum != null) && (nodeLineNum == lineNumber) && nodeToCheck.name().equals(name);
    }

    //if retVal == null then program input is incorrect
    public AstNode findByLineNumber(Program program, RenameOpParams op, boolean isMethod) {
        List<ClassDecl> classes = program.classDecls();
        int lineNumber = op.originalLine;
        //go over all program classes' symbol tables
        for (var c : classes) {

            SymbolTable cSymbolTable = symbolTablesManager.getEnclosingScope(c);
            var cSymTableEntries = cSymbolTable.entries;
            var cSymbolsKeySet = cSymTableEntries.keySet();

            // go over all symbols in class symbol table
            for (var symbol : cSymbolsKeySet) {
                AstNode cSymbolNode = cSymTableEntries.get(symbol).node;

                //check for field or method
                if(cSymbolNode instanceof MethodDecl && isMethod){
                    if (nodeFoundByLineNum((MethodDecl) cSymbolNode, lineNumber, op.originalName)) {
                        return cSymbolNode;
                    }
                }
                else if(cSymbolNode instanceof VariableIntroduction && !isMethod){
                    if (nodeFoundByLineNum((VariableIntroduction) cSymbolNode, lineNumber, op.originalName)) {
                        return cSymbolNode;
                    }
                }
                //check for formal arg/ local inside a method
                else if (cSymbolNode.getClass() == MethodDecl.class) {

                    SymbolTable mSymbolTable = symbolTablesManager.getEnclosingScope(cSymbolNode);
                    var mSymTableEntries = mSymbolTable.entries;
                    var mSymbolsKeySet = mSymTableEntries.keySet();

                    //go over all symbols in method symbol table
                    for (var symbolInMethod : mSymbolsKeySet) {
                        AstNode mSymbolNode = mSymTableEntries.get(symbolInMethod).node;
                        if(mSymbolNode instanceof VariableIntroduction)
                            if (nodeFoundByLineNum((VariableIntroduction) mSymbolNode, lineNumber, op.originalName)) {
                                return mSymbolNode;}
                    }
                }

            }

        }

        //if we're here then the program input is incorrect
        throw new IllegalArgumentException("Probably no line number");
    }

    // endregion


    /* find class inhering hierarchy (including the current class).
    the first element in ret is the first superclass.
    the last element is the current class.
     */
    public List<ClassDecl> getClassHierarchy(AstNode astNode) {
        List<ClassDecl> hierarchy = new ArrayList<>();
        ClassDecl classDecl = getClassDeclaration(astNode);
        SymbolTable symbolTable = getEnclosingScope(classDecl);
        AstNode scope = symbolTable.symbolTableScope;

        while (scope instanceof ClassDecl ) {
            ClassDecl clazz = (ClassDecl) scope;
            hierarchy.add(clazz);
            symbolTable = symbolTable.parentSymbolTable;
            scope = symbolTable.symbolTableScope;
        }

        Collections.reverse(hierarchy);
        return hierarchy;
    }

    public AstNode getDeclFromName(SymbolKeyType symbolKeyType, String name, AstNode startingAstNode){

        var symbolKey = new SymbolKey(name, symbolKeyType);
        var symbolTableScope = getEnclosingScope(startingAstNode);
        var entries = symbolTableScope.entries;

        while (!entries.containsKey(symbolKey)){
            symbolTableScope = symbolTableScope.parentSymbolTable;
            if(symbolTableScope == null) {
                return null;
            }
            entries = symbolTableScope.entries;
        }

        //found declaration scope
        return entries.get(symbolKey).node;
    }

    //temporary. TODO: refactor
    public ClassDecl getClassDeclFromId(Expr e, String classId) {
        var curSymbolTable = getEnclosingScope(e);
        var parentSymbolTable = curSymbolTable.parentSymbolTable;
        ClassDecl classDecl = null;

        while(parentSymbolTable != null){
            curSymbolTable = parentSymbolTable;
            parentSymbolTable = curSymbolTable.parentSymbolTable;
        }
        //program's symbolTable
        var progSymbolTable = (SymbolTable4Prog)curSymbolTable;
        var entries = progSymbolTable.entries;
        var keySet = entries.keySet();

        //go over all symbols (classes) to locate classId
        for(SymbolKey symbolKey : keySet){
            if(!symbolKey.name.equals(classId)){ continue;}
            classDecl = (ClassDecl) entries.get(symbolKey).node;
        }
        return classDecl;
    }

    public boolean isSubClass(String superClassName, String extendingClassName) {
        var classDeclarations = getClassDeclarations();
        var superClassDeclOptional = classDeclarations.stream().filter(classDecl -> classDecl.name().equals(superClassName)).findFirst();
        if(superClassDeclOptional.isEmpty())
            throw new IllegalArgumentException("Could not find class named " + superClassName);

        ClassDecl superClassDecl = superClassDeclOptional.get();
        var extendingClasses = getExtendingClasses(superClassDecl);

        return extendingClasses.stream().anyMatch(classDecl -> classDecl.name().equals(extendingClassName));
    }

    public List<ClassDecl> getClassDeclarations() {
        Program program = null;
        try {
            program = symbolTablesManager.getProgram();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return program.classDecls();
    }
}

