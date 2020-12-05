package solution.utils;

public class LabelCounter {

    private int labelCounter = 0;

    public int allocateLabelNumber() {
        labelCounter += 1;
        return labelCounter-1;
    }

    public void resetRegisterCounter() {
        labelCounter = 0;
    }

    public String getLastLabelNumber() {
        if (labelCounter > 0) {
            return "%_" + (labelCounter - 1);
        }

        throw new IllegalArgumentException("Asked for last label number but no label was used yet");
    }

}
