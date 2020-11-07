package solution;

import ast.*;

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

    // endregion

    // region Classes

    public List<ClassDecl> getExtendingClasses(ClassDecl classDecl) {
        List<ClassDecl> extendingClasses = new ArrayList<>();
        return extendingClasses;
    }

    // endregion

    // region Variables

    public boolean isLocal(VarDecl var){ return true; }
    public boolean isLocal(IdentifierExpr var){ return true; }

    public boolean isParameter(VarDecl var) {
        return true;
    }
    public boolean isParameter(IdentifierExpr var) {
        return true;
    }

    public boolean isField(VarDecl var){
        return true;
    }
    public boolean isField(IdentifierExpr var){
        return true;
    }

    // endregion

    // region Methods
    // endregion

}
