package test;

import ast.AstXMLSerializer;
import ast.Program;
import solution.AstNodeUtil;
import solution.SymbolTablesManager;
import solution.symbol_table.SymbolTableInitVisitor;

import java.io.File;

public abstract class AstNodeUtilTest {
    protected static SymbolTablesManager symbolTablesManager = new SymbolTablesManager();
    protected static AstNodeUtil astNodeUtil;
    protected static Program prog;

    private static String xmlFileName = "examples/ex1/field.java.xml";

    // TODO check if can add junit dependency

    public AstNodeUtilTest(String xmlFileName) {
        this.xmlFileName = xmlFileName;
    }

    public void init(){
        astNodeUtil = new AstNodeUtil(symbolTablesManager);

        AstXMLSerializer xmlSerializer = new AstXMLSerializer();
        prog = xmlSerializer.deserialize(new File(xmlFileName));

        prog.accept(new SymbolTableInitVisitor(symbolTablesManager));
    }

}
