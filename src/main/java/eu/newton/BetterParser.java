package eu.newton;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BetterParser implements IParser {


    @Override
    public IMathFunction parse(String input) {

        System.out.println("ORIGINAL: " + input);

        input = sanitizeFunction(input);

        System.out.println("UNRET: " + input);

        List<String> list = getXD(input, true);

        System.out.println("THE LIST: " + list);

        IMathFunction f = theTrueParsing(list);

        return f;
    }


    private String parseDijkstra(List<String> dijkstraHolyList) {
        StringBuilder fuuu = new StringBuilder();
        if (dijkstraHolyList.size() != 1) {
            ListIterator<String> it = dijkstraHolyList.listIterator();

            String current;
            String op;

            while (it.hasNext()) {
                current = it.next();

                if (it.hasNext()) {
                    op = it.next();

                    System.out.println("CURRENT: " + current);
                    System.out.println("OP: " + op);



                    if (op.equals("*") || op.equals("/")) {
                        String to = it.next();
                        it.set("(" + current + replaceBigMethods(op) + to + ")");
                        it.previous();
                        System.out.println("PREVIOUS: " + it.previous());
                        it.remove();
                        System.out.println("PREVIOUS: " + it.previous());
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
                        System.out.println("PREVIOUS: " + it.previous());
                        it.remove();
                        System.out.println("PREVIOUS: " + it.previous());
                        it.remove();
                    }
                }
            }

            System.out.println(dijkstraHolyList);


            dijkstraHolyList.forEach(s -> {
                if (s.length() == 1) {
                    fuuu.append(replaceBigMethods(s));
                } else {
                    fuuu.append(s);
                }
            });
            System.out.println("FUNC: " + fuuu);

            if (fuuu.toString().startsWith("java.lang.Math.")) {
                return "function(x) java.math.BigDecimal.valueOf(" + fuuu.toString() + ")";
            }
            fuuu.insert(0, "function(x) ");

        } else {
            if (fuuu.toString().startsWith("java.lang.Math.")) {
                return "function(x) java.math.BigDecimal.valueOf(" + fuuu.toString() + ")";
            }
            fuuu.append("function(x) ").append(dijkstraHolyList.get(0));
            System.out.println("FUNC: " + fuuu);

        }
        return fuuu.toString();
    }

    public int getIndexToken(String s) {
        System.out.println("Index token of: " + s);
        int index = -1;

        int check = -1;

        int counter = -1;

        char opening = 0;
        char closing = 0;

        char c = s.charAt(0);

        if (c == '*' || c == '/' || c == '+' || c == '-' || c == '^') {
            return 1;
        }

        if (Character.isDigit(c) || c == 'x') {

            for (int i = 0; i < s.length(); i++) {
                c = s.charAt(i);
                if (c == '+' || c == '-') {
                    check = i;
                }
                if ((Character.isLetter(c) && c != 'x') || c == '(') {
                    char previous = s.charAt(i-1);
                    if (check != -1 && (previous == '*' || previous == '/' || previous == '^')) {
                        return check;
                    }

                    return i - 1;
                }
            }
        } else {

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
                    index = i + 1;
                    break;
                }
            }
        }

        if (index == -1) {
            index = s.length();
        }
        return index;


    }


    public String sanitizeFunction(String input) {
        input = input.replaceAll(" ", "");

        if (input.charAt(0) == '-') {
            input = 0 + input;
        } else if ((input.charAt(0) != 'x' && Character.isLetter(input.charAt(0))) || input.charAt(0) == '|') {
            input = 0 + "+" + input;
        }

        input = input.replaceAll("([^0-9)x])(\\|\\+)", "$1|");
        input = input.replaceAll("\\(\\+", "(");

        //input = input.replaceAll("((\\d+|x|\\|)\\*(\\d+|x|\\|))+", "($0)");

        input = input.replaceAll("\\(-", "(0-");
        input = input.replaceAll("([^0-9)])(\\|(\\(+)?-)", "$1|$30-");

        //input = input.replaceAll("x\\*x", "(x*x)");

        return input;
    }

    private String replaceMathFunctions(String input) {
        System.out.println();
        System.out.println("ReplaceMath: " + input);

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
        input = input.replaceAll("(\\|(.*?)\\|)", "(java.lang.Math.abs($2))");


        input = "(java.math.BigDecimal.valueOf(" + input + "))";

        System.out.println("EndMath: " + input);

        return input;
    }

    private String replaceBigMethods(String input) {
        System.out.println("ReplaceBig: " + input);
        char open = 0;
        char closed = 0;
        int begin = 0;
        int end = input.length();

        if (input.isEmpty()) {
            return input;
        }

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
        //System.out.println();
//        input = input.replaceAll("((\\d+|x)\\*(\\d+|x))", "($0)");
//        //System.out.println(input);
//
//        input = input.replaceAll("\\(-", "(0-");
//        input = input.replaceAll("([^0-9|)])(\\|(\\(+)?-)", "$1|$30-");
        //input = input.replaceAll("([^(\\d+|))])(\\|-)", "$1|0-");
        //System.out.println(input);

        input = input.replaceAll("\\d+", "(java.math.BigDecimal.valueOf($0))");
        //System.out.println(input);

        input = input.replaceAll("x", "(x)");


        input = input.replaceAll("\\*", ".multiply");
        input = input.replaceAll("/", ".divide");

        Matcher plusMatcher = Pattern.compile("\\+").matcher(input);
        if (plusMatcher.find()) {
            input = '(' + plusMatcher.replaceAll(").add(") + ')';
        }
//        System.out.println("COS ? " + input);

        Matcher minusMatcher = Pattern.compile("-").matcher(input);
        if (minusMatcher.find()) {
            input = '(' + minusMatcher.replaceAll(").subtract(") + ')';
        }
//        System.out.println("COS ? " + input);

        input = input.replaceAll("[\\^]+", ".pow");

        input = input.replaceAll("\\(\\)+", "");

        if (open != 0) {
            input = open + input;
        }

        if (closed != 0) {
            input = input + closed;
        }

        System.out.println("EndBig: " + input);
        System.out.println();

        return input;

    }


    public IMathFunction theTrueParsing(List<String> input) {
        ScriptEngine engine = new ScriptEngineManager(null).getEngineByName("nashorn");
        for (int i = 0; i < input.size(); i++) {
            String item = input.get(i);
            if (item.length() != 1 && !uglyHackHasLetter(item)) {
                input.set(i, replaceBigMethods(item));
            }
        }
        String function = parseDijkstra(input);

        System.out.println("IT'S ME: " + function);
        IMathFunction f = null;
        try {
            f = (IMathFunction) engine.eval(String.format(
                    "var mathFunc = Java.type('"+IMathFunction.class.getName()+"');" +
                            "new mathFunc(%s)", function));
        } catch (ScriptException e) {
            e.printStackTrace();
        }
        return f;
    }


    public boolean uglyHackHasLetter(String input) {
        return input.chars().anyMatch(c -> c != (int)'x' && Character.isLetter(c));
    }

    public List<String> getXD(String input, boolean normal) {
        List<String> hoooly = new ArrayList<>();
        int index;


        System.out.println("Parsing: " + input);
        while (!input.isEmpty() && (index = getIndexToken(input)) != -1) {
            String deeper = input.substring(0, index);
            System.out.println("INDEX: " + index);
            System.out.println("F: " + deeper);
            if (deeper.length() == 1) {
                if (deeper.charAt(0) == 'x') {
                    deeper = "(x)";
                } else {
                    if (Character.isDigit(deeper.charAt(0))) {
                        deeper = replaceBigMethods(deeper);
                    }
                }
                hoooly.add(deeper);
            } else {
                if (deeper.length() != 1 && uglyHackHasLetter(input)) {
                    List<String> liist = parseNestedXD(deeper);
                    String first = liist.get(0);
                    liist.set(0, "(" + first);

                    String last = liist.get(liist.size() - 1);
                    liist.set(liist.size() - 1, last + ")");

                    hoooly.addAll(liist);
                } else {
                    if (normal) {
                        hoooly.add(replaceBigMethods(deeper));
                    } else {
                        hoooly.add(deeper);
                    }
                }
            }

            input = input.substring(index);
        }
        return hoooly;


    }


    public List<String> parseNestedXD(String input) {
        System.out.println();
        System.out.println("============BLOCK=============");
        List<String> hoooly = new ArrayList<>();
        int index = -1;

        while (input.charAt(0) == '(') {
            if (input.charAt(input.length() -1) == ')') {
                input = input.substring(1, input.length() - 1);
            } else {
                hoooly.addAll(getXD(input, false));
                return hoooly;
            }
        }
        System.out.println("SIMPLIFIED TO: " + input);

        char c = input.charAt(0);
        if (Character.isLetter(c) && c != 'x') {
            hoooly.add(replaceMathFunctions(input));
            for (int i = 0; i < input.length(); i++) {
                c = input.charAt(i);
                if (c == '(') {
                    index = i;
                    getXD(input.substring(i), false);
                    break;
                }
            }
        } else {
            List<String> liiist = getXD(input, false);
            StringBuilder ss = new StringBuilder();

            liiist.forEach(s -> {
                if (!uglyHackHasLetter(s)) {
                    s = replaceBigMethods(s);
                }
                ss.append(s);
            });

            hoooly.add(ss.toString());
        }

        if (index == -1) {
            System.out.println("Clean Function: " + input);
        }

        System.out.println("============BLOCK=============");
        System.out.println();
        return hoooly;
    }
}
