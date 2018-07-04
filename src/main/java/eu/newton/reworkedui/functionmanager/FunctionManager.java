package eu.newton.reworkedui.functionmanager;

import eu.newton.MathFunction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public final class FunctionManager implements IFunctionManager {

    private HashMap<Integer, MathFunction> functionsTable;
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
            System.err.println("ERROR: Unable to parse \"" + function + "\"");
        }

        if (f != null) {
            functionsTable.put(index, f);
        }

        // DEBUG
        viewTable();

        notifyObservers();

        return f != null;
    }

    @Override
    public boolean remove(int index) {
        boolean b = functionsTable.remove(index) != null;

        // DEBUG
        viewTable();

        notifyObservers();

        return b;
    }

    @Override
    public void clear() {
        functionsTable.clear();

        // DEBUG
        viewTable();

        notifyObservers();
    }

    @Override
    public Collection<MathFunction> getFunctions() {
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

        for (IObserver observer : observers)
            observer.update();
    }

    private void viewTable() {

        StringBuilder sb = new StringBuilder();

        sb.append("Function Manager:\n");

        for (Integer i : functionsTable.keySet()) {
            sb.append(i).append(" : ").append(functionsTable.get(i).toString()).append('\n');
        }

        System.err.println(sb.toString());
    }
}
