package uk.m0nom.adifweb.util;

import java.util.logging.Logger;

public class LoggerSetup {
    private static final Logger logger = Logger.getLogger(LoggerSetup.class.getName());

    public static Logger getRootLogger() {
        Logger current = logger;
        while (current.getParent() != null) {
            current = current.getParent();
        }
        return current;
    }

    public static CustomFileLogHandler setupNewLogFile(long runTimestamp) {
        // Use a custom logger to capture log output to a file
        CustomFileLogHandler customFileLogHandler = null;
        try {
            customFileLogHandler = new CustomFileLogHandler(runTimestamp);
            getRootLogger().addHandler(customFileLogHandler);
        } catch (Exception e) {
            logger.severe(String.format("Caught exception setting up custom log file handler: %s", e.getMessage()));
            customFileLogHandler = null;
        }
        return customFileLogHandler;
    }
}
