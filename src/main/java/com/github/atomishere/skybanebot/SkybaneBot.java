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
import com.github.atomishere.skybanebot.discord.DiscordChatLinker;
import com.github.atomishere.skybanebot.discord.DiscordManager;
import com.github.atomishere.skybanebot.error.ErrorHandler;
import com.github.atomishere.skybanebot.inactivity.InactivityManager;
import com.github.atomishere.skybanebot.log.LoggingManager;
import com.github.atomishere.skybanebot.service.ServiceManager;
import com.github.atomishere.skybanebot.teleport.NetherTeleportManager;
import com.github.atomishere.skybanebot.teleport.TeleportGui;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

public class SkybaneBot extends JavaPlugin {
    private ErrorHandler errorHandler;
    private LoggingManager loggingManager;

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
    public void onLoad() {
        errorHandler = new ErrorHandler();
        errorHandler.start();
    }

    @Override
    public void onEnable() {
        loggingManager = new LoggingManager(errorHandler);
        loggingManager.start();

        configHandler = new ConfigHandler(getDataFolder());
        configHandler.init();

        serviceManager = new ServiceManager(this);
        //Register services
        hypixelApiManager = serviceManager.registerService(HypixelApiManager.class);
        mojangApiManager = serviceManager.registerService(MojangApiManager.class);

        cacheManager = serviceManager.registerService(CacheManager.class);

        serviceManager.registerService(WhitelistManager.class);
        serviceManager.registerService(NetherTeleportManager.class);
        serviceManager.registerService(TeleportGui.class);

        inactivityManager = serviceManager.registerService(InactivityManager.class);
        discordManager = serviceManager.registerService(DiscordManager.class);
        serviceManager.registerService(DiscordChatLinker.class);

        //
        serviceManager.startServices();
    }

    @Override
    public void onDisable() {
        serviceManager.stopServices();
        configHandler.saveConfigs();
        serviceManager.clearServices();

        errorHandler.stop();
        loggingManager.stop();
    }
}
