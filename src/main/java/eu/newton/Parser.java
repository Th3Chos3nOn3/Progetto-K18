package eu.newton;

import jdk.nashorn.api.scripting.JSObject;
import jdk.nashorn.api.scripting.NashornScriptEngineFactory;

import javax.script.ScriptEngine;
import javax.script.ScriptException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Parser {

    // TODO: fix trigonometric hyperbolic functions

    private static final String TOKEN_PREFIX = "Math.";

    private ScriptEngine engine;

    private enum Token {
        abs("abs"),
        acos("acos"),
        acosh("acosh"),
        asin("asin"),
        asinh("asinh"),
        atan("atan"),
        atanh("atanh"),
        cbrt("cbrt"),
        ceil("ceil"),
        cos("cos"),
        cosh("cosh"),
        exp("exp"),
        floor("floor"),
        log("log"),
        round("round"),
        sin("sin"),
        sinh("sinh"),
        sqrt("sqrt"),
        tan("tan"),
        tanh("tanh"),
        pow("pow"),
        ;

        Token(String s) {
        }
    }

    public Parser() {
        engine = new NashornScriptEngineFactory().getScriptEngine();
    }

    public IGraphicMathFunction parse(String s) throws ScriptException {

        for (Token t : Token.values()) {

            String strToken = t.toString();
            Pattern p = Pattern.compile(strToken);
            Matcher matcher = p.matcher(s);

            s = matcher.replaceAll(TOKEN_PREFIX + strToken);
        }


        JSObject JSFunction = (JSObject) engine.eval(
                "function(x) {" +
                        " try {" +
                        " return (" + s + ");" +
                        " }" +
                        " catch (e) {" +
                        " }" +
                        " }");

        IGraphicMathFunction f = x -> (Double) JSFunction.call(null, x);

        // Test for internal error
        try {
            f.evaluate(.0);
        } catch (ClassCastException e) {
            throw new ScriptException("Internal error");
        }

        return f;
    }
}
