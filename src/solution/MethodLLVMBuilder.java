package solution;

public class MethodLLVMBuilder {

    private StringBuilder declarationBuilder = new StringBuilder();
    private StringBuilder bodyBuilder = new StringBuilder();

    public MethodLLVMBuilder appendBodyLine(String str) {
        bodyBuilder.append("\t").append(str).append("\n");
        return this;
    }

    public MethodLLVMBuilder appendBody(String str) {
        bodyBuilder.append(str);
        return this;
    }

    public MethodLLVMBuilder appendDeclaration(String str) {
        declarationBuilder.append(str);
        return this;
    }

    @Override
    public String toString() {
        return declarationBuilder.toString() + bodyBuilder.toString();
    }

    public void clear() {
        bodyBuilder.delete(0, bodyBuilder.length());
        declarationBuilder.delete(0, declarationBuilder.length());
    }
}
