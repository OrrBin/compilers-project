package solution;

import ast.AstNode;
import ast.ClassDecl;
import ast.MethodDecl;
import ast.VariableIntroduction;

public class AstNodeUtil {

    SymbolTablesManager symbolTablesManager;

    public AstNodeUtil(SymbolTablesManager symbolTablesManager) {
        this.symbolTablesManager = symbolTablesManager;
    }

    // region General

    public ClassDecl getClassDeclaration(AstNode astNode){
        return null;
    }

    // endregion

    // region Variables

    public MethodDecl getClassMethod(VariableIntroduction var){
        if (isField(var)){
            return null;
        }
        return null;
    }

    public boolean isLocal(VariableIntroduction var){
        return true;
    }

    public boolean isParameter(VariableIntroduction var) {
        return true;
    }

    public boolean isField(VariableIntroduction var){
        return true;
    }

    // endregion


    // region Methods


    // endregion

}
