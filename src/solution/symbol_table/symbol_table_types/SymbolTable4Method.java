package solution.symbol_table.symbol_table_types;

import ast.AstNode;

public class SymbolTable4Method extends SymbolTable {

    public SymbolTable4Method(AstNode symbolTableScope) {
        super(symbolTableScope);
    }

    public SymbolTable4Method(SymbolTable parentSymbolTable, AstNode symbolTableScope) {
        super(parentSymbolTable, symbolTableScope);

    }

}
