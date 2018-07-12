package eu.newton.ui.functionmanager;

import eu.newton.IMathFunction;
import eu.newton.MathFunction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;
import java.util.*;

public final class FunctionManager implements IFunctionManager<BigDecimal> {

    private static final Logger logger = LogManager.getLogger(FunctionManager.class);

    private Map<Integer, IMathFunction<BigDecimal>> functionsTable;
    private Map<Integer, Integer> derivativesTable;

    private List<IObserver> observers;

    public FunctionManager() {

        functionsTable = new HashMap<>();
        derivativesTable = new HashMap<>();
        observers = new ArrayList<>();
    }

    @Override
    public boolean add(int index, String function) {

        MathFunction f = null;

        try {

           f = new MathFunction(function);

        } catch (Exception e) {

            logger.trace("ERROR: Unable to parse {}", function);
        }

        if (f != null) {
            functionsTable.put(index, f);
        }

        logger.trace("FUNCTION MANAGER: {}", this::toString);

        notifyObservers();

        return f != null;
    }

    @Override
    public boolean addDerivative(int index, int order) {

        // Test if index corresponds to a valid function
        // if it is, add it to derivatives table
        if (functionsTable.get(index) != null) {

            derivativesTable.put(index, order);

        }

        logger.trace("FUNCTION MANAGER: {}", this::toString);

        notifyObservers();

        return functionsTable.get(index) != null;
    }

    @Override
    public boolean remove(int index) {

        boolean b = functionsTable.remove(index) != null;

        if (b) {

            derivativesTable.remove(index);
        }

        logger.trace("FUNCTION MANAGER: {}", this::toString);

        notifyObservers();

        return b;
    }

    @Override
    public boolean removeDerivative(int index) {

        boolean b = derivativesTable.remove(index) != null;

        logger.trace("FUNCTION MANAGER: {}", this::toString);

        notifyObservers();

        return b;
    }

    @Override
    public void clear() {

        functionsTable.clear();

        derivativesTable.clear();

        logger.trace("FUNCTION MANAGER: {}", this::toString);

        notifyObservers();
    }

    @Override
    public Collection<IMathFunction<BigDecimal>> getFunctions() {
        return functionsTable.values();
    }

    @Override
    public Map<IMathFunction<BigDecimal>, Integer> getDerivativeFunctions() {

        Map<IMathFunction<BigDecimal>, Integer> functions = new HashMap<>();

        for (Integer i : derivativesTable.keySet()) {
            functions.put(functionsTable.get(i), derivativesTable.get(i));
        }

        return functions;
    }

    @Override
    public void addObserver(IObserver observer) {
        observers.add(observer);
    }

    @Override
    public void removeObserver(IObserver observer) {
        observers.remove(observer);
    }

    @Override
    public void notifyObservers() {

        for (IObserver observer : observers) {
            observer.update();
        }
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();

        sb.append("\nFunction Table:\n");
        for (Integer i : functionsTable.keySet()) {
            sb.append(i).append(" : ").append(functionsTable.get(i).toString()).append('\n');
        }

        sb.append("\nDerivative Table\n");
        for (Integer i : derivativesTable.keySet()) {
            sb.append("[Index: ").append(i).append("] ").append("[Order: ").append(derivativesTable.get(i)).append("]\n");
        }

        return sb.toString();
    }
}
