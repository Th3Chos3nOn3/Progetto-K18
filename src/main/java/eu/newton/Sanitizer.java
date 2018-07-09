package eu.newton;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Sanitizer {

    private static final Pattern SPACE = Pattern.compile(" ");
    private static final Pattern BRACE_PLUS = Pattern.compile("\\(\\+");
    private static final Pattern BRACE_MINUS = Pattern.compile("\\(-");
    private static final Pattern ENCLOSED_X = Pattern.compile("([+\\-*/^])\\(x\\)");
    private static final Pattern ENCLOSED_CONSTANT = Pattern.compile("([^a-zA-Z0-9])\\((\\d+(\\.\\d+)?)\\)");

    private static final Pattern BEGINNING_ENCLOSED_CONSTANT = Pattern.compile("^\\((\\d+(\\.\\d+)?)\\)");


    public static String simplify(String input) {
        input = SPACE.matcher(input).replaceAll("");

        char first = input.charAt(0);

        if (first == '+') {
            input = input.substring(1);
        } else if (first == '-') {
            input = 0 + input;
        }

        input = BRACE_PLUS.matcher(input).replaceAll("(");
        input = BRACE_MINUS.matcher(input).replaceAll("(0-");

        Matcher x = ENCLOSED_X.matcher(input);

        while (x.find()) {
            input = x.replaceAll("$1x");
            x = ENCLOSED_X.matcher(input);
        }

        Matcher constant = ENCLOSED_CONSTANT.matcher(input);

        while (constant.find()) {
            input = constant.replaceAll("$1$2");
            constant = ENCLOSED_CONSTANT.matcher(input);
        }

        input = BEGINNING_ENCLOSED_CONSTANT.matcher(input).replaceAll("$1");

        return input;
    }
}
