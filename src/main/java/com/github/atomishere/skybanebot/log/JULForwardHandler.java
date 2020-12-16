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

import java.util.logging.ConsoleHandler;
import java.util.logging.LogRecord;

public class JULForwardHandler extends ConsoleHandler {
    private final LogFormatter logFormatter;

    public JULForwardHandler(LogFormatter logFormatter) {
        this.logFormatter = logFormatter;
    }

    @Override
    public void publish(LogRecord record) {
        logFormatter.log(record.getMillis(), record.getLevel().getName(), record.getLoggerName(), record.getMessage(), record.getThrown());
    }

    @Override
    public void flush() {
    }

    @Override
    public void close() {
    }
}
