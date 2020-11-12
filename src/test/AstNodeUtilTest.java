package test;

import ast.AstXMLSerializer;
import ast.Program;
import solution.AstNodeUtil;
import solution.SymbolTablesManager;
import solution.symbol_table.SymbolTableInitVisitor;

import java.io.File;

public abstract class AstNodeUtilTest {
    protected SymbolTablesManager symbolTablesManager;
    protected AstNodeUtil astNodeUtil;
    protected Program prog;

    private String xmlFileName = "examples/ex1/field.java.xml";

    // TODO check if can add junit dependency

    public AstNodeUtilTest(String xmlFileName) {
        this.xmlFileName = xmlFileName;
        AstXMLSerializer xmlSerializer = new AstXMLSerializer();
        prog = xmlSerializer.deserialize(new File(xmlFileName));
        symbolTablesManager = new SymbolTablesManager();
        prog.accept(new SymbolTableInitVisitor(symbolTablesManager));
        astNodeUtil = new AstNodeUtil(symbolTablesManager);

    }
}
