package eu.newton;

import eu.newton.api.IDifferentiable;
import eu.newton.api.IExtrema;
import eu.newton.api.IMonotone;
import eu.newton.api.IZero;

public interface IMathFunction<T> extends IDifferentiable<T>, IExtrema<T>, IZero<T>, IMonotone<T> {

}
