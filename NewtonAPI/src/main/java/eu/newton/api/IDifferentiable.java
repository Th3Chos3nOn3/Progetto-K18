package eu.newton.api;

public interface IDifferentiable<T> extends IFunction<T> {

    T differentiate(T x, int grade);

}
