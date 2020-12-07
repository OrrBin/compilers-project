package solution.symbol_table.symbol_table_types;

import ast.AstNode;
import ast.ClassDecl;
import ast.MethodDecl;
import solution.symbol_table.symbol_types.Symbol;
import solution.symbol_table.symbol_types.SymbolKey;
import solution.symbol_table.symbol_types.SymbolKeyType;


import java.util.HashMap;
import java.util.Map;

public abstract class SymbolTable {
    public Map<SymbolKey, Symbol> entries;
    public SymbolTable parentSymbolTable;
    public AstNode symbolTableScope;


    public SymbolTable(AstNode symbolTableScope)
    {
        this.symbolTableScope = symbolTableScope;
        entries = new HashMap<>();
    }


    private SymbolKeyType findType(Symbol symbol) {
        var symbolNode = symbol.node;
        //symbol can only be a method or a field
        if(symbolNode instanceof MethodDecl) {return SymbolKeyType.METHOD;}
        else if(symbolNode instanceof ClassDecl) {return SymbolKeyType.CLASS;}
        return SymbolKeyType.VAR;
    }

    public void addSymbol2Table(Symbol symbol) {
        String id = symbol.id;
        SymbolKeyType type = findType(symbol);
        SymbolKey symbolKey = new SymbolKey(id, type);
        entries.put(symbolKey, symbol);
    }

}
