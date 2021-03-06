package solution;

import ast.AstNode;
import ast.Program;
import solution.symbol_table.SymbolTableInitVisitor;
import solution.symbol_table.symbol_table_types.SymbolTable;

import java.util.HashMap;
import java.util.Map;


// TODO implement this class after SymbolicTable interface is published

public class SymbolTablesManager {

    Map<AstNode, SymbolTable> ast2SymbolicTable = new HashMap<>();

    /**
     * get reference to symbol table of enclosing scope
     **/
    public SymbolTable getEnclosingScope(AstNode astNode) {
        return ast2SymbolicTable.get(astNode);
    }

    /**
     * set reference to symbol table of enclosing scope
     **/
    public void setEnclosingScope(AstNode astNode, SymbolTable symbolTable) {
        ast2SymbolicTable.put(astNode, symbolTable);
    }

    public Program getProgram() throws Exception {
        var programOptional = ast2SymbolicTable.entrySet().stream().filter(astNodeSymbolTableEntry -> astNodeSymbolTableEntry.getKey() instanceof Program).findFirst();
        if(programOptional.isEmpty()) {
            throw new Exception("No program scope in SymbolTablesManager found");
        }

        return (Program) programOptional.get().getValue().symbolTableScope;
    }

}
