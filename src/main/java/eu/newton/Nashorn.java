package eu.newton;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

public final class Nashorn {

    private static final ScriptEngine INSTANCE = new ScriptEngineManager(null).getEngineByName("nashorn");

    private Nashorn() {}

    public static ScriptEngine getInstance() {
        return INSTANCE;
    }

}
