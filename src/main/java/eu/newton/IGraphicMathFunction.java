package eu.newton;

import eu.newton.api.IDifferentiable;
import eu.newton.utility.MathHelper;

public interface IGraphicMathFunction extends IDifferentiable<Double> {

    Double evaluate(Double x);

    @Override
    default Double differentiate(Double x, int order) {

        if (order < 1)
            throw new IllegalArgumentException("order >= 1");

        double h = 0.0001;
        double coeff = 1 / Math.pow(h, order);
        double tmp = 0;

        for (int k = 0; k <= order; k++) {
            tmp += coeff * ((k % 2 == 0) ? 1 : -1) * MathHelper.binomialCoefficient(order, k) * evaluate(x + k * h);
        }

        return tmp;
    }

    default boolean isMonotone(double a, double b, double step) {

        if (a >= b)
            throw new IllegalArgumentException("a < b");
        if (step <= 0)
            throw new IllegalArgumentException("step > 0");

        double dySign = Math.signum(evaluate(b) - evaluate(a));

        double yPrev = evaluate(a);

        for (double x = a + step; x <= b; x += step) {
            double y = evaluate(x);

            if (Math.signum(y - yPrev) == dySign || Math.signum(y - yPrev) == 0) {

            } else {
                return false;
            }

            yPrev = y;
        }

        return true;
    }

    default double[] max(double a, double b, double step) {

        if (!isMonotone(a, b, step))
            throw new AssertionError("The function must be monotone on [a, b] with the given step");

        if (a >= b)
            throw new IllegalArgumentException("a < b");
        if (step <= 0)
            throw new IllegalArgumentException("step > 0");

        double xMax = a;
        double yMax = evaluate(a);

        for (double x = a + step; x <= b; x += step) {
            double yTmp = evaluate(x);

            if (yTmp > yMax) {
                xMax = x;
                yMax = yTmp;
            }
        }

        return new double[] {xMax, yMax};
    }

    default double[] min(double a, double b, double step) {

        if (!isMonotone(a, b, step))
            throw new AssertionError("The function must be monotone on [a, b] with the given step");

        if (a >= b)
            throw new IllegalArgumentException("a < b");
        if (step <= 0)
            throw new IllegalArgumentException("step > 0");

        double xMin = a;
        double yMax = evaluate(a);

        for (double x = a + step; x <= b; x += step) {
            double yTmp = evaluate(x);

            if (yTmp < yMax) {
                xMin = x;
                yMax = yTmp;
            }
        }

        return new double[] {xMin, yMax};
    }

    default double findZero(double a, double b, double step, int precision) {

        if (!isMonotone(a, b, step))
            throw new AssertionError("The function must be monotone on [a, b] with the given step");

        double x = a;

        for (int i = 0; i < precision; i++) {
            x += (evaluate(x) / differentiate(x, 1));
        }

        return x;
    }
}
