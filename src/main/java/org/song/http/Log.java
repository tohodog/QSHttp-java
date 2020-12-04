
package org.song.http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author song
 * @Email vipqinsong@gmail.com
 * @date 2018年8月6日
 * <p>
 * 服务器日记
 */
public class Log {
    public static Logger LOGGER = LoggerFactory.getLogger("QSHTTP");

    public static void e(String msg) {
        LOGGER.error(String.valueOf(msg));
    }

    public static void e(String msg, Throwable t) {
        LOGGER.error(msg, t);
    }

    public static void e(String tag, String msg) {
        e(tag + ": " + msg);
    }

    public static void e(String tag, String msg, Throwable t) {
        e(tag + ": " + msg, t);
    }

    public static void i(String tag, String msg) {
        i(tag + ": " + msg);
    }

    public static void i(Object msg) {
        LOGGER.info(String.valueOf(msg));
    }

    public static void w(String tag, String msg) {
        w(tag + ": " + msg);
    }

    public static void w(String msg) {
        LOGGER.warn(msg);
    }

    public static void d(String tag, String msg) {
        d(tag + ": " + msg);
    }

    public static void d(String msg) {
        LOGGER.debug(msg);
    }

}
