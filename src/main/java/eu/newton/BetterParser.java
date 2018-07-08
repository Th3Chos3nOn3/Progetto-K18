package eu.newton;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.script.ScriptEngine;
import javax.script.ScriptException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BetterParser {

    private static final Logger logger = LogManager.getLogger(BetterParser.class);

    public Function<BigDecimal, BigDecimal> parse(String input) throws ScriptException {

        if (input.isEmpty()) {
            throw new IllegalArgumentException("REEEEEE");
        }

        logger.trace("ORIGINAL: {}", input);

        input = sanitizeFunction(input);

        logger.trace("UNRET: {}", input);

        List<String> list = getXD(input);

        logger.trace("THE LIST: {}", list);

        list = fixThePow(list);

        logger.trace("POW FIXED: {}", list);

        String function = theTrueParsing(list);

        @SuppressWarnings("unchecked")
        Function<BigDecimal,BigDecimal>  f = (Function<BigDecimal, BigDecimal>) Nashorn.getInstance().eval(String.format("new java.util.function.Function(function(x) %s)", function));

        return f;

    }

    private List<String> fixThePow(List<String> list) {
        ListIterator<String> it = list.listIterator(list.size() - 1);

        String current = it.next();
        String op;


        while (it.hasPrevious()) {
            current = it.previous();

            if (it.hasPrevious()) {
                op = it.previous();

                logger.trace("CURRENT: {}", current);
                logger.trace("OP: {}", op);


                if (op.equals("^")) {
                    String to = it.previous();

                    char open;
                    char close;

                    do {
                        open = current.charAt(0);
                        close = current.charAt(current.length() - 1);

                        if (open == '(' && close == ')') {
                            current = current.substring(1, current.length() - 1);
                        }

                    } while (open == '(' && close == ')');

                    do {
                        open = to.charAt(0);
                        close = to.charAt(to.length() - 1);

                        if (open == '(' && close == ')') {
                            to = to.substring(1, to.length() - 1);
                        }

                    } while (open == '(' && close == ')');

                    if (current.startsWith("java.math.BigDecimal")) {
                        current = "(" + current + ")" + ".doubleValue()";
                    }
                    if (to.startsWith("java.math.BigDecimal")) {
                        to = "(" + to + ")" + ".doubleValue()";
                    }

                    it.set("java.lang.Math.pow" + "(" + to + "," + current + ")");
                    it.next();
                    logger.trace("PREVIOUS: {}", it.next());
                    it.remove();
                    logger.trace("PREVIOUS: {}", it.next());
                    it.remove();
                }
            }
        }

        logger.trace("POW L: {}", list);

        return list;

    }


    private String parseDijkstra(List<String> dijkstraHolyList) {
        logger.trace("");
        logger.trace("PARSING: {}", dijkstraHolyList);

        StringBuilder fuuu = new StringBuilder();
        if (dijkstraHolyList.size() != 1) {
            ListIterator<String> it = dijkstraHolyList.listIterator();

            String current;
            String op;

            while (it.hasNext()) {
                current = it.next();

                if (current.startsWith("java.lang.Math.")) {
                    current = "java.math.BigDecimal.valueOf(" + current + ")";
                    it.set(current);
                    logger.trace("NEW CURRENT: {}", current);
                }

                if (it.hasNext()) {
                    op = it.next();

                    logger.trace("CURRENT: {}", current);
                    logger.trace("OP: {}", op);



                    if (op.equals("*") || op.equals("/")) {
                        String to = it.next();
                        it.set("(" + current + replaceBigMethods(op) + to + ")");
                        it.previous();
                        logger.trace("PREVIOUS: {}", it.previous());
                        it.remove();
                        logger.trace("PREVIOUS: {}", it.previous());
                        it.remove();
                    } else if (op.equals("^")) {
                        String to = it.next();

                        char open;
                        char close;

                        do {
                            open = current.charAt(0);
                            close = current.charAt(current.length() - 1);

                            if (open == '(' && close == ')') {
                                current = current.substring(1, current.length() - 1);
                            }

                        } while (open == '(' && close == ')');

                        do {
                            open = to.charAt(0);
                            close = to.charAt(to.length() - 1);

                            if (open == '(' && close == ')') {
                                to = to.substring(1, to.length() - 1);
                            }

                        } while (open == '(' && close == ')');

                        if (current.startsWith("java.math.BigDecimal")) {
                            current = "(" + current + ")" + ".doubleValue()";
                        }
                        if (to.startsWith("java.math.BigDecimal")) {
                            to = "(" + to + ")" + ".doubleValue()";
                        }

                        it.set("java.lang.Math.pow" + "(" + current + "," + to + ")");
                        it.previous();
                        logger.trace("PREVIOUS: {}", it.previous());
                        it.remove();
                        logger.trace("PREVIOUS: {}", it.previous());
                        it.remove();
                    }
                }
            }

            logger.trace("DHL: {}", dijkstraHolyList);


            dijkstraHolyList.forEach(s -> {
                if (s.length() == 1) {
                    fuuu.append(replaceBigMethods(s));
                } else {
                    fuuu.append('(').append(s).append(')');
                }
            });

        } else {
            fuuu.append(dijkstraHolyList.get(0));
        }

        String ffs;

        char c;
        int s = 0;
        for (int i = 0; i < fuuu.toString().length(); i++) {
            c = fuuu.toString().charAt(i);
            if (c != '(') {
                s = i;
                break;
            }
        }

        if (fuuu.toString().substring(s).startsWith("java.lang.Math.")) {
            ffs = "java.math.BigDecimal.valueOf(" + fuuu.toString() + ")";
        } else {
            ffs = fuuu.toString();
        }

        logger.trace("DONE: {}", ffs);
        logger.trace("");

        return ffs;
    }


    private int getGroupIndex(String s) {
        logger.trace("Index token of: {}", s);

        char c = s.charAt(0);

        if (c == '*' || c == '/' || c == '+' || c == '-' || c == '^' || c == 'x') {
            return 1;
        }

        if (Character.isDigit(c)) {
            for (int i = 1; i < s.length(); i++) {
                char d = s.charAt(i);
                if (!Character.isDigit(d) && d != '.') {
                    return i;
                }
            }
            return s.length();
        }

        if (Character.isDigit(c) || c == 'x') {

            int index = 0;

            for (int i = 0; i < s.length(); i++) {
                c = s.charAt(i);
                if (c == '^') {
                    return i;
                }
                if (c == '+' || c == '-') {
                    index = i;
                }
                if ((Character.isLetter(c) && c != 'x') || c == '(') {
                    char previous = s.charAt(i-1);
                    if (index != 0 && (previous == '*' || previous == '/' || previous == '^')) {
                        return index;
                    }

                    return i - 1;
                }
            }

            return s.length();

        } else {

            int counter = -1;

            char opening = 0;
            char closing = 0;

            for (int i = 0; i < s.length(); i++) {
                c = s.charAt(i);


                if (opening == 0) {
                    if (c == '(') {
                        opening = '(';
                        closing = ')';
                    }
                }

                if (c == opening) {
                    if (counter == -1) {
                        counter = 0;
                    }
                    counter++;
                } else if (c == closing) {
                    counter--;
                }


                if (counter == 0) {
                    return i + 1;
                }
            }
        }

        throw new IllegalArgumentException("You are retarded");

    }


    private String sanitizeFunction(String input) {
        input = input.replaceAll(" ", "");

        char first = input.charAt(0);

        if (first == '+') {
            input = input.substring(1);
        } else if (first == '-') {
            input = 0 + input;
        }

        input = input.replaceAll("\\(\\+", "(");
        input = input.replaceAll("\\(-", "(0-");

        Pattern x = Pattern.compile("([+\\-*/^])\\(x\\)");
        Matcher xMatcher = x.matcher(input);

        while (xMatcher.find()) {
            input = xMatcher.replaceAll("$1x");
            xMatcher = x.matcher(input);
        }

        Pattern constant = Pattern.compile("([^a-zA-Z0-9])\\((\\d+(\\.\\d+)?)\\)");
        Matcher constantMatcher = constant.matcher(input);

        while (constantMatcher.find()) {
            input = constantMatcher.replaceAll("$1$2");
            constantMatcher = constant.matcher(input);
        }

        input = input.replaceAll("^\\((\\d+(\\.\\d+)?)\\)", "$1");

        return input;
    }

    private String replaceMathFunctions(String input) {
        logger.trace("");
        logger.trace("ReplaceMath: {}", input);

        input = input.replaceAll("\\basin\\b", "java.lang.Math.asin");
        input = input.replaceAll("\\bsinh\\b", "java.lang.Math.sinh");
        input = input.replaceAll("\\bsin\\b", "java.lang.Math.sin");
        input = input.replaceAll("\\bacos\\b", "java.lang.Math.acos");
        input = input.replaceAll("\\bcosh\\b", "java.lang.Math.cosh");
        input = input.replaceAll("\\bcos\\b", "java.lang.Math.cos");
        input = input.replaceAll("\\batan\\b", "java.lang.Math.atan");
        input = input.replaceAll("\\btanh\\b", "java.lang.Math.tanh");
        input = input.replaceAll("\\btan\\b", "java.lang.Math.tan");
        input = input.replaceAll("\\bln\\b", "java.lang.Math.log");
        input = input.replaceAll("\\bsqrt\\b", "java.lang.Math.sqrt");

        logger.trace("EndMath: {}", input);

        return input;
    }

    private String replaceBigMethods(String input) {
        logger.trace("ReplaceBig: {}", input);
        char open = 0;
        char closed = 0;
        int begin = 0;
        int end = input.length();

        if (input.length() != 1) {

            if (input.charAt(0) != 'x' && !Character.isDigit(input.charAt(0))) {
                open = input.charAt(0);
                begin = 1;
            }

            if (input.charAt(input.length() - 1) != 'x' && !Character.isDigit(input.charAt(input.length() - 1))) {
                closed = input.charAt(input.length() - 1);
                end = input.length() - 1;
            }
            input = input.substring(begin, end);
        }

        input = input.replaceAll("(\\d+(\\.\\d+)?)", "(java.math.BigDecimal.valueOf($0))");

        input = input.replaceAll("x", "(x)");

        input = input.replaceAll("\\*", ".multiply");
        input = input.replaceAll("/", ".divide");

        Matcher plusMatcher = Pattern.compile("\\+").matcher(input);
        if (plusMatcher.find()) {
            input = '(' + plusMatcher.replaceAll(").add(") + ')';
        }

        Matcher minusMatcher = Pattern.compile("-").matcher(input);
        if (minusMatcher.find()) {
            input = '(' + minusMatcher.replaceAll(").subtract(") + ')';
        }

        input = input.replaceAll("\\(\\)+", "");

        if (open != 0) {
            input = open + input;
        }

        if (closed != 0) {
            input = input + closed;
        }

        logger.trace("EndBig: {}", input);
        logger.trace("");

        return input;

    }


    private String theTrueParsing(List<String> input) {
        for (int i = 0; i < input.size(); i++) {
            String item = input.get(i);
            if (item.length() != 1 && !uglyHackHasLetter(item)) {
                input.set(i, replaceBigMethods(item));
            }
        }
        String function = parseDijkstra(input);

        logger.debug("IT'S ME: {}", function);

        return function;
    }


    private boolean uglyHackHasLetter(String input) {
        return input.chars().anyMatch(c -> c != (int)'x' && Character.isLetter(c));
    }

    private Collection<String> parseXD(String input) { //TODO inline this
        if (isNumber(input)) {
            return Collections.singleton("(java.math.BigDecimal.valueOf(" + input + "))");
        } else if (input.length() == 1) {
            char c = input.charAt(0);
            if (c == 'x' || Character.isDigit(c)) {
                return Collections.singleton('(' + input + ')');
            } else {
                return Collections.singleton(input);
            }
        } else if (input.charAt(0) == '(' && input.charAt(input.length() - 1) == ')') {
            List<String> l = getXD(input.substring(1, input.length()-1));
            return Collections.singleton('(' + parseDijkstra(l) + ')');
        } else if (input.charAt(0) != 'x' && Character.isLetter(input.charAt(0))) {
            return Collections.singleton('(' + parseNestedXD(input) + ')');
        } else {
            return Collections.singleton(replaceBigMethods(input));
        }
    }

    private List<String> getXD(String input) {
        List<String> hoooly = new ArrayList<>();
        int index;

        logger.trace("");
        logger.trace("Parsing: {}", input);
        do {
            index = getGroupIndex(input);
            String sub = input.substring(0, index);
            logger.trace("INDEX: {}", index);
            logger.trace("F: {}", sub);

            Collection<String> l = parseXD(sub);
            logger.trace("ADDING {}", l);
            hoooly.addAll(l);
            logger.trace("");

            input = input.substring(index);
        } while (!input.isEmpty());

        return hoooly;
    }

    private String parseNestedXD(String input) {
        logger.trace("");
        logger.trace("============BLOCK=============");

        char c;

        for (int i = 0; i < input.length(); i++) {
            c = input.charAt(i);

            if (c == '(') {
                String mathf = replaceMathFunctions(input.substring(0, i));
                logger.trace("MATHF: {}", mathf);
                String plzsaveme = mathf + '(' + parseDijkstra(getXD(input.substring(i+1, input.length()-1))) + ".doubleValue())";
                logger.trace("============BLOCK=============");
                logger.trace("");
                return "java.math.BigDecimal.valueOf(" + plzsaveme + ')';
            }
        }

        throw new IllegalArgumentException("You're not supposed to be here");

    }


    private boolean isNumber(String s) {
        try {
            Double.parseDouble(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
