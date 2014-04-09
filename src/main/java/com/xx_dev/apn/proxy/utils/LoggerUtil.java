package com.xx_dev.apn.proxy.utils;

import org.apache.log4j.Logger;

/**
 * @author xmx
 * @version $Id: com.xx_dev.apn.proxy.utils.LoggerUtil 2014-04-09 14:13 (xmx) Exp $
 */
public class LoggerUtil {
    private LoggerUtil() {
        //
    }

    public static void debug(Logger logger, Object... obj) {
        if (logger.isDebugEnabled()) {
            logger.debug(convertLogStr(obj));
        }
    }

    public static void info(Logger logger, Object... obj) {
        if (logger.isInfoEnabled()) {
            logger.info(convertLogStr(obj));
        }
    }

    public static void warn(Logger logger, Object... obj) {
        logger.warn(convertLogStr(obj));
    }

    public static void warn(Logger logger, Throwable t, Object... obj) {
        if (t == null) {
            logger.warn(convertLogStr(obj));
        } else {
            logger.warn(convertLogStr(obj), t);
        }
    }

    public static void error(Logger logger, Object... obj) {
        logger.error(convertLogStr(obj));
    }

    public static void error(Logger logger, Throwable t, Object... obj) {
        if (t == null) {
            logger.error(convertLogStr(obj));
        } else {
            logger.error(convertLogStr(obj), t);
        }
    }

    private static String convertLogStr(Object[] obj) {
        if (obj == null || obj.length == 0) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < obj.length; i++) {
            if (i > 0) {
                sb.append(", ");
            }

            sb.append(obj[i]);
        }
        return sb.toString();
    }

}
