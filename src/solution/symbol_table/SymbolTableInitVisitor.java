package solution.symbol_table;

import ast.*;
import solution.SymbolTablesManager;
import solution.VariableType;
import solution.symbol_table.symbol_table_types.SymbolTable;
import solution.symbol_table.symbol_table_types.SymbolTable4Class;
import solution.symbol_table.symbol_table_types.SymbolTable4Method;
import solution.symbol_table.symbol_types.ClassSymbol;
import solution.symbol_table.symbol_types.MethodSymbol;
import solution.symbol_table.symbol_types.Symbol;
import solution.symbol_table.symbol_types.VarSymbol;

import java.util.Stack;

public class SymbolTableInitVisitor implements Visitor {
    SymbolTablesManager symbolTablesManager;
    Name2AstNodeVisitor name2AstNodeVisitor;
    Stack<AstNode> curScopeStack;

    public SymbolTableInitVisitor(SymbolTablesManager symbolTablesManager,
                                  Name2AstNodeVisitor name2AstNodeVisitor){

        this.symbolTablesManager = symbolTablesManager;
        this.name2AstNodeVisitor =name2AstNodeVisitor;
        curScopeStack = new Stack<>();

    }


    void updateParentTableRef(ClassDecl classDecl){
        SymbolTable classSymbolTable = symbolTablesManager.getEnclosingScope(classDecl);

        var parent = name2AstNodeVisitor.name2AstNode.get(classDecl.superName());
        if(parent == null) {
            //program will be the parent of classDecl
            var programParent = curScopeStack.lastElement();
            classSymbolTable.parentSymbolTable = symbolTablesManager.getEnclosingScope(programParent);
            return;
     }

        SymbolTable4Class parentTable =(SymbolTable4Class) symbolTablesManager.getEnclosingScope(parent);
        if(parentTable == null) {
            parentTable = new SymbolTable4Class(parent);
            symbolTablesManager.setEnclosingScope(parent, parentTable);
        }
        classSymbolTable.parentSymbolTable = parentTable;
        parentTable.childrenSymbolTables.add(classSymbolTable);
    }

    void updateManager4NonDeclScopeAstNodes(AstNode node){
        var declScope = curScopeStack.lastElement();
        SymbolTable formalSymbolTable = symbolTablesManager.getEnclosingScope(declScope);
        symbolTablesManager.setEnclosingScope(node, formalSymbolTable);
    }

    @Override
    public void visit(Program program) {

        //initializing name2AstNodeMap
        name2AstNodeVisitor.visit(program);

        SymbolTable rootTable = new SymbolTable(program);
        symbolTablesManager.setEnclosingScope(program, rootTable);

        //update curScopeStack
        curScopeStack.push(program);

        //Adding main class to rootTable
        MainClass mainClass = program.mainClass();
        Symbol mainClassSymbol = new ClassSymbol(mainClass.name());
        rootTable.addSymbol2Table(mainClassSymbol);
        mainClass.accept(this);

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
        if( classSymbolTable == null) {
            classSymbolTable = new SymbolTable4Class(classDecl);
            symbolTablesManager.setEnclosingScope(classDecl, classSymbolTable);
        }

        //updating parent-child references with parent class
        updateParentTableRef(classDecl);

        //update curScopeStack
        curScopeStack.push(classDecl);

        //fields declarations
        for(VarDecl field: classDecl.fields()) {

            //Creating varSymbol for each field
            Symbol varSymbol = new VarSymbol(field, VariableType.FIELD);
            classSymbolTable.addSymbol2Table(varSymbol);

            field.accept(this);
        }

        //methods declarations
        for(MethodDecl method : classDecl.methoddecls()){

            //Creating methodSymbol for each method
            Symbol methodSymbol = new MethodSymbol(method);
            classSymbolTable.addSymbol2Table(methodSymbol);

            method.accept(this);
        }

        //update curScopeStack
        curScopeStack.pop();
    }

    @Override
    public void visit(MainClass mainClass) {
        updateManager4NonDeclScopeAstNodes(mainClass);

        Statement mainStatement = mainClass.mainStatement();
        //mainStatement != null
        mainStatement.accept(this);
    }

    @Override
    public void visit(MethodDecl methodDecl) {

        SymbolTable methodSymbolTable = new SymbolTable4Method(methodDecl);
        symbolTablesManager.setEnclosingScope(methodDecl, methodSymbolTable);

        //Adding reference to parent table
        var classScopeTable = curScopeStack.lastElement();
        methodSymbolTable.parentSymbolTable = symbolTablesManager.getEnclosingScope(classScopeTable);

        //update curScopeStack
        curScopeStack.push(methodDecl);

        //formal arguments declarations
        for(FormalArg formal : methodDecl.formals()) {
            //Creating VarSymbol for each formal argument
            Symbol formalSymbol = new VarSymbol(formal, VariableType.PARAMETER);
            methodSymbolTable.addSymbol2Table(formalSymbol);

            formal.accept(this);
        }

        //local variables declarations
        for(VarDecl local : methodDecl.vardecls()){
            //Creating VarSymbol for each local var
            Symbol localSymbol = new VarSymbol(local, VariableType.LOCAL);
            methodSymbolTable.addSymbol2Table(localSymbol);

            local.accept(this);
        }

        //statements
        for(Statement statement: methodDecl.body()){
            statement.accept(this);
        }

        //return expression
        Expr retExp = methodDecl.ret();
        if(retExp != null) {retExp.accept(this);}

        //update curScopeStack
        curScopeStack.pop();
    }

