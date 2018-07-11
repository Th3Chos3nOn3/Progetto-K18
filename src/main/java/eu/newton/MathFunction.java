package eu.newton;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.script.ScriptException;
import java.math.BigDecimal;
import java.util.function.Function;
import java.util.stream.IntStream;

import static eu.newton.Main.k;

public final class MathFunction implements IMathFunction<BigDecimal> {

    private static final Logger logger = LogManager.getLogger(BetterParser.class);

    private static final double RETARDED_H = 0.0000000001;
    private static final int SAMPLING_FACTOR = 20;

    private final String function;
    private final Function<BigDecimal, BigDecimal> f;

    public MathFunction(String function) throws ScriptException {
        this.f = new BetterParser().parse(function);
        this.function = function;
    }

    @Override
    public String toString() {
        return function;
    }

    @Override
    public BigDecimal differentiate(BigDecimal x, int grade) {
        final BigDecimal h = BigDecimal.valueOf(RETARDED_H).negate();
        final BigDecimal[] sum = {BigDecimal.ZERO};

        IntStream.rangeClosed(0, grade).forEachOrdered(k -> {
            int nfact = IntStream.rangeClosed(1, grade).reduce(1, (x1, y) -> x1 * y);
            int kfact = IntStream.rangeClosed(1, k).reduce(1, (x1, y) -> x1 * y);
            int nkfact = IntStream.rangeClosed(1, grade-k).reduce(1, (x1, y) -> x1 * y);

            double coeffb = nfact / (nfact * nkfact);

            logger.trace("k = {}", k);
            logger.trace("Nfact = {}", nfact);
            logger.trace("Kfact = {}", kfact);
            logger.trace("NKfact = {}", nkfact);
            logger.trace("Coeff = {}", coeffb);


            logger.trace("Coeff = {}", this);

            BigDecimal xh = evaluate(x.add(k(k).multiply(h))).stripTrailingZeros();
            logger.trace("x + kh = {}", xh);

            BigDecimal result = xh.multiply(k(coeffb)).stripTrailingZeros();
            if (k % 2 == 0) {
                result = result.negate();
            }
            logger.trace("Result = {}", result);


            sum[0] = sum[0].add(result);
            logger.trace("sum = {}", sum[0]);

        });

        if (grade % 2 == 0) { //TODO plz fix me I need help
            sum[0] = sum[0].negate();
        }
        logger.trace(sum[0]);


        return sum[0].divide(h.pow(grade), BigDecimal.ROUND_CEILING).setScale(6, BigDecimal.ROUND_CEILING).stripTrailingZeros();
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

    public static void main(String[] args) throws ScriptException {

        // Plz test me!

    }
}
