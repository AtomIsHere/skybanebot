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

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.ChatColor;
import org.bukkit.Material;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public enum TeleportLocation {
    AUSTRALIA_OCEANIA("Australia/Oceania", ChatColor.GOLD, Material.DIRT, 9637.0, 62.0, 8685.0),
    ASIA("Asia", ChatColor.BLUE, Material.STONE, 6804.0, 202.0, 4930.0),
    EUROPE("Europe", ChatColor.GREEN, Material.GRASS_BLOCK, 1860.0, 47.0, 3564.0),
    AFRICA("Africa", ChatColor.YELLOW, Material.SAND, 3139.0, 42.0, 6770.0),
    AMERICA("America", ChatColor.RED, Material.TNT, 16189.0, 44.0, 4464.0);

    public static final TeleportLocationDataType DATA_TYPE = new TeleportLocationDataType();

    private final String displayName;
    private final ChatColor displayColor;
    private final Material displayMat;
    private final double x;
    private final double y;
    private final double z;
}
