package test;

import ast.AstPrintVisitor;
import ast.AstXMLSerializer;
import ast.Program;
import solution.AstNodeUtil;
import solution.RenameOpParams;
import solution.Renamer;
import solution.SymbolTablesManager;
import solution.symbol_table.SymbolTableInitVisitor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.List;
import java.util.stream.Collectors;


//  unmarshal rename var x 10 y  examples/ex1/field_renamed.java.xml examples/ex1/field.java.xml examples/ex1/field_renamed_res.java.xml
public class RenameTest {

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

        var filename = "examples/ast/Factorial.java.xml";
        var outFileName = "examples/ast/Factorial_renamed_res.java.xml";
        var type = "method";
        var originalName = "ComputeFac";
        var originalLine = 19;
        var newName = "OR_HABAT";

        testRename(filename, outFileName, type, originalName, originalLine, newName);
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

                prog.accept(new SymbolTableInitVisitor(manager));
                AstNodeUtil util = new AstNodeUtil(manager);
                Renamer renamer = new Renamer(prog, util);
                renamer.rename(new RenameOpParams(type, originalName, originalLine, newName, isMethod));

                AstPrintVisitor astPrintVisitor = new AstPrintVisitor();
                prog.accept(astPrintVisitor);

                String renamedStr = astPrintVisitor.getString();

                List<String> originalLines = originalStr.lines().collect(Collectors.toList());
                List<String> renamedLines = renamedStr.lines().collect(Collectors.toList());

                int maxLineLength = 0;
                for (String line : originalLines) {
                    int lineLength = 0;
                    for (char c : line.toCharArray()) {
                        if (c == '\t') {
                            lineLength += 4;
                        } else {
                            lineLength += 1;
                        }
                    }
                    if (lineLength > maxLineLength) {
                        maxLineLength = lineLength;
                    }
                }

                int linesNum = originalLines.size();

                System.out.println(String.format("Renaming '%s' in line %d to '%s'", originalName, originalLine, newName));
                System.out.println("====================================================================================");

                for (int i = 0; i < linesNum; i++) {
                    int lineLength = 0;
                    for (char c : originalLines.get(i).toCharArray()) {
                        if (c == '\t') {
                            lineLength += 4;
                        } else {
                            lineLength += 1;
                        }
                    }
                    String whitespace = " ".repeat(maxLineLength - lineLength);
                    System.out.println(String.format("%02d", i+1) + ". " + originalLines.get(i) + whitespace + "   |   " + renamedLines.get(i));
                }


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
