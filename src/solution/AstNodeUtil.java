package solution;

import ast.*;
import solution.symbol_table.symbol_table_types.SymbolTable;

import java.util.ArrayList;
import java.util.List;

public class AstNodeUtil {

    private SymbolTablesManager symbolTablesManager;

    public AstNodeUtil(SymbolTablesManager symbolTablesManager) {
        this.symbolTablesManager = symbolTablesManager;
    }

    // region General

    public MethodDecl getMethod(AstNode astNode){
        return null;
    }

    public ClassDecl getClassDeclaration(AstNode astNode){
        return null;
    }

    public SymbolTable getEnclosingScope(AstNode astNode) {
        return symbolTablesManager.getEnclosingScope(astNode);
    }

        // endregion

    // region Classes

    public List<ClassDecl> getExtendingClasses(ClassDecl classDecl) {
        List<ClassDecl> extendingClasses = new ArrayList<>();
        return extendingClasses;
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
