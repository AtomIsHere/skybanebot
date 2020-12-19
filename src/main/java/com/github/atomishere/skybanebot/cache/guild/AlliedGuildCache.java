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

package com.github.atomishere.skybanebot.cache.guild;

import com.github.atomishere.skybanebot.SkybaneBot;
import com.github.atomishere.skybanebot.cache.ICache;
import com.github.atomishere.skybanebot.config.ConfigContainer;
import com.github.atomishere.skybanebot.config.ConfigField;
import lombok.RequiredArgsConstructor;
import net.hypixel.api.reply.GuildReply;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

@RequiredArgsConstructor
public class AlliedGuildCache implements ICache<UUID> {
    private static final Logger logger = Logger.getLogger(AlliedGuildCache.class.getName());

    private final Set<String> guildIds = new HashSet<>();
    private final Set<UUID> alliedMembers = ConcurrentHashMap.newKeySet();

    private final SkybaneBot plugin;
    @ConfigField
    private ConfigContainer config;

    public void init() {
        guildIds.addAll(config.getConfig().getStringList("guildIds"));
    }

    @Override
    public void updateCache() {
        logger.info("Updating allied guild cache");

        clearCache();

        for(String guildId : guildIds) {
            GuildReply guild;
            try {
                guild = plugin.getHypixelApiManager().getApi().getGuildById(guildId).get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
                continue;
            }

            guild.getGuild().getMembers().stream().map(GuildReply.Guild.Member::getUuid).forEach(alliedMembers::add);
        }
    }

    @Override
    public void clearCache() {
        alliedMembers.clear();
    }

    @Override
    public Class<UUID> getType() {
        return UUID.class;
    }

    @Override
    public Collection<UUID> getValues() {
        return alliedMembers;
    }
}
