package eu.newton;

import javax.script.ScriptException;
import java.math.BigDecimal;

public final class MathematicalFunction {

    private final String function;
    private final IMathFunction f;

    public MathematicalFunction(String function) throws ScriptException {
        this.f = new BetterParser().parse(function);
        this.function = function;
    }

    public double evaluate(double x) {
        return f.evaluate(BigDecimal.valueOf(x)).doubleValue();
    }

    @Override
    public String toString() {
        return function;
    }
}
