/*
 *  Copyright (C) 2008-2013  John-Paul.Stanford <dev@stanwood.org.uk>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.stanwood.media.logging;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Writes JDK log messages to commons logging.
 */
public class JavaLoggingToCommonLoggingRedirector {
        static JDKLogHandler activeHandler;

        /**
         * Activates this feature.
         */
        public static void activate() {
                try {
                        Logger rootLogger = LogManager.getLogManager().getLogger("");
                        // remove old handlers
                        for (Handler handler : rootLogger.getHandlers()) {
                                rootLogger.removeHandler(handler);
                        }
                        // add our own
                        activeHandler = new JDKLogHandler();
                        activeHandler.setLevel(Level.ALL);
                        rootLogger.addHandler(activeHandler);
                        rootLogger.setLevel(Level.ALL);
                        // done, let's check it right away!!!

                } catch (Exception exc) {
                        LogFactory.getLog(JavaLoggingToCommonLoggingRedirector.class).error("activation failed", exc);
                }
        }

        public static void deactivate() {
                Logger rootLogger = LogManager.getLogManager().getLogger("");
                rootLogger.removeHandler(activeHandler);

                Logger.getLogger(JavaLoggingToCommonLoggingRedirector.class.getName()).info("dactivated");
        }

        protected static class JDKLogHandler extends Handler {
                private Map<String, Log> cachedLogs = new ConcurrentHashMap<String, Log>();

                private Log getLog(String logName) {
                        Log log = cachedLogs.get(logName);
                        if (log == null) {
                                log = LogFactory.getLog(logName);
                                cachedLogs.put(logName, log);
                        }
                        return log;
                }

                @Override
                public void publish(LogRecord record) {
                        Log log = getLog(record.getLoggerName());
                        String message = record.getMessage();
                        Throwable exception = record.getThrown();
                        Level level = record.getLevel();
                        if (level == Level.SEVERE) {
                                log.error(message, exception);
                        } else if (level == Level.WARNING) {
                                log.warn(message, exception);
                        } else if (level == Level.INFO) {
                                log.info(message, exception);
                        } else if (level == Level.CONFIG) {
                                log.debug(message, exception);
                        } else {
                                log.trace(message, exception);
                        }
                }

                @Override
                public void flush() {
                        // nothing to do
                }

                @Override
                public void close() {
                        // nothing to do
                }
        }
}