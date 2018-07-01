package eu.newton;

import javax.script.ScriptException;

public final class GraphicMathFunction {

    private final String function;
    private final IGraphicMathFunction f;

    public GraphicMathFunction(String function) throws ScriptException {
        this.f = new Parser().parse(function);
        this.function = function;
    }

    public double evaluate(double x) {
        return f.evaluate(x);
    }

    public double differentiate(double x, int order) {
        return f.differentiate(x, order);
    }

    @Override
    public String toString() {
        return function;
    }
}
