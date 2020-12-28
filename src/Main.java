import ast.AstPrintVisitor;
import ast.AstXMLSerializer;
import ast.Program;
import jflex.base.Pair;
import solution.*;
import solution.symbol_table.SymbolTableInitVisitor;
import solution.symbol_table.SymbolTablePreInitVisitor;
import solution.utils.AstNodeUtil;
import solution.visitors.SemanticsCheckVisitor;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        try {
            var inputMethod = args[0];
            var action = args[1];
            var filename = args[args.length - 2];
            var outfilename = args[args.length - 1];

            Program prog;

            if (inputMethod.equals("parse")) {
                throw new UnsupportedOperationException("TODO - Ex. 4");
            } else if (inputMethod.equals("unmarshal")) {
                AstXMLSerializer xmlSerializer = new AstXMLSerializer();
                prog = xmlSerializer.deserialize(new File(filename));
            } else {
                throw new UnsupportedOperationException("unknown input method " + inputMethod);
            }

            var outFile = new PrintWriter(outfilename);
            try {

                if (action.equals("marshal")) {
                    AstXMLSerializer xmlSerializer = new AstXMLSerializer();
                    xmlSerializer.serialize(prog, outfilename);
                } else if (action.equals("print")) {
                    AstPrintVisitor astPrinter = new AstPrintVisitor();
                    astPrinter.visit(prog);
                    outFile.write(astPrinter.getString());

                } else if (action.equals("semantic")) {

                    // initialize symbol tables
                    OutputStream os = new FileOutputStream(outfilename);
                    SymbolTablesManager manager = null;
                    try {
                        manager = activateSymbolTable(prog);
                    } catch (Exception e) {
                        os.write("ERROR\n".getBytes());
                        return;
                    }

                    AstNodeUtil util = new AstNodeUtil(manager);
                    SemanticsCheckGenerator generator = new SemanticsCheckGenerator(prog, util);

                    generator.generate(os);


                } else if (action.equals("compile")) {
                    // initialize symbol tables
                    SymbolTablesManager manager = activateSymbolTable(prog);

                    AstNodeUtil util = new AstNodeUtil(manager);
                    LLVMGenerator generator = new LLVMGenerator(prog, util);

                    OutputStream os = new FileOutputStream(outfilename);
                    generator.generate(os);

                } else if (action.equals("rename")) {
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

                    // initialize symbol tables
                    SymbolTablesManager manager = activateSymbolTable(prog);

                    // execute renaming
                    AstNodeUtil util = new AstNodeUtil(manager);
                    Renamer renamer = new Renamer(prog, util);
                    renamer.rename(new RenameOpParams(type, originalName, Integer.parseInt(originalLine), newName, isMethod));

                    // export to xml file
                    AstXMLSerializer xmlSerializer = new AstXMLSerializer();
                    xmlSerializer.serialize(prog, outfilename);

                } else {
                    throw new IllegalArgumentException("unknown command line action " + action);
                }
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

    private static SymbolTablesManager activateSymbolTable(Program prog) {
        SymbolTablesManager manager = new SymbolTablesManager();
        var preInitVisitor = new SymbolTablePreInitVisitor(manager);
        prog.accept(preInitVisitor);
        prog.accept(new SymbolTableInitVisitor(manager, preInitVisitor.name2AstNodeMap));
        return manager;
    }

    public static void testMain(String[] args) {
        String[] secMainArgs = new String[4];
        List<Pair<String, String>> simpleTests = new ArrayList<>();

        var Simple = "1_vars/Simple.xml";
        var VarType = "2_vars_type/VarType.java.xml";
        var SimpleExpr = "3_simple_expr/SimpleExpr.java.xml";
        var CompoundExpr = "4_compound_expr/CompoundExpr.java.xml";
        var If = "5_if/If.java.xml";
        var And = "6_and/And.java.xml";
        var Arrays = "7_arrays/Arrays.java.xml";
        var Classes = "8_classes/Classes.xml";

        simpleTests.add(new Pair<>(Simple, "Simple"));
        simpleTests.add(new Pair<>(VarType, "VarType"));
        simpleTests.add(new Pair<>(SimpleExpr, "SimpleExpr"));
        simpleTests.add(new Pair<>(CompoundExpr, "CompoundExpr"));
        simpleTests.add(new Pair<>(If, "If"));
        simpleTests.add(new Pair<>(And, "And"));
        simpleTests.add(new Pair<>(Arrays, "Arrays"));
        simpleTests.add(new Pair<>(Classes, "Classes"));

        String fileNamePrefix = "examples/ex2/simple/";
        String outFileNamePrefix = "ex2_temp_out/";

        secMainArgs[0] = "unmarshal";
        secMainArgs[1] = "compile";

        for (var item : simpleTests) {
            secMainArgs[2] = fileNamePrefix + item.fst;
            secMainArgs[3] = outFileNamePrefix + item.snd + ".ll";

            main(secMainArgs);
        }
    }
}
