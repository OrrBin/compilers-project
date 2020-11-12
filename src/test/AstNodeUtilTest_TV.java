package test;

import ast.*;
import solution.VariableType;


public class AstNodeUtilTest_TV extends AstNodeUtilTest{

    private static final String xmlFileName = "examples/ast/BinarySearch.java.xml";

    public AstNodeUtilTest_TV(String xmlFileName){
        super(xmlFileName);
    }

    public void getMethodTest(){
        ClassDecl BS = prog.classDecls().get(0);
        MethodDecl startMethod = BS.methoddecls().get(1);

        VarDecl auxLocal = startMethod.vardecls().get(0);
        FormalArg sz = startMethod.formals().get(0);

        assert astNodeUtil.getMethod(auxLocal) == startMethod;
        assert astNodeUtil.getMethod(sz) == startMethod;
    }

    public void findVariableTypeTest() throws Exception {
        ClassDecl BS = prog.classDecls().get(0);
        VarDecl number = BS.fields().get(0);
        VariableType type;

        type = astNodeUtil.findVariableType(number);
        assert type == VariableType.FIELD;

        MethodDecl startMethod = BS.methoddecls().get(1);
        VarDecl auxLocal = startMethod.vardecls().get(0);
        type = astNodeUtil.findVariableType(auxLocal);
        assert type == VariableType.LOCAL;

        FormalArg sz = startMethod.formals().get(0);
        type = astNodeUtil.findVariableType(sz);
        assert type == VariableType.PARAMETER;
    }


    public static void main(String[] args) throws Exception {
        AstNodeUtilTest_TV astNodeUtilTestTV = new AstNodeUtilTest_TV(xmlFileName);

        astNodeUtilTestTV.findVariableTypeTest();
        System.out.println("Test1 passed");
        astNodeUtilTestTV.getMethodTest();
        System.out.println("Test2 passed");

        System.out.println("All tests passed");

    }

}
