package eu.newton;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.script.ScriptException;
import java.math.BigDecimal;
import java.util.function.Function;

public final class MathFunction implements IMathFunction<BigDecimal> {

    private static final Logger logger = LogManager.getLogger(FunctionParser.class);

    private static final int SAMPLING_FACTOR = 50;

    private final String function;
    private final Function<BigDecimal, BigDecimal> f;

    public MathFunction(String function) throws ScriptException {
        this.f = new FunctionParser().parse(function);
        this.function = function;
    }

    @Override
    public String toString() {
        return function;
    }

    @Override
    public BigDecimal differentiate(BigDecimal x, int grade) {

        // TODO: plz find a suitable proportional rule
        double h = 1 / (Math.sqrt(grade) / Math.log(grade + 1));

        double coeff = 1 / Math.pow(h, grade);
        double tmp = 0;

        for (int k = 0; k <= grade; k++) {
            tmp += coeff * ((k % 2 == 0) ? 1 : -1) * binomialCoefficient(grade, k).doubleValue() * evaluate(x.add(BigDecimal.valueOf(k * h))).doubleValue();
        }

        if (grade % 2 != 0) {
            tmp *= -1;
        }

        return BigDecimal.valueOf(tmp);
    }

    @Override
    public BigDecimal evaluate(BigDecimal x) {
        return f.apply(x);
    }

    @Override
    public BigDecimal max(BigDecimal a, BigDecimal b) {

        double da = a.doubleValue();
        double db = b.doubleValue();

        if (da >= db) {
            return null;
        }

        double step = computeDistance(da, db) / SAMPLING_FACTOR;

        double xMax = da;
        double yMax = evaluate(a).doubleValue();

        for (double x = da + step; x <= db; x += step) {

            double y = evaluate(BigDecimal.valueOf(x)).doubleValue();

            if (y > yMax) {
                xMax = x;
                yMax = y;
            }
        }

        return BigDecimal.valueOf(xMax);
    }

    @Override
    public BigDecimal min(BigDecimal a, BigDecimal b) {

        double da = a.doubleValue();
        double db = b.doubleValue();

        if (da >= db) {
            System.err.println("da >= db");
            return null;
        }

        double step = computeDistance(da, db) / SAMPLING_FACTOR;

        double xMin = da;
        double yMin = evaluate(a).doubleValue();

        for (double x = da + step; x <= db; x += step) {

            double y = evaluate(BigDecimal.valueOf(x)).doubleValue();

            if (y < yMin) {
                xMin = x;
                yMin = y;
            }
        }

        return BigDecimal.valueOf(xMin);
    }

    @Override
    public boolean isMonotone(BigDecimal a, BigDecimal b) {

        double da = a.doubleValue();
        double db = b.doubleValue();

        if (da >= db) {
            return false;
        }

        double step = computeDistance(da, db) / SAMPLING_FACTOR;

        double dySign = Math.signum(db - da);

        double yPrev = evaluate(a).doubleValue();

        for (double x = da + step; x <= db; x += step) {

            double y = evaluate(BigDecimal.valueOf(x)).doubleValue();

            if (Math.signum(y - yPrev) == dySign || Math.signum(y - yPrev) == 0) {

                // Do nothing, may be monotone

            } else {

                return false;
            }

            yPrev = y;
        }

        return true;
    }

    @Override
    public BigDecimal zero(BigDecimal a, BigDecimal b) {

        if ((isMonotone(a, b)) && (evaluate(a).multiply(evaluate(b)).signum() <= 0)) {

            return a.add(b).divide(BigDecimal.valueOf(2), BigDecimal.ROUND_HALF_EVEN);

        }

        return null;
    }

    private double computeDistance(double a, double b) {

        if (a == b) {

            return 0;

        } else if (a > b) {

            return a - b;

        } else {

            return b - a;

        }

    }

    private static BigDecimal binomialCoefficient(int n, int k) {

        return factorial(n).divide((factorial(k).multiply(factorial(n - k))));
    }

    private static BigDecimal factorial(int a) {
        BigDecimal result = BigDecimal.ONE;

        for (int i = 2; i <= a; i++) {
            result = result.multiply(BigDecimal.valueOf(i));
        }

        return result;
    }

    public static void main(String[] args) throws ScriptException {


        MathFunction sin = new MathFunction("sin(x)");

        BigDecimal x = BigDecimal.ONE;

        System.err.println("f'(" + x + ") = " + sin.differentiate(x, 2));

        System.err.println(factorial(21));

    }
}
