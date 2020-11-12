package solution;

import ast.AstNode;
import ast.Program;

public class NaiveProgramCrawler implements ProgramCrawler {

    private Program program;

    public NaiveProgramCrawler(Program program) {
        this.program = program;
    }

    @Override
    public <T extends AstNode> T findByLineNumber(int originalLine, Class<T> clazz) {
        return null;
    }
}
