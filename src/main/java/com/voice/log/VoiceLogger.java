package com.voice.log;

import com.voice.VoiceMod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.MessageFormat;

public class VoiceLogger {
    private static final Logger logger = LogManager.getLogger(VoiceMod.MODID);


    public static void info(String info, Object... args) {
        if (args == null) {
            logger.info(info);
        } else {
            logger.info(MessageFormat.format(info, args));
        }
    }

    public static void debug(String info, Object... args) {
        if (args == null) {
            logger.debug(info);
        } else {
            logger.debug(MessageFormat.format(info, args));
        }
    }

    public static void warn(String info, Object[] args) {
        if (args != null) {
            logger.warn(MessageFormat.format(info, args));
        } else {
            logger.warn(info);
        }
    }

    public static void error(String info, Object... args) {
        if (args == null) {
            logger.error(info);
        } else {
            logger.error(MessageFormat.format(info, args));
        }
    }

    public static void fatal(String info, Object... args) {
        if (args == null) {
            logger.fatal(info);
        } else {
            logger.fatal(MessageFormat.format(info, args));
        }
    }
}
