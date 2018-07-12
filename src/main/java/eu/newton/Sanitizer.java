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

    /**
     * Sanitizes the user input: removes blank spaces and redundant brackets, adds 0 when implied.
     *
     * @param function literal representation of user input
     * @return sanitized version of input
     */
    public static String simplify(String function) {
        function = SPACE.matcher(function).replaceAll("");

        char first = function.charAt(0);

        if (first == '+') {
            function = function.substring(1);
        } else if (first == '-') {
            function = 0 + function;
        }

        function = BRACE_PLUS.matcher(function).replaceAll("(");
        function = BRACE_MINUS.matcher(function).replaceAll("(0-");

        Matcher x = ENCLOSED_X.matcher(function);

        while (x.find()) {
            function = x.replaceAll("$1x");
            x = ENCLOSED_X.matcher(function);
        }

        Matcher constant = ENCLOSED_CONSTANT.matcher(function);

        while (constant.find()) {
            function = constant.replaceAll("$1$2");
            constant = ENCLOSED_CONSTANT.matcher(function);
        }

        function = BEGINNING_ENCLOSED_CONSTANT.matcher(function).replaceAll("$1");

        return function;
    }
}
