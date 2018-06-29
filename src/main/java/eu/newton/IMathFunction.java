package eu.newton;

import eu.newton.api.IDifferentiable;

import java.math.BigDecimal;
import java.util.stream.IntStream;

import static eu.newton.Main.k;

@FunctionalInterface
public interface IMathFunction extends IDifferentiable<BigDecimal> {

    BigDecimal evaluate(BigDecimal x);

    default BigDecimal differentiate(BigDecimal x, int n) {
        final BigDecimal h = BigDecimal.valueOf(Double.MIN_VALUE).negate();
        final BigDecimal[] sum = {BigDecimal.ZERO};

        IntStream.rangeClosed(0, n).forEachOrdered(k -> {
            int nfact = IntStream.rangeClosed(1, n).reduce(1, (x1, y) -> x1 * y);
            int kfact = IntStream.rangeClosed(1, k).reduce(1, (x1, y) -> x1 * y);
            int nkfact = IntStream.rangeClosed(1, n-k).reduce(1, (x1, y) -> x1 * y);

            double coeffb = nfact / (nfact * nkfact);

            System.out.println("k = " + k);
            System.out.println("Nfact = " + nfact);
            System.out.println("Kfact = " + kfact);
            System.out.println("NKfact = " + nkfact);
            System.out.println("Coeff = " + coeffb);


            System.out.println("Coeff = " + this);

            BigDecimal xh = evaluate(x.add(k(k).multiply(h))).stripTrailingZeros();
            System.out.println("x + kh = " + xh);

            BigDecimal result = xh.multiply(k(coeffb)).stripTrailingZeros();
            if (k % 2 == 0) {
                result = result.negate();
            }
            System.out.println("Result = " + result);


            sum[0] = sum[0].add(result);
            System.out.println("sum = " + sum[0]);

        });

        if (n % 2 == 0) { //TODO plz fix me I need help
            sum[0] = sum[0].negate();
        }
        System.out.println(sum[0]);


        return sum[0].divide(h.pow(n), BigDecimal.ROUND_CEILING).setScale(6, BigDecimal.ROUND_CEILING).stripTrailingZeros();
    }

}

