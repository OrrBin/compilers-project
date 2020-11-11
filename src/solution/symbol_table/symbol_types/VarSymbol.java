package solution.symbol_table.symbol_types;

import ast.VariableIntroduction;
import solution.VariableType;

public class VarSymbol extends Symbol {
    public String decl;
    public VariableType kind;

    public VarSymbol(VariableIntroduction varNode, VariableType varType) {
        super(varNode);
        id= varNode.name();
        decl = varNode.type().toString();
        this.kind = varType;
    }

}
