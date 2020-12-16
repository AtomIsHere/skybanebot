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

import com.github.atomishere.skybanebot.SkybaneBot;
import lombok.RequiredArgsConstructor;

import javax.print.attribute.standard.Severity;
import java.util.logging.Level;
import java.util.logging.Logger;

@RequiredArgsConstructor
public class ErrorHandler {
    private static final Logger log = Logger.getLogger(ErrorHandler.class.getName());

    private final SkybaneBot plugin;

    public void start() {
        //TODO: Integrate sentry support.

        log.warning("Sentry currently not implemented, will not report errors.");
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
    }
}
