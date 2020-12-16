/*
 * Copyright (C) 2020 Archie O'Connor
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.github.atomishere.skybanebot.log;

import com.github.atomishere.skybanebot.error.ErrorHandler;
import com.github.atomishere.skybanebot.service.IService;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.appender.RollingRandomAccessFileAppender;

import java.io.PrintStream;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;

@RequiredArgsConstructor
public class LoggingManager implements IService {
    private static final Logger log = Logger.getLogger(LoggingManager.class.getName());
    private Logger parent;
    private Level level = Level.INFO;

    private final ErrorHandler errorHandler;

    private boolean started = false;

    @Override
    public void start() {
        if(started) {
            log.info("Logging has already been taken over...");
            return;
        }

        System.out.println("[SkybaneBot] Taking over logging...");
        if(errorHandler == null) {
            System.err.println("ERRORHANDLER IS NULL, ABORTING");
            started = true;
            return;
        }

        java.util.logging.LogManager manager = java.util.logging.LogManager.getLogManager();
        Enumeration<String> names = manager.getLoggerNames();
        while(names.hasMoreElements()) {
            String name = names.nextElement();
            if(name.startsWith("com.github.atomishere")) {
                Logger logger = Logger.getLogger(name);
                logger.setUseParentHandlers(true);
            }
        }
        parent = Logger.getLogger("com.github.atomishere");

        org.apache.logging.log4j.core.Logger log4j = (org.apache.logging.log4j.core.Logger) LogManager.getLogger("Minecraft");
        java.util.Optional<Appender> appender = log4j.getContext().getConfiguration().getAppenders().values().stream().filter(app -> app instanceof RollingRandomAccessFileAppender).findAny();
        RollingRandomAccessFileAppender log4jAppender = null;
        if(appender.isPresent()) {
            log4jAppender = (RollingRandomAccessFileAppender) appender.get();
        } else {
            log.warning("COULD NOT FIND LOG4J APPENDER! FILE LOGGING IS DISABLED!");
        }
        LogFormatter logFormatter = new LogFormatter(log4jAppender, errorHandler);

        Logger global = Logger.getLogger("");
        global.setUseParentHandlers(false);
        for(java.util.logging.Handler handler : global.getHandlers()) {
            global.removeHandler(handler);
        }

        log4j.getContext().getConfiguration().getAppenders().values().forEach(log4j::removeAppender);

        ((org.apache.logging.log4j.core.Logger) LogManager.getRootLogger()).addFilter(new Log4JForwardHandler(logFormatter));

        global.addHandler(new JULForwardHandler(logFormatter));

        System.setOut(new PrintStream(new SoutForwardHandler(), true));
        System.setErr(new PrintStream(new SoutForwardHandler(), true));

        started = true;
    }

    @Override
    public void stop() {
        if(started) {
            started = false;
        }
    }

    @Override
    public boolean isStarted() {
        return started;
    }
}
