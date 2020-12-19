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

package com.github.atomishere.skybanebot.error;

import io.sentry.Sentry;

import javax.print.attribute.standard.Severity;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ErrorHandler {
    private static final Logger log = Logger.getLogger(ErrorHandler.class.getName());

    public void start() {
        Sentry.init(options -> {
            options.setDsn("https://23fcda70cdef48228df7c1881a0d2a45@o493752.ingest.sentry.io/5563672");
        });
    }

    public void stop() {
        //read start
    }

    @SuppressWarnings("DuplicateExpressions")
    public void handle(Exception ex, Severity severity, boolean shouldLog) {
        if(shouldLog) {
            log.log(severity.equals(Severity.ERROR) ? Level.SEVERE : Level.WARNING,
                    "Caught exception with level " + severity.getValue(), ex);
        } else {
            log.log(severity.equals(Severity.ERROR) ? Level.SEVERE : Level.WARNING,
                    "Caught exception with level " + severity.getValue());
        }

        Sentry.captureException(ex);
    }
}
