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
import com.github.atomishere.skybanebot.config.ConfigurationValue;
import lombok.RequiredArgsConstructor;
import net.hypixel.api.reply.GuildReply;
import org.shanerx.mojang.PlayerProfile;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

@RequiredArgsConstructor
public class GuildCache implements ICache<GuildMember> {
    private static final Logger logger = Logger.getLogger(GuildCache.class.getName());

    private final SkybaneBot plugin;
    private final List<GuildMember> members = new CopyOnWriteArrayList<>();

    @ConfigurationValue
    private String guildId = "5d9219f577ce8436b66ad36a";

    @Override
    public void updateCache() {
        logger.info("Updating guild cache");

        GuildReply guild;

        try {
            guild = plugin.getHypixelApiManager().getApi().getGuildById(this.guildId).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return;
        }

        clearCache();
        for(GuildReply.Guild.Member member : guild.getGuild().getMembers()) {
            PlayerProfile profile = plugin.getMojangApiManager().getMojang().getPlayerProfile(member.getUuid().toString());
            if (profile != null) {
                members.add(createMember(member, profile.getUsername()));
            } else {
                members.add(createMember(member, null));
            }
        }
    }

    private GuildMember createMember(GuildReply.Guild.Member member, String username) {
        int xp = 0;
        for(int day : member.getExpHistory().values()) {
            xp += day;
        }

        return new GuildMember(member.getUuid(), username,  xp);
    }

    @Override
    public void clearCache() {
        members.clear();
    }

    @Override
    public Class<GuildMember> getType() {
        return GuildMember.class;
    }

    @Override
    public Collection<GuildMember> getValues() {
        return members;
    }
}
