import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Log4jLab {

    private static final Logger logger =
            LogManager.getLogger(Log4jLab.class);

    public static void main(String[] args) {

        logger.info("Application started (Log4j 2).");

        logger.warn("This is a warning message.");

        try {
            int x = 10 / 0;
        } catch (ArithmeticException e) {

            logger.error("Arithmetic error occurred", e);
        }

        logger.debug("Expensive operation result: {}",
                () -> getExpensiveData());

        logger.info("Application finished.");
    }

    private static String getExpensiveData() {

        return "Data loaded";
    }
}
