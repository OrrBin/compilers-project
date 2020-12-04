package solution.visitors;

import ast.AddExpr;
import ast.AndExpr;
import ast.ArrayAccessExpr;
import ast.ArrayLengthExpr;
import ast.AssignArrayStatement;
import ast.AssignStatement;
import ast.BlockStatement;
import ast.BoolAstType;
import ast.ClassDecl;
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
import ast.SubtractExpr;
import ast.SysoutStatement;
import ast.ThisExpr;
import ast.TrueExpr;
import ast.VarDecl;
import ast.Visitor;
import ast.WhileStatement;
import solution.AstNodeUtil;

import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedHashMap;

import static solution.LLVMUtil.getTypeName;

public class VTableVisitor implements Visitor {


    private OutputStream outputStream;
    private AstNodeUtil util;

    public VTableVisitor(OutputStream outputStream, AstNodeUtil util) {
        this.outputStream = outputStream;
        this.util = util;
    }

    @Override
    public void visit(Program program) {
        program.classDecls().forEach(classDecl -> classDecl.accept(this));
    }

    @Override
    public void visit(ClassDecl classDecl) {
        var ancestors = util.getClassHierarchy(classDecl);
        LinkedHashMap<String, MethodDecl> methods = new LinkedHashMap<>();
        LinkedHashMap<String, ClassDecl> methodToImplementingClass = new LinkedHashMap<>();

        for (var ancestor : ancestors) {
            var ancestorMethods = ancestor.methoddecls();
            for (var method : ancestorMethods) {
                if (!methods.containsKey(method.name())) {
                    methods.put(method.name(), method);
                }

                methodToImplementingClass.put(method.name(), ancestor);
            }
        }


        writeVTablePrefix(classDecl.name(), methods.size());
        int i = 0;
        for (var method : methods.entrySet()) {
            writeVTableMethodRow(method.getValue(), methodToImplementingClass.get(method.getValue().name()), i == methods.size() - 1);
            i++;
        }
        writeVTableSuffix();
    }

    private void writeVTableMethodRow(MethodDecl method, ClassDecl implementingClass, boolean isLastMethod) {
        StringBuilder builder = new StringBuilder();

        //Example: i8* bitcast (i32 (i8*, i32)* @Base.set to i8*),
        builder.append("\t")
                .append("i8* bitcast (")
                .append(getTypeName(method.returnType()))
                .append(" (i8*");

        for (var param : method.formals()) {
            builder.append(", ").append(getTypeName(param.type()));
        }

        builder.append(")* ")
                .append("@").append(implementingClass.name()).append(".").append(method.name())
                .append(" to i8*)");

        if (!isLastMethod) {
            builder.append(",");
        }

        builder.append("\n");

        writeToStream(builder);
    }

    private void writeVTablePrefix(String className, int numOfFunctions) {
        StringBuilder builder = new StringBuilder();

        builder.append("@.")
                .append(className)
                .append("_vtable = global [").append(numOfFunctions).append(" x i8*] [")
                .append("\n");

        writeToStream(builder);
    }

    private void writeVTableSuffix() {
        StringBuilder builder = new StringBuilder();

        builder.append("]").append("\n");

        writeToStream(builder);
    }

    private void writeToStream(StringBuilder builder) {
        try {
            outputStream.write(builder.toString().getBytes());
        } catch (IOException e) {
            System.out.println("ERROR: IO exception while writing class vTable to output stream");
            e.printStackTrace();
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
