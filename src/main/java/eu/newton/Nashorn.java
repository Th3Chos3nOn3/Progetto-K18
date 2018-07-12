package eu.newton;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

/**
 * This Java application has a single instance of class
 * <code>Nashorn</code> that allows the application to interface with
 * the NashornEngine. The current
 * engine can be obtained from the <code>getInstance</code> method.
 * <p>
 * An application cannot create its own instance of this class.
 *
 * @see Nashorn#getInstance()
 */
public final class Nashorn {

    private static final ScriptEngine INSTANCE = new ScriptEngineManager(null).getEngineByName("nashorn");

    /** Don't let anyone else instantiate this class */
    private Nashorn() {}

    /**
     * Returns the NashornEngine object associated with the current Java application.
     *
     * @return the <code>NashornEngine</code> object associated with the current Java application
     */
    public static ScriptEngine getInstance() {
        return INSTANCE;
    }

}