    @Override
    public void visit(FormalArg formalArg) {

        updateManager4NonDeclScopeAstNodes(formalArg);
    }

    @Override
    public void visit(VarDecl varDecl) {

        updateManager4NonDeclScopeAstNodes(varDecl);
    }

    @Override
    public void visit(BlockStatement blockStatement) {
        updateManager4NonDeclScopeAstNodes(blockStatement);

        for(Statement statement: blockStatement.statements()){
            statement.accept(this);
        }
    }

    @Override
    public void visit(IfStatement ifStatement) {
        updateManager4NonDeclScopeAstNodes(ifStatement);

        Expr cond = ifStatement.cond();
        if(cond != null) {cond.accept(this);}

        Statement elseCase = ifStatement.elsecase();
        if(elseCase != null) {elseCase.accept(this);}

        Statement thenCase = ifStatement.thencase();
        if(thenCase != null) {thenCase.accept(this);}

    }

    @Override
    public void visit(WhileStatement whileStatement) {
        updateManager4NonDeclScopeAstNodes(whileStatement);

        Expr cond = whileStatement.cond();
        if(cond != null) {cond.accept(this);}

        Statement body = whileStatement.body();
        if(body != null) {body.accept(this);}

    }

    @Override
    public void visit(SysoutStatement sysoutStatement) {
        updateManager4NonDeclScopeAstNodes(sysoutStatement);

        Expr arg = sysoutStatement.arg();
        if(arg != null) {arg.accept(this);}

    }

    @Override
    public void visit(AssignStatement assignStatement) {
        updateManager4NonDeclScopeAstNodes(assignStatement);

        Expr rv = assignStatement.rv();
        if(rv != null) {rv.accept(this);}

    }

    @Override
    public void visit(AssignArrayStatement assignArrayStatement) {
        updateManager4NonDeclScopeAstNodes(assignArrayStatement);

        Expr index = assignArrayStatement.index();
        if(index != null) {index.accept(this);}

        Expr rv = assignArrayStatement.rv();
        if(rv != null) {rv.accept(this);}

    }

    @Override
    public void visit(AndExpr e) {

        updateManager4NonDeclScopeAstNodes(e);
    }

    @Override
    public void visit(LtExpr e) {
        updateManager4NonDeclScopeAstNodes(e);

    }

    @Override
    public void visit(AddExpr e) {
        updateManager4NonDeclScopeAstNodes(e);

    }

    @Override
    public void visit(SubtractExpr e) {
        updateManager4NonDeclScopeAstNodes(e);

    }

    @Override
    public void visit(MultExpr e) {
        updateManager4NonDeclScopeAstNodes(e);

    }

    @Override
    public void visit(ArrayAccessExpr e) {
        updateManager4NonDeclScopeAstNodes(e);

        Expr arrayExpr = e.arrayExpr();
        if(arrayExpr != null) {arrayExpr.accept(this);}

        Expr indexExpr= e.indexExpr();
        if(indexExpr != null) {indexExpr.accept(this);}

    }

    @Override
    public void visit(ArrayLengthExpr e) {
        updateManager4NonDeclScopeAstNodes(e);

        Expr arrayExpr = e.arrayExpr();
        if(arrayExpr != null) {arrayExpr.accept(this);}
    }

    @Override
    public void visit(MethodCallExpr e) {
        updateManager4NonDeclScopeAstNodes(e);

        Expr ownerExpr = e.ownerExpr();
        if(ownerExpr != null) {ownerExpr.accept(this);}

        for(Expr actual : e.actuals()){
            actual.accept(this);
        }

    }

    @Override
    public void visit(IntegerLiteralExpr e) {

        updateManager4NonDeclScopeAstNodes(e);
    }

    @Override
    public void visit(TrueExpr e) {
        updateManager4NonDeclScopeAstNodes(e);

    }

    @Override
    public void visit(FalseExpr e) {
        updateManager4NonDeclScopeAstNodes(e);

    }

    @Override
    public void visit(IdentifierExpr e) {
        updateManager4NonDeclScopeAstNodes(e);

    }

    @Override
    public void visit(ThisExpr e) {
        updateManager4NonDeclScopeAstNodes(e);

    }

    @Override
    public void visit(NewIntArrayExpr e) {
        updateManager4NonDeclScopeAstNodes(e);

        Expr lengthExpr = e.lengthExpr();
        if(lengthExpr != null) {lengthExpr.accept(this);}

    }

    @Override
    public void visit(NewObjectExpr e) {
        updateManager4NonDeclScopeAstNodes(e);

    }

    @Override
    public void visit(NotExpr e) {
        updateManager4NonDeclScopeAstNodes(e);

        Expr expr = e.e();
        if(expr != null) {expr.accept(this);}

    }

    @Override
    public void visit(IntAstType t) {
        updateManager4NonDeclScopeAstNodes(t);

    }

    @Override
    public void visit(BoolAstType t) {
        updateManager4NonDeclScopeAstNodes(t);

    }

    @Override
    public void visit(IntArrayAstType t) {
        updateManager4NonDeclScopeAstNodes(t);

    }

    @Override
    public void visit(RefType t) {
        updateManager4NonDeclScopeAstNodes(t);

    }
}
