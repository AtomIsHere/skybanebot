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

package com.github.atomishere.skybanebot.cache;

import com.github.atomishere.skybanebot.SkybaneBot;
import com.github.atomishere.skybanebot.service.AbstractService;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.scheduler.BukkitTask;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class CacheManager extends AbstractService {
    private final Map<Class<?>, ICache<?>> caches = new HashMap<>();

    private BukkitTask cacheUpdateTask;

    public CacheManager(SkybaneBot plugin) {
        super(plugin);
    }

    @Override
    public void onStart() {
        cacheUpdateTask = Bukkit.getServer().getScheduler().runTaskTimerAsynchronously(plugin, () -> caches.values().forEach(ICache::updateCache), 20L, 72000L);
    }

    @Override
    public void onStop() {
        cacheUpdateTask.cancel();
        caches.values().forEach(c -> {
            try {
                plugin.getConfigHandler().saveConfigValues(c, c.getName());
            } catch (IOException | InvalidConfigurationException e) {
                e.printStackTrace();
            }

            c.clearCache();
        });
        caches.clear();
    }

    public <T> /* ensure same type by using generics */ void registerCache(Class<T> cacheClass, ICache<T> cache) {
        if(caches.containsKey(cacheClass)) {
            return;
        }

        try {
            plugin.getConfigHandler().injectConfigValues(cache, cache.getName());
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }

        caches.put(cacheClass, cache);
    }

    @SuppressWarnings("unchecked")
    public <T, C extends ICache<T>> C getCache(Class<T> typeClass) {
        return (C) caches.get(typeClass);
    }
}
