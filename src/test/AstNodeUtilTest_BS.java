package test;

import ast.*;

import java.util.List;

public class AstNodeUtilTest_BS extends AstNodeUtilTest{

    private static final String xmlFileName = "examples/ast/TreeVisitor.java.xml";

    public AstNodeUtilTest_BS(String xmlFileName) {
        super(xmlFileName);
    }

    public void getExtendingClassesTest(){
        ClassDecl visitorClass = prog.classDecls().get(2);
        List<ClassDecl> extendingClasses = astNodeUtil.getExtendingClasses(visitorClass);
        assert extendingClasses.size() == 1;
        assert extendingClasses.get(0).name().equals("MyVisitor");
    }

    public void getClassTest(){
        ClassDecl visitorClass = prog.classDecls().get(2);
        MethodDecl visitorMethod = visitorClass.methoddecls().get(0);
        VarDecl nti = visitorMethod.vardecls().get(0);
        assert astNodeUtil.getClassDeclaration(visitorClass).equals(visitorClass);
        assert astNodeUtil.getClassDeclaration(visitorMethod).equals(visitorClass);
        assert astNodeUtil.getClassDeclaration(nti).equals(visitorClass);
    }

    public static void main(String[] args) {
        AstNodeUtilTest_BS astNodeUtilTestBS = new AstNodeUtilTest_BS(xmlFileName);

        astNodeUtilTestBS.getExtendingClassesTest();
        System.out.println("Test1 passed");
        astNodeUtilTestBS.getClassTest();
        System.out.println("Test2 passed");

        System.out.println("All tests passed");

    }

}
