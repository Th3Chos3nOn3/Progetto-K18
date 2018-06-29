package eu.newton;

import eu.newton.api.IDifferentiable;
import eu.newton.utility.MathHelper;

public interface IGraphicMathFunction extends IDifferentiable<Double> {

    Double evaluate(Double x);

    @Override
    default Double differentiate(Double x, int grade) {

        double h = 0.0001;
        double coefficient = 1 / Math.pow(x, grade);

        double sum = 0;

        for (int i = 0; i < grade; i++) {
            sum += ((i % 2 == 0) ? 1 : -1) * MathHelper.binomialCoefficient(grade, i) * evaluate(x + i * h);
        }

        return sum;
    }

}
