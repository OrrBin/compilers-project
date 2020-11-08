package solution.symbol_table.symbol_table_types;

import ast.AstNode;
import solution.symbol_table.symbol_types.Symbol;

import java.util.HashMap;
import java.util.Map;

public  class SymbolTable {
    public Map<String, Symbol> entries;
    public SymbolTable parentSymbolTable;
    public AstNode symbolTableScope;


    public SymbolTable(AstNode symbolTableScope)
    {
        this.symbolTableScope = symbolTableScope;
        entries = new HashMap<>();
    }


    public void addSymbol2Table(Symbol symbol) {
        String id = symbol.id;
        entries.put(id, symbol);
    }
}
