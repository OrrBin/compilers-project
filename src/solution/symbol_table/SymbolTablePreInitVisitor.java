package solution.symbol_table;

import ast.AddExpr;
import ast.AndExpr;
import ast.ArrayAccessExpr;
import ast.ArrayLengthExpr;
import ast.AssignArrayStatement;
import ast.AssignStatement;
import ast.AstNode;
import ast.BlockStatement;
import ast.BoolAstType;
import ast.ClassDecl;
import ast.Expr;
import ast.FalseExpr;
import ast.FormalArg;
import ast.IdentifierExpr;
import ast.IfStatement;
import ast.IntArrayAstType;
import ast.IntAstType;
import ast.IntegerLiteralExpr;
import ast.LtExpr;
import ast.MainClass;
import ast.MethodCallExpr;
import ast.MethodDecl;
import ast.MultExpr;
import ast.NewIntArrayExpr;
import ast.NewObjectExpr;
import ast.NotExpr;
import ast.Program;
import ast.RefType;
import ast.Statement;
import ast.SubtractExpr;
import ast.SysoutStatement;
import ast.ThisExpr;
import ast.TrueExpr;
import ast.VarDecl;
import ast.Visitor;
import ast.WhileStatement;
import solution.SymbolTablesManager;
import solution.VariableType;
import solution.symbol_table.symbol_table_types.SymbolTable;
import solution.symbol_table.symbol_table_types.SymbolTable4Class;
import solution.symbol_table.symbol_table_types.SymbolTable4Prog;
import solution.symbol_table.symbol_types.ClassSymbol;
import solution.symbol_table.symbol_types.MethodSymbol;
import solution.symbol_table.symbol_types.Symbol;
import solution.symbol_table.symbol_types.VarSymbol;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class SymbolTablePreInitVisitor implements Visitor {
    public SymbolTablesManager symbolTablesManager;
    public Map<String, ClassDecl> name2AstNodeMap;
    public Stack<AstNode> curScopeStack;

    public SymbolTablePreInitVisitor(SymbolTablesManager symbolTablesManager) {

        this.symbolTablesManager = symbolTablesManager;
        name2AstNodeMap = new HashMap<>();
        curScopeStack = new Stack<>();

    }

    void initName2ClassNode(Program program) {
        for (ClassDecl c : program.classDecls()) {
            name2AstNodeMap.put(c.name(), c);
        }
    }

    @Override
    public void visit(Program program) {

        //initializing name2AstNodeMap
        initName2ClassNode(program);

        SymbolTable rootTable = new SymbolTable4Prog(program);
        symbolTablesManager.setEnclosingScope(program, rootTable);

        //update curScopeStack
        curScopeStack.push(program);

        for (ClassDecl c : program.classDecls()) {

            //Adding all program classes to rootTable
            Symbol classSymbol = new ClassSymbol(c);
            rootTable.addSymbol2Table(classSymbol);
            c.accept(this);
        }
    }

    @Override
    public void visit(ClassDecl classDecl) {

        SymbolTable classSymbolTable = symbolTablesManager.getEnclosingScope(classDecl);
        if (classSymbolTable == null) {
            classSymbolTable = new SymbolTable4Class(classDecl);
            symbolTablesManager.setEnclosingScope(classDecl, classSymbolTable);
        }
    }

    @Override
    public void visit(MainClass mainClass) {

    }

    @Override
    public void visit(MethodDecl methodDecl) {

    }

    @Override
    public void visit(FormalArg formalArg) {

    }

    @Override
    public void visit(VarDecl varDecl) {

    }

    @Override
    public void visit(BlockStatement blockStatement) {

    }

    @Override
    public void visit(IfStatement ifStatement) {

    }

    @Override
    public void visit(WhileStatement whileStatement) {

    }

    @Override
    public void visit(SysoutStatement sysoutStatement) {

    }

    @Override
    public void visit(AssignStatement assignStatement) {

    }

    @Override
    public void visit(AssignArrayStatement assignArrayStatement) {

    }

    @Override
    public void visit(AndExpr e) {

    }

    @Override
    public void visit(LtExpr e) {

    }

    @Override
    public void visit(AddExpr e) {

    }

    @Override
    public void visit(SubtractExpr e) {

    }

    @Override
    public void visit(MultExpr e) {

    }

    @Override
    public void visit(ArrayAccessExpr e) {

    }

    @Override
    public void visit(ArrayLengthExpr e) {

    }

    @Override
    public void visit(MethodCallExpr e) {

    }

    @Override
    public void visit(IntegerLiteralExpr e) {

    }

    @Override
    public void visit(TrueExpr e) {

    }

    @Override
    public void visit(FalseExpr e) {

    }

    @Override
    public void visit(IdentifierExpr e) {

    }

    @Override
    public void visit(ThisExpr e) {

    }

    @Override
    public void visit(NewIntArrayExpr e) {

    }

    @Override
    public void visit(NewObjectExpr e) {

    }

    @Override
    public void visit(NotExpr e) {

    }

    @Override
    public void visit(IntAstType t) {

    }

    @Override
    public void visit(BoolAstType t) {

    }

    @Override
    public void visit(IntArrayAstType t) {

    }

    @Override
    public void visit(RefType t) {

    }
}
