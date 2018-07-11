package eu.newton.ui.planes.helpers;

import eu.newton.IMathFunction;

public final class MathExceptionFilter<T> {

    public T eval(IMathFunction<T> f, T x) {

        T result = null;

        try {

            result = f.evaluate(x);

        } catch (Exception e) {

            // Do nothing
        }

        return result;
    }

    public T differ(IMathFunction<T> f, T x, int grade) {

        T result = null;

        try {

            result = f.differentiate(x, grade);

        } catch (Exception e) {

            // Do nothing
        }

        return result;

    }

    public T min(IMathFunction<T> f, T a, T b) {

        T result = null;

        try {

            result = f.min(a, b);

        } catch (Exception e) {

            // Do nothing
        }

        return result;

    }

    public T max(IMathFunction<T> f, T a, T b) {

        T result = null;

        try {

            result = f.max(a, b);

        } catch (Exception e) {

            // Do nothing
        }

        return result;

    }

    public T zero(IMathFunction<T> f, T a, T b) {

        T result = null;

        try {

            result = f.zero(a, b);

        } catch (Exception e) {

            // Do nothing
        }

        return result;

    }

}
