package eu.newton.reworkedui.functionmanager;

public interface IObservable {

    void addObserver(IObserver observer);

    void removeObserver(IObserver observer);

    void notifyObservers();
}
