package test;

import solution.AstNodeUtil;
import solution.SymbolTablesManager;

public class AstNodeUtilTest {

    public static SymbolTablesManager symbolTablesManager = new SymbolTablesManager();
    public static AstNodeUtil astNodeUtil = new AstNodeUtil(symbolTablesManager);

    // TODO ask or how to init the Symbol Tables
    // TODO check if can add junit dependency

    public AstNodeUtilTest(){

    }

    public static void getExtendingClassesTest(){
        // astNodeUtil.getExtendingClasses();
    }

    public static void findVariableTypeTest(){
        // astNodeUtil.findVariableType();
    }

}
