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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class SoutForwardHandler extends ByteArrayOutputStream {
    private final String separator = System.getProperty("line.separator");
    private final Logger logger = LogManager.getLogManager().getLogger("");

    @Override
    public void flush() throws IOException {
        synchronized (this) {
            super.flush();
            String record = this.toString();
            super.reset();

            if(record.length() > 0 && !record.equals(this.separator)) {
                this.logger.info(record);
            }
        }
    }
}
