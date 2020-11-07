package solution;

import ast.AstNode;
import ast.ClassDecl;
import ast.MethodDecl;
import ast.Program;
import ast.VarDecl;
import solution.actions.RenameOp;
import solution.symbol_table.symbol_table_types.SymbolTable;
import solution.visitors.RenameLocalVariableVisitor;
import solution.visitors.RenameParameterVisitor;

import java.util.List;

public class Renamer {

    private final SymbolTablesManager manager;
    private Program prog;
    private ProgramCrawler crawler;
    private List<RenameOp<?>> renameOps;

    public Renamer(Program prog, SymbolTablesManager manager) {
        this.prog = prog;
//        this.crawler = new ProgramCrawler(prog);
        this.manager = manager;
    }

    public void rename(RenameOpParams op) throws Exception {
        if (op.isMethod) {
            renameMethod(op);
        } else {
            renameVariable(op);
        }
    }

    public void renameVariable(RenameOpParams op) throws Exception {
        VariableType variableType = findVariableType(op.originalName, op.originalLine);
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
        SymbolTable table = manager.getEnclosingScope(var);
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
        SymbolTable table = manager.getEnclosingScope(var);
        AstNode node = table.symbolTableScope;
        if (!(node instanceof MethodDecl)) {
            throw new Exception("table.symbolTableScope expected to be of type MethodDecl but was of type : " + node.getClass());
        }

        MethodDecl method = (MethodDecl) node;
        RenameParameterVisitor visitor = new RenameParameterVisitor(op, renameOps);
        method.accept(visitor);
    }

    private void renameField(RenameOpParams op) {
    }


    public void renameMethod(RenameOpParams op) {

    }

    private VariableType findVariableType(String originalName, int originalLine) {
        return VariableType.LOCAL;
    }

    private void executeRenameOps(List<RenameOp<?>> renameOps) {
        renameOps.forEach(RenameOp::rename);
    }
}
