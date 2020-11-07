package solution.symbol_table.symbol_table_types;

import ast.AstNode;

import java.util.ArrayList;
import java.util.List;

public class SymbolTable4Class extends SymbolTable {
    public List<SymbolTable> childrenSymbolTables;

    public SymbolTable4Class(AstNode symbolTableScope) {
        super(symbolTableScope);
        childrenSymbolTables = new ArrayList<>();
    }

    public SymbolTable4Class(SymbolTable parentSymbolTable, AstNode symbolTableScope) {
        super(parentSymbolTable, symbolTableScope);
    }

}
