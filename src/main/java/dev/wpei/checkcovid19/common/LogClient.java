package dev.wpei.checkcovid19.common;

import org.slf4j.Logger;

public class LogClient {
    public static void info(Logger logger, String message) {
        logger.info(message);
    }
    public static void warn(Logger logger, String message) {
        logger.warn(message);
    }
    public static void error(Logger logger, String message) {
        logger.error(message);
    }
    public static void debug(Logger logger, String message) {
        logger.debug(message);
    }
}
