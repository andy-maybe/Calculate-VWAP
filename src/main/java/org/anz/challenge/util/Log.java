package org.anz.challenge.util;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Log {
    private static final Logger logger = LogManager.getLogger();

    private Log() {

    }

    public static Logger getLogger() {
        return logger;
    }

    public static void info(String message) {
        logger.info(message);
    }

    public static void debug(String message) {
        logger.debug(message);
    }

    public static void error(String message) {
        logger.error(message);
    }

    public static void error(String message, Object object) {
        logger.error(message, object);
    }

    public static void newLine() {
        logger.log(Level.ALL, "/n");
    }
}
