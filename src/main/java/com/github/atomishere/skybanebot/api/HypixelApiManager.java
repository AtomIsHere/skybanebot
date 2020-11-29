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

package com.github.atomishere.skybanebot.api;

import com.github.atomishere.skybanebot.SkybaneBot;
import com.github.atomishere.skybanebot.config.ConfigurationValue;
import com.github.atomishere.skybanebot.service.AbstractService;
import lombok.Getter;
import net.hypixel.api.HypixelAPI;

import java.util.UUID;

public class HypixelApiManager extends AbstractService {
    @ConfigurationValue
    private String apiKey = UUID.randomUUID().toString();

    @Getter
    private HypixelAPI api;

    public HypixelApiManager(SkybaneBot plugin) {
        super(plugin);
    }

    @Override
    public void onStart() {
        api = new HypixelAPI(UUID.fromString(apiKey));
    }

    @Override
    public void onStop() {
        api = null;
    }
}
