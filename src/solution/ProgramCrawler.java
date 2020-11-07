package solution;

import ast.AstNode;
import ast.Program;
import ast.VarDecl;

//TODO: Orr
public interface ProgramCrawler {


    public <T extends AstNode> T findAncestor(int lineNumber, Class<T> clazz);

    public <T extends AstNode> T findByLineNumber(int originalLine, Class<T> varDeclClass);
}
