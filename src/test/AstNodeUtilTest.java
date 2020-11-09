package test;

import ast.*;
import solution.AstNodeUtil;
import solution.SymbolTablesManager;
import solution.VariableType;
import solution.symbol_table.SymbolTableInitVisitor;

import java.io.File;

public class AstNodeUtilTest {

    public static SymbolTablesManager symbolTablesManager = new SymbolTablesManager();
    public static AstNodeUtil astNodeUtil;
    public static Program prog;
    public static final String xmlFileName = "examples/ex1/field.java.xml";

    // TODO check if can add junit dependency

    public static void init(){
        astNodeUtil = new AstNodeUtil(symbolTablesManager);

        AstXMLSerializer xmlSerializer = new AstXMLSerializer();
        prog = xmlSerializer.deserialize(new File(xmlFileName));

        prog.accept(new SymbolTableInitVisitor(symbolTablesManager));
    }

    public static void getExtendingClassesReturnEmptyListTest(){
        ClassDecl exampleClass = prog.classDecls().get(0);
        assert astNodeUtil.getExtendingClasses(exampleClass).size() == 0;
    }

    public static void findVariableTypeTest() throws Exception {
        ClassDecl exampleClass = prog.classDecls().get(0);
        VarDecl xField = exampleClass.fields().get(0);
        VariableType type = null;

        type = astNodeUtil.findVariableType(xField);
        assert type == VariableType.FIELD;

        MethodDecl otherMethod = exampleClass.methoddecls().get(1);
        VarDecl xLocal = otherMethod.vardecls().get(0);
        type = astNodeUtil.findVariableType(xLocal);
        assert type == VariableType.LOCAL;
    }


    public static void main(String[] args) throws Exception {
        AstNodeUtilTest.init();

        AstNodeUtilTest.getExtendingClassesReturnEmptyListTest();
        System.out.println("Test1 passed");
        AstNodeUtilTest.findVariableTypeTest();
        System.out.println("Test2 passed");
        System.out.println("All AstNodeUtilTest's tests passed");

    }

}
