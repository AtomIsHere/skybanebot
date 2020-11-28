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

package com.github.atomishere.skybanebot.exceptions;

import com.github.atomishere.skybanebot.service.IService;

public class ServiceNotFoundException extends SkybaneBotException {
    public ServiceNotFoundException(Class<? extends IService> serviceClass) {
        this(serviceClass.getSimpleName());
    }

    public ServiceNotFoundException(String serviceName) {
        super(serviceName + " not found");
    }
}
