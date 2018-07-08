import eu.newton.MathFunction;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

import javax.script.ScriptException;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ParserAndEvaluationTest {

    @ParameterizedTest
    @CsvFileSource(resources = "functions.csv", numLinesToSkip = 2)
    void testFunction(String function, double expected) throws ScriptException {
        MathFunction f = new MathFunction(function);
        BigDecimal result = f.evaluate(BigDecimal.valueOf(2));

        assertEquals(expected, result.doubleValue());
        System.out.println(result);

    }

}
