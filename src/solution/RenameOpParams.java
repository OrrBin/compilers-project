package solution;

public class RenameOpParams {
    public String type;
    public String originalName;
    public int originalLine;
    public String newName;
    public boolean isMethod;

    public RenameOpParams(String type, String originalName, int originalLine, String newName, boolean isMethod) {
        this.type = type;
        this.originalName = originalName;
        this.originalLine = originalLine;
        this.newName = newName;
        this.isMethod = isMethod;
    }
}
