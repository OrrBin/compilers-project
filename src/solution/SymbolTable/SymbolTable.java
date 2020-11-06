package solution.SymbolTable;

import ast.AstNode;

import java.util.Map;

public class SymbolTable {
    private Map<String, Symbol> entries;
    private SymbolTable parentSymbolTable;
    private AstNode symbolTableScope;
    private SymbolTableScopeType scopeType;  // perhaps unuseful

}
