package test;

import ast.*;

import java.util.List;

public class AstNodeUtilTest_TV extends AstNodeUtilTest{

    private static final String xmlFileName = "examples/ast/TreeVisitor.java.xml";

    public AstNodeUtilTest_TV(String xmlFileName) {
        super(xmlFileName);
    }

    public void getExtendingClassesTest(){
        ClassDecl visitorClass = prog.classDecls().get(2);
        List<ClassDecl> extendingClasses = astNodeUtil.getExtendingClasses(visitorClass);
        assert extendingClasses.size() == 1;
        assert extendingClasses.get(0).name().equals("MyVisitor");
    }

    public void getMethodSuperClassDeclaration(){
        ClassDecl visitorClass = prog.classDecls().get(2);
        List<ClassDecl> extendingClasses = astNodeUtil.getExtendingClasses(visitorClass);
        ClassDecl myVisitorClass = extendingClasses.get(0);
        MethodDecl visitMethod = myVisitorClass.methoddecls().get(0);
        ClassDecl superClassDeclaration = astNodeUtil.getMethodSuperClassDeclaration(visitMethod);
        assert superClassDeclaration.name().equals("Visitor");
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
        AstNodeUtilTest_TV astNodeUtilTestBS = new AstNodeUtilTest_TV(xmlFileName);

        astNodeUtilTestBS.getExtendingClassesTest();
        System.out.println("Test1 passed");
        astNodeUtilTestBS.getClassTest();
        System.out.println("Test2 passed");
        astNodeUtilTestBS.getMethodSuperClassDeclaration();
        System.out.println("Test3 passed");

        System.out.println("All tests passed");

    }

}
