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

package com.github.atomishere.skybanebot.service;

import com.github.atomishere.skybanebot.SkybaneBot;
import com.github.atomishere.skybanebot.exceptions.ServiceStateConflictException;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import java.io.IOException;

@RequiredArgsConstructor
public abstract class AbstractService implements IService {
    protected final SkybaneBot plugin;

    private boolean started = false;

    @Override
    public void start() {
        if(started) {
            throw new ServiceStateConflictException(getName(), ServiceStateConflictException.State.STARTED);
        }

        try {
            plugin.getConfigHandler().injectConfigValues(this, this.getName());
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }

        if(this instanceof Listener) {
            Bukkit.getServer().getPluginManager().registerEvents((Listener) this, plugin);
        }

        onStart();
        started = true;
    }

    @Override
    public void stop() {
        if(!started) {
            throw new ServiceStateConflictException(getName(), ServiceStateConflictException.State.STOPPED);
        }

        if(this instanceof Listener) {
            HandlerList.unregisterAll((Listener) this);
        }

        onStop();
        try {
            plugin.getConfigHandler().saveConfigValues(this, this.getName());
        } catch (InvalidConfigurationException | IOException e) {
            e.printStackTrace();
            return;
        }

        started = false;
    }

    @Override
    public boolean isStarted() {
        return started;
    }

    public abstract void onStart();
    public abstract void onStop();
}
