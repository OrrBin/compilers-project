package test;

import ast.AstPrintVisitor;
import ast.AstXMLSerializer;
import ast.Program;
import solution.RenameOpParams;
import solution.Renamer;
import solution.SymbolTablesManager;
import solution.symbol_table.SymbolTableInitVisitor;
import solution.symbol_table.SymbolTablePreInitVisitor;
import solution.utils.AstNodeUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;


//  unmarshal rename var x 10 y  examples/ex1/field_renamed.java.xml examples/ex1/field.java.xml examples/ex1/field_renamed_res.java.xml
public class RenameTest {
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";

    public static void main(String[] args) {
//        var filename = "examples/ex1/method.java.xml";
//        var outFileName = "examples/ex1/method_renamed_res.java.xml";

//        var filename = "examples/ex1/method.java.xml";
//        var outFileName = "examples/ex1/method_renamed_res.java.xml";
//        var type = "var";
//        var originalName = "e";
//        var originalLine = 10;
//        var newName = "OR_HABAT";

//        var filename = "examples/ex1/field.java.xml";
//        var outFileName = "examples/ex1/field_renamed_res.java.xml";
//        var type = "var";
//        var originalName = "x";
//        var originalLine = 17;
//        var newName = "OR_HABAT";

//        var filename = "examples/ast/Factorial.java.xml";
//        var outFileName = "examples/ast/Factorial_renamed_res.java.xml";
//        var type = "method";
//        var originalName = "ComputeFac";
//        var originalLine = 19;
//        var newName = "OR_HABAT";

//        var filename = "examples/ast/TreeVisitor.java.xml";
//        var outFileName = "examples/ast/TreeVisitor_renamed_res.java.xml";
//        var type = "method";
//        var originalName = "visit";
//        var originalLine = 382;
//        var newName = "OR_HABAT";


        var inputMethod = args[0];
        var action = args[1];
        var type = args[2];
        var originalName = args[3];
        int originalLine = Integer.parseInt(args[4]);
        var newName = args[5];
        var filename = args[args.length - 2];
        var outfilename = args[args.length - 1];

        testRename(filename, outfilename, type, originalName, originalLine, newName);
    }

    public static void testRename(String fileName, String outFileName, String type,
                                  String originalName, int originalLine, String newName) {
        try {
            boolean isMethod = type.equals("method");

            Program prog;
            AstXMLSerializer xmlSerializer = new AstXMLSerializer();
            prog = xmlSerializer.deserialize(new File(fileName));
            AstPrintVisitor originalAstPrintVisitor = new AstPrintVisitor();
            prog.accept(originalAstPrintVisitor);
            String originalStr = originalAstPrintVisitor.getString();

            var outFile = new PrintWriter(outFileName);
            try {
                SymbolTablesManager manager = new SymbolTablesManager();

                var preInitVisitor = new SymbolTablePreInitVisitor(manager);
                prog.accept(preInitVisitor);
                prog.accept(new SymbolTableInitVisitor(manager, preInitVisitor.name2AstNodeMap));
                AstNodeUtil util = new AstNodeUtil(manager);
                Renamer renamer = new Renamer(prog, util);
                renamer.rename(new RenameOpParams(type, originalName, originalLine, newName, isMethod));

                AstPrintVisitor astPrintVisitor = new AstPrintVisitor();
                prog.accept(astPrintVisitor);

                String renamedStr = astPrintVisitor.getString();
                Util.writeDiff(originalStr, renamedStr);
                outFile.write(renamedStr);

            } finally {
                outFile.flush();
                outFile.close();
            }

        } catch (
                FileNotFoundException e) {
            System.out.println("Error reading file: " + e);
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("General error: " + e);
            e.printStackTrace();
        }
    }
}
