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
import com.github.atomishere.skybanebot.cache.guild.AlliedGuildCache;
import com.github.atomishere.skybanebot.cache.guild.GuildCache;
import com.github.atomishere.skybanebot.service.AbstractService;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.scheduler.BukkitTask;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

public class CacheManager extends AbstractService {
    private static final Logger logger = Logger.getLogger(CacheManager.class.getName());

    private final Set<ICache<?>> caches = new HashSet<>();

    private BukkitTask cacheUpdateTask;

    public CacheManager(SkybaneBot plugin) {
        super(plugin);
    }

    @Override
    public void onStart() {
        registerCache(new GuildCache(plugin));

        // Inject config fields then init
        AlliedGuildCache alliedGuildCache = new AlliedGuildCache(plugin);
        registerCache(alliedGuildCache);
        alliedGuildCache.init();

        cacheUpdateTask = Bukkit.getServer().getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            caches.forEach(ICache::updateCache);
            logger.info("Cache updated!");
        }, 20L, 72000L);
    }

    @Override
    public void onStop() {
        cacheUpdateTask.cancel();
        caches.forEach(c -> {
            try {
                plugin.getConfigHandler().saveConfigValues(c, c.getName());
            } catch (IOException | InvalidConfigurationException e) {
                e.printStackTrace();
            }

            c.clearCache();
        });
        caches.clear();
    }

    public void registerCache(ICache<?> cache) {
        if(caches.stream().anyMatch(c -> c.getName().equals(cache.getName()))) {
            return;
        }

        try {
            plugin.getConfigHandler().injectConfigValues(cache, cache.getName());
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }

        caches.add(cache);
    }

    @SuppressWarnings("unchecked")
    public <T, C extends ICache<T>> C getCacheFromClass(Class<C> cacheClass) {
        return (C) caches.stream()
                .filter(c -> cacheClass.isInstance(cacheClass))
                .findFirst()
                .orElse(null);
    }

    public ICache<?> getCacheFromName(String name) {
        return caches.stream()
                .filter(c -> c.getName().equals(name))
                .findFirst()
                .orElse(null);
    }
}
