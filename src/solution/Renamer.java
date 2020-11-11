package solution;

import ast.*;
import solution.actions.RenameOp;
import solution.symbol_table.symbol_table_types.SymbolTable;
import solution.visitors.RenameFieldVariableVisitor;
import solution.visitors.RenameLocalVariableVisitor;
import solution.visitors.RenameMethodVisitor;
import solution.visitors.RenameParameterVisitor;

import java.util.ArrayList;
import java.util.List;

public class Renamer {

    private final AstNodeUtil astNodeUtil;
    private Program prog;
    private ProgramCrawler crawler;
    private List<RenameOp<?>> renameOps;

    public Renamer(Program prog, AstNodeUtil astNodeUtil) {
        this.prog = prog;
//        this.crawler = new ProgramCrawler(prog);
        this.astNodeUtil = astNodeUtil;
    }

    public void rename(RenameOpParams op) throws Exception {
        if (op.isMethod) {
            renameMethod(op);
        } else {
            renameVariable(op);
        }
    }

    public void renameVariable(RenameOpParams op) throws Exception {
        VariableIntroduction var = crawler.findByLineNumber(op.originalLine, VariableIntroduction.class);
        VariableType variableType = astNodeUtil.findVariableType(var);
        switch (variableType) {
            case FIELD:
                renameField(op);
                break;
            case LOCAL:
                renameLocal(op);
                break;
            case PARAMETER:
                renameParameter(op);
                break;
        }
    }

    private void renameLocal(RenameOpParams op) throws Exception {
        VarDecl var = crawler.findByLineNumber(op.originalLine, VarDecl.class);
        SymbolTable table = astNodeUtil.getEnclosingScope(var);
        AstNode node = table.symbolTableScope;
        if (!(node instanceof MethodDecl)) {
            throw new Exception("table.symbolTableScope expected to be of type MethodDecl but was of type : " + node.getClass());
        }

        MethodDecl method = (MethodDecl) node;
        RenameLocalVariableVisitor visitor = new RenameLocalVariableVisitor(op, renameOps);
        method.accept(visitor);
    }

    private void renameParameter(RenameOpParams op) throws Exception {
        VarDecl var = crawler.findByLineNumber(op.originalLine, VarDecl.class);
        SymbolTable table = astNodeUtil.getEnclosingScope(var);
        AstNode node = table.symbolTableScope;
        if (!(node instanceof MethodDecl)) {
            throw new Exception("table.symbolTableScope expected to be of type MethodDecl but was of type : " + node.getClass());
        }

        MethodDecl method = (MethodDecl) node;
        RenameParameterVisitor visitor = new RenameParameterVisitor(op, renameOps);
        method.accept(visitor);
    }

    private void renameField(RenameOpParams op) {
        VarDecl var = crawler.findByLineNumber(op.originalLine, VarDecl.class);
        ClassDecl clazz = astNodeUtil.getClassDeclaration(var); // will find the super class

        List<ClassDecl> classes = new ArrayList<>();
        classes.add(clazz);
        classes.addAll(astNodeUtil.getExtendingClasses(clazz));
        RenameFieldVariableVisitor visitor = new RenameFieldVariableVisitor(op, renameOps, astNodeUtil);

        for (ClassDecl classDecl : classes) {
            classDecl.accept(visitor);
        }
    }

    public void renameMethod(RenameOpParams op) {
        MethodDecl method = crawler.findByLineNumber(op.originalLine, MethodDecl.class);
        ClassDecl clazz = astNodeUtil.getClassDeclaration(method); // will find the super class

        List<ClassDecl> classes = new ArrayList<>();
        classes.add(clazz);
        classes.addAll(astNodeUtil.getExtendingClasses(clazz));
        RenameMethodVisitor visitor = new RenameMethodVisitor(op, renameOps);

        for (ClassDecl classDecl : classes) {
            classDecl.accept(visitor);
        }
    }


    private void executeRenameOps(List<RenameOp<?>> renameOps) {
        renameOps.forEach(RenameOp::rename);
    }
}
