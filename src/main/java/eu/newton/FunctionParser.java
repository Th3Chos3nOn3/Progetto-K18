package eu.newton;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.script.ScriptException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.function.Function;
import java.util.regex.Pattern;

public class FunctionParser {

    private static final Logger logger = LogManager.getLogger(FunctionParser.class);

    private static final Pattern LN = Pattern.compile("\\bln\\b");
    private static final Pattern SIN = Pattern.compile("\\bsin\\b");
    private static final Pattern COS = Pattern.compile("\\bcos\\b");
    private static final Pattern TAN = Pattern.compile("\\btan\\b");
    private static final Pattern ASIN = Pattern.compile("\\basin\\b");
    private static final Pattern ACOS = Pattern.compile("\\bacos\\b");
    private static final Pattern ATAN = Pattern.compile("\\batan\\b");
    private static final Pattern SINH = Pattern.compile("\\bsinh\\b");
    private static final Pattern COSH = Pattern.compile("\\bcosh\\b");
    private static final Pattern TANH = Pattern.compile("\\btanh\\b");
    private static final Pattern SQRT = Pattern.compile("\\bsqrt\\b");

    private static final Pattern DOUBLE = Pattern.compile("(\\d+(\\.\\d+)?)");

    public Function<BigDecimal, BigDecimal> parse(String function) throws ScriptException {

        if (function.isEmpty()) {
            throw new IllegalArgumentException("You didn't insert a function");
        }

        logger.trace("ORIGINAL: {}", function);

        function = Sanitizer.simplify(function);

        logger.trace("SIMPLIFIED: {}", function);

        List<String> groups = getGroups(function);

        logger.trace("THE LIST: {}", groups);

        function = parseGroups(groups);

        final String parserVar = "var parser = Packages.eu.newton.FunctionParser;";

        @SuppressWarnings("unchecked")
        Function<BigDecimal,BigDecimal>  f = (Function<BigDecimal, BigDecimal>)
                Nashorn.getInstance().eval(parserVar + " new java.util.function.Function(function(x) " + function + ")");

        return f;

    }

    private List<String> getGroups(String group) {
        List<String> groups = new ArrayList<>();
        int index;

        logger.trace("");
        logger.trace("Parsing: {}", group);
        do {
            index = getGroupIndex(group);
            String sub = group.substring(0, index);
            logger.trace("INDEX: {}", index);
            logger.trace("F: {}", sub);

            String parsed = parseGroup(sub);
            logger.trace("ADDING {}", parsed);
            groups.add(parsed);
            logger.trace("");

            group = group.substring(index);
        } while (!group.isEmpty());

        return groups;
    }

