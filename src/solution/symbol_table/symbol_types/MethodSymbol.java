package solution.symbol_table.symbol_types;

import ast.MethodDecl;

import java.util.List;

public class MethodSymbol extends Symbol {
    public List<String> inputDecls;
    public String outputDecl;

    public MethodSymbol(MethodDecl methodNode){
        id = methodNode.name();
    }

}
