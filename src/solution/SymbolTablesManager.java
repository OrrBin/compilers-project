package solution;

import ast.AstNode;

import java.util.HashMap;
import java.util.Map;

// TODO implement this class after SymbolicTable interface is published

public class SymbolTablesManager {

    Map<AstNode, SymbolTable> ast2SymbolicTable = new HashMap<>();

    /**
     * reference to symbol table of enclosing scope
     **/
    public SymbolTable getEnclosingScope(AstNode astNode) {
        return ast2SymbolicTable.get(astNode);
    }

    public void setEnclosingScope(AstNode astNode, SymbolTable symbolTable) {
        ast2SymbolicTable.put(astNode, symbolTable);
    }

}
