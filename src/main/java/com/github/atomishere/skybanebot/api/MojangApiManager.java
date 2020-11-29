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
import com.github.atomishere.skybanebot.service.AbstractService;
import lombok.Getter;
import org.shanerx.mojang.Mojang;

public class MojangApiManager extends AbstractService {
    @Getter
    private Mojang mojang;

    public MojangApiManager(SkybaneBot plugin) {
        super(plugin);
    }

    @Override
    public void onStart() {
        mojang = new Mojang().connect();
    }

    @Override
    public void onStop() {
        mojang = null;
    }
}
