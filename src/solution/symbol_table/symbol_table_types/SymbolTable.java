package solution.symbol_table.symbol_table_types;

import ast.AstNode;
import solution.symbol_table.symbol_types.Symbol;

import java.util.HashMap;
import java.util.Map;

public abstract class SymbolTable {
    public Map<String, Symbol> entries;
    public SymbolTable parentSymbolTable;
    public AstNode symbolTableScope;
    public int scopeStartingLine;


    public SymbolTable(AstNode symbolTableScope)
    {
        this.symbolTableScope = symbolTableScope;
        this.scopeStartingLine = symbolTableScope.lineNumber;
        entries = new HashMap<>();
    }


    public void addSymbol2Table(Symbol symbol) {
        String id = symbol.id;
        entries.put(id, symbol);
    }
}
