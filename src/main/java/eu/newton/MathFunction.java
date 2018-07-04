package eu.newton;

import eu.newton.api.IDifferentiable;
import eu.newton.api.IFunction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.script.ScriptException;
import java.math.BigDecimal;
import java.util.function.Function;
import java.util.stream.IntStream;

import static eu.newton.Main.k;

public final class MathFunction implements IFunction<BigDecimal>, IDifferentiable<BigDecimal> {

    private static final Logger logger = LogManager.getLogger(BetterParser.class);
    
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
}
