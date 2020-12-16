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
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.RollingRandomAccessFileAppender;
import org.apache.logging.log4j.core.impl.Log4jLogEvent;
import org.apache.logging.log4j.core.impl.MutableLogEvent;
import org.apache.logging.log4j.message.Message;

import javax.print.attribute.standard.Severity;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LogFormatter {
    private static final SimpleDateFormat hourFormat = new SimpleDateFormat("kk:mm:ss:SSS");
    private final PrintStream sout = new PrintStream(new FileOutputStream(FileDescriptor.out));

    private final RollingRandomAccessFileAppender log4jAppender;
    private ErrorHandler errorHandler;

    public LogFormatter(RollingRandomAccessFileAppender log4jAppender, ErrorHandler errorHandler) {
        this.log4jAppender = log4jAppender;
        this.errorHandler = errorHandler;
    }

    public void log(long millis, String levelName, String loggerName, String message, Throwable throwable) {
        levelName = formatLevel(levelName, message);
        loggerName = formatLoggerName(loggerName, message);
        message = formatMessage(message);

        // print out to sout
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(hourFormat.format(new Date(millis))).append(" ").append(levelName).append("]:");
        sb.append("<").append(loggerName).append("> ");
        sb.append(message);
        sout.println(sb.toString());

        Level level = toLog4j(levelName);
        if(log4jAppender != null) {
            Message log4jMessage = new MutableLogEvent(new StringBuilder(message), new Object[0]);
            LogEvent log4jEvent = Log4jLogEvent.newBuilder().setMessage(log4jMessage).setTimeMillis(millis).setLevel(level).setLoggerName(loggerName).build();
            log4jAppender.append(log4jEvent);
        }

        if(throwable != null) {
            // forward to error handler
            if(throwable instanceof Exception) {
                Exception exception = (Exception) throwable;
                if(level.equals(Level.WARN)) {
                    errorHandler.handle(exception, Severity.WARNING, false);
                } else {
                    errorHandler.handle(exception, Severity.ERROR, false);
                }
            }

            throwable.printStackTrace(sout);

            if(log4jAppender != null) {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);

                throwable.printStackTrace(pw);

                Message log4jMessage = new MutableLogEvent(new StringBuilder(sw.toString()), new Object[0]);
                LogEvent log4jEvent = Log4jLogEvent.newBuilder().setMessage(log4jMessage).setTimeMillis(millis).setLevel(level).setLoggerName(loggerName).build();
                log4jAppender.append(log4jEvent);
            }
        }
    }

    public Level toLog4j(String level) {
        switch(level) {
            case "SEVERE":
                return org.apache.logging.log4j.Level.ERROR;
            case "WARNING":
                return org.apache.logging.log4j.Level.WARN;
            case "INFO":
                return org.apache.logging.log4j.Level.INFO;
            case "CONFIG":
                return org.apache.logging.log4j.Level.DEBUG;
            case "FINE":
                return org.apache.logging.log4j.Level.DEBUG;
            case "FINER":
                return org.apache.logging.log4j.Level.TRACE;
            case "FINEST":
                return org.apache.logging.log4j.Level.ALL;
            default:
                return org.apache.logging.log4j.Level.INFO;
        }
    }

    private String formatLevel(String level, String msg) {
        if(msg.startsWith("Hibernate:")) {
            return "Finer ";
        }

        return StringUtils.rightPad(level.replace("WARNING", "WARN"), 6);
    }

    private String formatMessage(String message) {
        if(message.endsWith("\r\n")) {
            message = message.substring(0, message.length() - 2);
        } else if(message.endsWith("\n")) {
            message = message.substring(0, message.length() - 1);
        }

        if(message.startsWith("Hibernate:")) {
            return message.replace("Hibernate: ", "");
        }
        return message;
    }

    private String formatLoggerName(String name, String msg) {
        if(name == null || name.length() == 0) {
            if(msg.startsWith("Hibernate:")) {
                return "Hibernate   ";
            }

            return "Unknown   ";
        }

        if(name.contains("skybanebot")) {
            return "SkybaneBot";
        } else if(name.contains("hibernate")) {
            return "Hibernate   ";
        } else if(name.contains("net.minecraft.server")) {
            return "Minecraft   ";
        }

        return StringUtils.rightPad(name, 13);
    }
}
