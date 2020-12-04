package test;

import java.io.Writer;
import java.util.List;
import java.util.stream.Collectors;

public class Util {
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";

    public static void writeDiff(String originalString, String newString) {
        originalString = originalString.replace("\t", "    ");
        newString = newString.replace("\t", "    ");

        List<String> originalLines = originalString.lines().collect(Collectors.toList());
        List<String> newLines = newString.lines().collect(Collectors.toList());

        int maxLineLength = originalLines.stream().map(String::length).max(Integer::compareTo).get();
        int linesNum = originalLines.size();

        System.out.println("====================================================================================");
        for (int i = 0; i < linesNum; i++) {
            int lineLength = 0;
            for (char c : originalLines.get(i).toCharArray()) {
                if (c == '\t') {
                    lineLength += 4;
                } else {
                    lineLength += 1;
                }
            }
            String whitespace = " ".repeat(maxLineLength - lineLength);
            if (originalLines.get(i).equals(newLines.get(i))) {
                System.out.println(String.format("%03d", i + 1) + ". " + fixedLengthString(originalLines.get(i), maxLineLength) +
                        "   |   " + newLines.get(i));
            } else {
                System.out.println(String.format("%03d", i + 1) + ". " + ANSI_RED + fixedLengthString(originalLines.get(i), maxLineLength) +
                        ANSI_RESET + "   |   " + ANSI_GREEN + newLines.get(i) + ANSI_RESET);
            }
        }
    }

    public static String fixedLengthString(String s, int lineLength) {
        String format = "%-" + lineLength + "s";
        return String.format(format, s);
    }
}