    private int getGroupIndex(String group) {
        logger.trace("Index of: {}", group);

        char c = group.charAt(0);

        if (c == '*' || c == '/' || c == '+' || c == '-' || c == '^' || c == 'x') {
            return 1;
        }

        if (Character.isDigit(c)) {
            for (int i = 1; i < group.length(); i++) {
                char d = group.charAt(i);
                if (!Character.isDigit(d) && d != '.') {
                    return i;
                }
            }
            return group.length();
        }

        int counter = -1;

        char opening = 0;
        char closing = 0;

        for (int i = 0; i < group.length(); i++) {
            c = group.charAt(i);

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

        throw new IllegalArgumentException("Dangling brace");

    }

    private String parseGroup(String group) {
        if (isNumber(group)) {
            return "(java.math.BigDecimal.valueOf(" + group + "))";
        }

        char c = group.charAt(0);

        if (group.length() == 1) {
            if (c == 'x') {
                return '(' + group + ')';
            } else {
                return group;
            }
        }

        if (c == '(' && group.charAt(group.length() - 1) == ')') {
            List<String> l = getGroups(group.substring(1, group.length() - 1));
            return '(' + parseGroups(l) + ')';
        }

        if (c != 'x' && Character.isLetter(c)) {
            return '(' + parseNestedGroups(group) + ')';
        }

        throw new IllegalArgumentException("Unsupported group");

    }

    private String parseNestedGroups(String group) {
        logger.trace("");
        logger.trace("============BLOCK=============");

        char c;

        for (int i = 0; i < group.length(); i++) {
            c = group.charAt(i);

            if (c == '(') {
                String f = replaceMathFunctions(group.substring(0, i));
                logger.trace("MATHF: {}", f);
                String parsed = f + "(parser.unwrap(" + parseGroups(getGroups(group.substring(i + 1, group.length() - 1))) + "))";
                logger.trace("============BLOCK=============");
                logger.trace("");
                return "java.math.BigDecimal.valueOf(" + parsed + ')';
            }
        }

        throw new IllegalArgumentException("You're not supposed to be here");

    }

    private String parseGroups(List<String> groups) {
        logger.trace("");
        logger.trace("PARSING: {}", groups);

        StringBuilder function = new StringBuilder();
        if (groups.size() != 1) {
            groups = fixThePow(groups);
            ListIterator<String> it = groups.listIterator();

            String current;
            String op;

            while (it.hasNext()) {
                current = it.next();

                if (current.startsWith("java.lang.Math.")) {
                    current = "java.math.BigDecimal.valueOf(" + current + ')';
                    it.set(current);
                    logger.trace("NEW CURRENT: {}", current);
                }

                if (it.hasNext()) {
                    op = it.next();

                    logger.trace("CURRENT: {}", current);
                    logger.trace("OP: {}", op);

                    if (op.equals("*") || op.equals("/")) {
                        String to = it.next();
                        it.set('(' + current + replaceBigMethods(op) + to + ')');
                        it.previous();
                        it.previous();
                        it.remove();
                        it.previous();
                        it.remove();
                    }
                }
            }

            logger.trace("DHL: {}", groups);


            groups.forEach(s -> {
                if (s.length() == 1) {
                    function.append(replaceBigMethods(s));
                } else {
                    function.append('(').append(s).append(')');
                }
            });

        } else {
            function.append(groups.get(0));
        }

        String ffs = function.toString();

        logger.trace("DONE: {}", ffs);
        logger.trace("");

        return ffs;
    }

    private List<String> fixThePow(List<String> list) {
        ListIterator<String> it = list.listIterator(list.size() - 1);

        it.next();
        String current;
        String op;


        while (it.hasPrevious()) {
            current = it.previous();

            if (it.hasPrevious()) {
                op = it.previous();

                logger.trace("CURRENT: {}", current);
                logger.trace("OP: {}", op);


                if (op.equals("^")) {
                    String to = it.previous();

                    current = "parser.unwrap(" + current + ")";
                    to = "parser.unwrap("  + to + ")";


                    it.set("(parser.wrap(java.lang.Math.pow(" + to + ',' + current + ")))");
                    it.next();
                    it.next();
                    it.remove();
                    it.next();
                    it.remove();
                }
            }
        }

        logger.trace("POW L: {}", list);

        return list;

    }

    private String replaceMathFunctions(String input) {
        logger.trace("");
        logger.trace("ReplaceMath: {}", input);

        input = LN.matcher(input).replaceAll("java.lang.Math.log");
        input = SIN.matcher(input).replaceAll("java.lang.Math.sin");
        input = COS.matcher(input).replaceAll("java.lang.Math.cos");
        input = TAN.matcher(input).replaceAll("java.lang.Math.tan");
        input = ASIN.matcher(input).replaceAll("java.lang.Math.asin");
        input = ACOS.matcher(input).replaceAll("java.lang.Math.acos");
        input = ATAN.matcher(input).replaceAll("java.lang.Math.atan");
        input = SINH.matcher(input).replaceAll("java.lang.Math.sinh");
        input = COSH.matcher(input).replaceAll("java.lang.Math.cosh");
        input = TANH.matcher(input).replaceAll("java.lang.Math.tanh");
        input = SQRT.matcher(input).replaceAll("java.lang.Math.sqrt");

        logger.trace("EndMath: {}", input);

        return input;
    }

    private String replaceBigMethods(String input) {
        logger.trace("ReplaceBig: {}", input);

        input = DOUBLE.matcher(input).replaceAll("(java.math.BigDecimal.valueOf($0))");

        input = input.replace("x", "(x)");
        input = input.replace("*", ".multiply");
        input = input.replace("/", ".divide");
        input = input.replace("-", ".subtract");
        input = input.replace("+", ".add");

        logger.trace("EndBig: {}", input);
        logger.trace("");

        return input;

    }

    private boolean isNumber(String s) {
        try {
            Double.parseDouble(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static BigDecimal wrap(double d) {
        return BigDecimal.valueOf(d);
    }

    public static BigDecimal wrap(BigDecimal bd) {
        return bd;
    }

    public static double unwrap(double d) {
        return d;
    }

    public static double unwrap(BigDecimal bd) {
        return bd.doubleValue();
    }
}
