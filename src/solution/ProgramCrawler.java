package solution;

import ast.AstNode;
import ast.Program;

public class ProgramCrawler {

    private Program program;

    public ProgramCrawler(Program program) {
        this.program = program;
    }

    //TODO: Orr
    public <T extends AstNode> T findAncestor(int lineNumber, Class<T> clazz) {
        return null;
    }
}
