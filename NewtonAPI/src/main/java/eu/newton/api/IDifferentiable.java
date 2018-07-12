package eu.newton.api;

/**
 * This is a specialization of {@code IFunction} for
 * the case where the function is differentiable.
 *
 * @param <T> the type of the input to the function and the type of the return value
 */
public interface IDifferentiable<T> extends IFunction<T> {

    /**
     * Estimates the derivative of this function at the given value.
     *
     * @param x value given to estimate the derivative at
     * @param grade grade of derivation
     * @return the derivative evaluated at the given value
     */
    T differentiate(T x, int grade);

}
