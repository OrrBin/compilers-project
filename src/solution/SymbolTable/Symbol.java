package solution.SymbolTable;

import solution.VariableType;

import java.util.List;

public abstract class Symbol {
    private String id;
}
class varSymbol extends Symbol {
    private String decl;
    private VariableType kind;

}
class methodSymbol extends Symbol {
    private List<String> inputDecls;
    private String outputDecls;
}
