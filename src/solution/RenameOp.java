package solution;

public class RenameOp {
    public String type;
    public String originalName;
    public int originalLine;
    public String newName;
    public boolean isMethod;

    public RenameOp(String type, String originalName, int originalLine, String newName, boolean isMethod) {
        this.type = type;
        this.originalName = originalName;
        this.originalLine = originalLine;
        this.newName = newName;
        this.isMethod = isMethod;
    }
}
