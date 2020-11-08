package solution;

import ast.*;
import solution.symbol_table.symbol_table_types.SymbolTable;
import solution.symbol_table.symbol_table_types.SymbolTable4Class;

import java.util.List;
import java.util.stream.Collectors;

public class AstNodeUtil {

    private SymbolTablesManager symbolTablesManager;

    public AstNodeUtil(SymbolTablesManager symbolTablesManager) {
        this.symbolTablesManager = symbolTablesManager;
    }

    // region General

    public MethodDecl getMethod(AstNode astNode){
        SymbolTable symbolTable = symbolTablesManager.getEnclosingScope(astNode);
        AstNode node  = symbolTable.symbolTableScope;
        if (!(node instanceof MethodDecl)) {
            throw new RuntimeException("table.symbolTableScope expected to be of type MethodDecl but was of type : " + node.getClass());
        }
        return (MethodDecl)node;
    }

    public ClassDecl getClassDeclaration(AstNode astNode){
        SymbolTable symbolTable = symbolTablesManager.getEnclosingScope(astNode);
        while (!(symbolTable.symbolTableScope instanceof ClassDecl)){
            symbolTable = symbolTable.parentSymbolTable;
            if (symbolTable == null){
                throw new RuntimeException("Couln't find symbolTableScope of ClassDecl for node of type: " + astNode.getClass());
            }
        }

        return (ClassDecl)symbolTable.symbolTableScope;
    }

    public SymbolTable getEnclosingScope(AstNode astNode) {
        return symbolTablesManager.getEnclosingScope(astNode);
    }

    // endregion

    // region Classes

    public List<ClassDecl> getExtendingClasses(ClassDecl classDecl) {
        SymbolTable4Class rootSymbolTable = (SymbolTable4Class)getEnclosingScope(classDecl);
        return rootSymbolTable.childrenSymbolTables.stream().map(symbolTable -> (ClassDecl)symbolTable.symbolTableScope).collect(Collectors.toList());
    }

    // endregion

    // region Variables

    public VariableType findVariableType(VarDecl var) throws Exception {
        if (isLocal(var)) return VariableType.LOCAL;
        if (isParameter(var)) return VariableType.PARAMETER;
        if (isField(var)) return VariableType.FIELD;
        throw new Exception("Can't determine variable type");
    }

    public VariableType findVariableType(IdentifierExpr var) throws Exception {
        if (isLocal(var)) return VariableType.LOCAL;
        if (isParameter(var)) return VariableType.PARAMETER;
        if (isField(var)) return VariableType.FIELD;
        throw new Exception("Can't determine variable type");
    }

    public boolean isLocal(VarDecl var){ return true; }
    public boolean isLocal(IdentifierExpr var){ return true; }

    public boolean isParameter(VarDecl var) { return true; }
    public boolean isParameter(IdentifierExpr var) { return true; }

    public boolean isField(VarDecl var){ return true;}
    public boolean isField(IdentifierExpr var){ return true; }

    // endregion

    // region Methods
    // endregion

}
