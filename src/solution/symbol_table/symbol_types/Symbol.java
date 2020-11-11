package solution.symbol_table.symbol_types;

import ast.AstNode;
import solution.VariableType;

import java.util.List;

public class Symbol {
    public String id;
    public AstNode node;


    public Symbol(AstNode astNode) {
        this.node = astNode;
    }
}

