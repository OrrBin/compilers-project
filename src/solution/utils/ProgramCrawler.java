package solution.utils;

import ast.AstNode;

//TODO: Orr
public interface ProgramCrawler {

    public <T extends AstNode> T findByLineNumber(int originalLine, Class<T> varDeclClass);
}
