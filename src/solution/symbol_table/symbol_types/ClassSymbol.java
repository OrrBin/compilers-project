package solution.symbol_table.symbol_types;

import ast.ClassDecl;

public class ClassSymbol extends Symbol {
    public ClassSymbol(ClassDecl classNode) {
        id = classNode.name();
    }

    public ClassSymbol(String id){
        this.id = id;
    }
}
