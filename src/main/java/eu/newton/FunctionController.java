package eu.newton;

import eu.unipv.projectk.functions.FooMathFunction;

import java.util.Arrays;

public class FunctionController {
    private static final int STD_SIZE = 5;
    private FooMathFunction functions[];
    private int index, resizeCounter;

    public FunctionController() {
        functions = new FooMathFunction[STD_SIZE];
        index = 0;
        resizeCounter = 1;
    }

    public void add(FooMathFunction f) {
        if (index >= STD_SIZE * resizeCounter) {
            functions = Arrays.copyOf(functions, functions.length + STD_SIZE);
            resizeCounter++;
        }

        functions[index++] = f;
    }

    public FooMathFunction[] getFunctions() {
        return functions;
    }
}
