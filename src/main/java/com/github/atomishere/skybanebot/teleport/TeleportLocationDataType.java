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

package com.github.atomishere.skybanebot.teleport;

import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;

public class TeleportLocationDataType implements PersistentDataType<byte[], TeleportLocation> {
    @NotNull
    public Class<byte[]> getPrimitiveType() {
        return byte[].class;
    }

    @NotNull
    public Class<TeleportLocation> getComplexType() {
        return TeleportLocation.class;
    }

    public byte @NotNull [] toPrimitive(@NotNull TeleportLocation complex, @NotNull PersistentDataAdapterContext context) {
        return complex.name().getBytes(StandardCharsets.UTF_8);
    }

    @NotNull
    public TeleportLocation fromPrimitive(byte @NotNull [] primitive, @NotNull PersistentDataAdapterContext context) {
        return TeleportLocation.valueOf(new String(primitive, StandardCharsets.UTF_8));
    }
}
