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


// EXAMPLE: unmarshal rename var x 10 y  examples/ex1/field_renamed.java.xml examples/ex1/field.java.xml examples/ex1/field_renamed_res.java.xml

public class FullFlowTest {
    public static void main(String[] args) {
        try {
            var inputMethod = args[0];
            var filenameExpected = args[args.length - 3];
            var filename = args[args.length - 2];
            var outfilename = args[args.length - 1];

            Program prog;
            Program progExpected;

            if (inputMethod.equals("unmarshal")) {
                AstXMLSerializer xmlSerializer = new AstXMLSerializer();
                prog = xmlSerializer.deserialize(new File(filename));
                progExpected = xmlSerializer.deserialize(new File(filenameExpected));
            } else {
                throw new UnsupportedOperationException("unknown input method " + inputMethod);
            }

            var outFile = new PrintWriter(outfilename);
            try {
                var type = args[2];
                var originalName = args[3];
                var originalLine = args[4];
                var newName = args[5];

                boolean isMethod;
                if (type.equals("var")) {
                    isMethod = false;
                } else if (type.equals("method")) {
                    isMethod = true;
                } else {
                    throw new IllegalArgumentException("unknown rename type " + type);
                }

                SymbolTablesManager manager = new SymbolTablesManager();
                prog.accept(new SymbolTableInitVisitor(manager));
                AstNodeUtil util = new AstNodeUtil(manager);
                Renamer renamer = new Renamer(prog, util);
                renamer.rename(new RenameOpParams(type, originalName, Integer.parseInt(originalLine), newName, isMethod));

                //
                AstPrintVisitor astPrintVisitor = new AstPrintVisitor();
                prog.accept(astPrintVisitor);
                String real = astPrintVisitor.getString();

                AstPrintVisitor astPrintVisitor2 = new AstPrintVisitor();
                progExpected.accept(astPrintVisitor2);
                String expected = astPrintVisitor2.getString();

                System.out.println(real);
                System.out.println("////////////////////////////////////////////////////////////");
                System.out.println(expected);
                assert real.equals(expected);



                System.out.println("Test Passed");

            } finally {
                outFile.flush();
                outFile.close();
            }

        } catch (FileNotFoundException e) {
            System.out.println("Error reading file: " + e);
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("General error: " + e);
            e.printStackTrace();
        }
    }

}
