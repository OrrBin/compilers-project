package solution.symbol_table.symbol_types;

import ast.AstNode;
import ast.MainClass;

public class MainClassSymbol extends Symbol{

    public MainClassSymbol(MainClass mainClassNode) {
        super(mainClassNode);
        this.id = mainClassNode.name();

    }
}
