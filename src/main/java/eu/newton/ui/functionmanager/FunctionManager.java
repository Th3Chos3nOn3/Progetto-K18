package eu.newton.ui.functionmanager;

import eu.newton.MathFunction;
import eu.newton.api.IDifferentiable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.script.ScriptException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Collection;

public final class FunctionManager implements IFunctionManager<BigDecimal> {

    private static final Logger logger = LogManager.getLogger(FunctionManager.class);

    private Map<Integer, IDifferentiable<BigDecimal>> functionsTable;
    private List<IObserver> observers;

    public FunctionManager() {
        functionsTable = new HashMap<>();
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
    public boolean remove(int index) {
        boolean b = functionsTable.remove(index) != null;

        logger.trace("FUNCTION MANAGER: {}", this::toString);

        notifyObservers();

        return b;
    }

    @Override
    public void clear() {
        functionsTable.clear();

        // DEBUG
        logger.trace("FUNCTION MANAGER: {}", this::toString);

        notifyObservers();
    }

    @Override
    public Collection<IDifferentiable<BigDecimal>> getFunctions() {
        return functionsTable.values();
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

        sb.append('\n');

        for (Integer i : functionsTable.keySet()) {
            sb.append(i).append(" : ").append(functionsTable.get(i).toString()).append('\n');
        }

        return sb.toString();
    }
}
