package solution.utils;

public class RegisterCounter {

    private int registerCounter = 0;

    public String allocateRegister() {
        String registerName = "%_" + registerCounter;
        registerCounter += 1;
        return registerName;
    }

    public void resetRegisterCounter() {
        registerCounter = 0;
    }

    public String getLastRegister() {
        if (registerCounter > 0 ) {
            return "%_" + (registerCounter - 1);
        }

        throw new IllegalArgumentException("Asked for last register but no register used yet");
    }

    public String getRegister(int offset) {
        if (registerCounter >= offset)
            return "%_" + (registerCounter - offset);

        throw new IllegalArgumentException(String.format("Last register is %d but got offset %d", registerCounter-1, offset));
    }
}
