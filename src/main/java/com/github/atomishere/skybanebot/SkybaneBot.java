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

package com.github.atomishere.skybanebot;

import com.github.atomishere.skybanebot.api.HypixelApiManager;
import com.github.atomishere.skybanebot.api.MojangApiManager;
import com.github.atomishere.skybanebot.cache.CacheManager;
import com.github.atomishere.skybanebot.config.ConfigHandler;
import com.github.atomishere.skybanebot.discord.DiscordManager;
import com.github.atomishere.skybanebot.inactivity.InactivityManager;
import com.github.atomishere.skybanebot.service.ServiceManager;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

public class SkybaneBot extends JavaPlugin {
    @Getter
    private ConfigHandler configHandler;
    private ServiceManager serviceManager;

    @Getter
    private HypixelApiManager hypixelApiManager;
    @Getter
    private MojangApiManager mojangApiManager;

    @Getter
    private CacheManager cacheManager;

    @Getter
    private InactivityManager inactivityManager;
    @Getter
    private DiscordManager discordManager;

    @Override
    public void onEnable() {
        configHandler = new ConfigHandler(getDataFolder());
        configHandler.init();

        serviceManager = new ServiceManager(this);
        //Register services
        hypixelApiManager = serviceManager.registerService(HypixelApiManager.class);
        mojangApiManager = serviceManager.registerService(MojangApiManager.class);

        cacheManager = serviceManager.registerService(CacheManager.class);

        serviceManager.registerService(WhitelistManager.class);

        inactivityManager = serviceManager.registerService(InactivityManager.class);
        discordManager = serviceManager.registerService(DiscordManager.class);

        //
        serviceManager.startServices();
    }

    @Override
    public void onDisable() {
        serviceManager.stopServices();
        configHandler.saveConfigs();
        serviceManager.clearServices();
    }
}
