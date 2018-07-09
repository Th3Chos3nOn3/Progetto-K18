package eu.newton.ui.functionmanager;

import eu.newton.IMathFunction;
import eu.newton.api.IDifferentiable;

import java.util.Collection;

/**
 * Must be implemented in order to manage functions through GUI
 */
public interface IFunctionManager<T> extends IObservable {

    /**
     * Add a function to be managed
     * @param index index associated with the function to be added
     * @param function  the function to be added
     * @return  if the add was successful or not
     */
    boolean add(int index, String function);

    /**
     * Remove the function associated with the given index
     * @param index the index associated with the function
     * @return  if the remove operation was successful or not
     */
    boolean remove(int index);

    /**
     * Clear the functions to be managed
     */
    void clear();

    /**
     * Get all managed functions
     * @return  all managed functions
     */
    Collection<IMathFunction<T>> getFunctions();
}
