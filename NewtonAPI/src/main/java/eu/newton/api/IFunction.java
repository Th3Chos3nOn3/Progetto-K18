package eu.newton.api;

/**
 * Represents a function that accepts one argument and produces a result.
 *
 * <p>This is a <a href="package-summary.html">functional interface</a>
 * whose functional method is {@link #evaluate(Object)}.
 *
 * @param <T> the type of the input to the function and the type of the return value
 */
@FunctionalInterface
public interface IFunction<T> {

    /**
     * Evaluates this function at the given value.
     *
     * @param x the argument of the function
     * @return the function result
     */
    T evaluate(T x);

}
