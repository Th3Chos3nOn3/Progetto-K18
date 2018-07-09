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

    private static final int SAMPLING_FACTOR = 100;
    private static final int ZERO_PRECISION = 5;

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
        final BigDecimal h = BigDecimal.valueOf(Double.MIN_VALUE).negate();
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
    public BigDecimal[] max(BigDecimal a, BigDecimal b) {

        if (a.compareTo(b) >= 0) {
            throw new AssertionError("a < b");
        }

        if (!isMonotone(a, b)) {
            return null;
        }

        BigDecimal step = b.subtract(a).divide(BigDecimal.valueOf(SAMPLING_FACTOR), BigDecimal.ROUND_CEILING);

        BigDecimal xMax = a;
        BigDecimal yMax = evaluate(a);

        for (BigDecimal x = a.add(step); x.compareTo(b) <= 0; x = x.add(step)) {

            BigDecimal y = evaluate(x);

            if (y.compareTo(yMax) > 0) {
                xMax = x;
                yMax = y;
            }
        }

        return new BigDecimal[] {xMax, yMax};
    }

    @Override
    public BigDecimal[] min(BigDecimal a, BigDecimal b) {

        if (a.compareTo(b) >= 0) {
            throw new AssertionError("a < b");
        }

        if (!isMonotone(a, b)) {
            return null;
        }

        BigDecimal step = b.subtract(a).divide(BigDecimal.valueOf(SAMPLING_FACTOR), BigDecimal.ROUND_CEILING);

        BigDecimal xMin = a;
        BigDecimal yMin = evaluate(a);

        for (BigDecimal x = a.add(step); x.compareTo(b) <= 0; x = x.add(step)) {

            BigDecimal y = evaluate(x);

            if (y.compareTo(yMin) < 0) {
                xMin = x;
                yMin = y;
            }
        }

        return new BigDecimal[] {xMin, yMin};
    }

    @Override
    public boolean isMonotone(BigDecimal a, BigDecimal b) {

        if (a.compareTo(b) >= 0) {
            throw new AssertionError("a < b");
        }

        BigDecimal step = b.subtract(a).divide(BigDecimal.valueOf(SAMPLING_FACTOR), BigDecimal.ROUND_CEILING);

        int dySign = b.subtract(a).signum();

        BigDecimal yPrev = evaluate(a);

        for (BigDecimal x = a.add(step); x.compareTo(b) <= 0; x = x.add(step)) {
            BigDecimal y = evaluate(x);

            if (y.subtract(yPrev).signum() == dySign || y.subtract(yPrev).signum() == 0) {
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

        if (isMonotone(a, b) && evaluate(a).multiply(evaluate(b)).signum() <= 0) {

            BigDecimal x = a;

            for (int i = 0; i < ZERO_PRECISION; i++) {
                x = x.subtract(evaluate(x).divide(differentiate(x, 1), BigDecimal.ROUND_CEILING));
            }

            return x;
        }

        return null;
    }


}
