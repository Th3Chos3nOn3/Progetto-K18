package eu.newton;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.script.ScriptException;
import java.math.BigDecimal;


public class Main {

    private static final Logger logger = LogManager.getLogger(Main.class);

    public static void main(String[] args) {

        final String f = "(2^2^(x-2)^sin(x)^2+sqrt(3))^sin(3*x^2)";

        try {
            MathFunction function = new MathFunction(f);

            BigDecimal result = null;

            try {
                result = function.evaluate(k(2));
            } catch (NumberFormatException | ArithmeticException ex) {
                logger.error("Are you retarded ? ");
                logger.error(ex.getMessage());
            } catch (Exception ex) {
                logger.error("Stop it java");
                logger.error(ex.getMessage());
            }

            logger.debug("RESULT: {}", result);

        } catch (ScriptException e) {
            logger.error("Good job, you won. Now fuck off ");
            logger.error(e.getMessage());
        }

    }


    public static BigDecimal k(double d) {
        return BigDecimal.valueOf(d);
    }




//    public static BigDecimal precise(BigDecimal a, BigDecimal b) {
//        return a.divide(b, 1000, BigDecimal.ROUND_CEILING);
//    }

}


