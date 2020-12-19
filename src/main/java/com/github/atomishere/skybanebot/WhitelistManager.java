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

import com.github.atomishere.skybanebot.cache.guild.AlliedGuildCache;
import com.github.atomishere.skybanebot.cache.guild.GuildCache;
import com.github.atomishere.skybanebot.cache.guild.GuildMember;
import com.github.atomishere.skybanebot.service.AbstractService;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.UUID;
import java.util.logging.Logger;

public class WhitelistManager extends AbstractService implements Listener {
    private static final Logger logger = Logger.getLogger(WhitelistManager.class.getName());

    private GuildCache guildCache;
    private AlliedGuildCache alliedGuildCache;

    public WhitelistManager(SkybaneBot plugin) {
        super(plugin);
    }

    @Override
    public void onStart() {
        guildCache = plugin.getCacheManager().getCacheFromClass(GuildCache.class);
        alliedGuildCache = plugin.getCacheManager().getCacheFromClass(AlliedGuildCache.class);
    }

    @Override
    public void onStop() {
        guildCache = null;
        alliedGuildCache = null;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if(!canAllow(event.getPlayer().getUniqueId())) {
            event.getPlayer().kickPlayer(ChatColor.RED + "You need to be in Skybane or in an allied guild to join this server!");
            logger.info(event.getPlayer().getName() + " tried to join the server but they aren't in the main or an allied guild!");
        }
    }

    private boolean canAllow(UUID playerUid) {
        return guildCache.getValues().stream().map(GuildMember::getMemberUUID).anyMatch(u -> u.equals(playerUid)) || alliedGuildCache.getValues().stream().anyMatch(u -> u.equals(playerUid));
    }
}
