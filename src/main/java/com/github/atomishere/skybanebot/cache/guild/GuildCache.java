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

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;

@RequiredArgsConstructor
public class GuildCache implements ICache<GuildMember> {
    private final SkybaneBot plugin;
    private final List<GuildMember> members = new CopyOnWriteArrayList<>();

    @ConfigurationValue
    private String guildId;

    @Override
    public void updateCache() {
        GuildReply guild;

        try {
            guild = plugin.getHypixelApiManager().getApi().getGuildById(this.guildId).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return;
        }

        clearCache();
        guild.getGuild()
                .getMembers()
                .stream()
                .map(this::createMember)
                .forEach(this.members::add);
    }

    private GuildMember createMember(GuildReply.Guild.Member member) {
        int xp = 0;
        for(int day : member.getExpHistory().values()) {
            xp += day;
        }

        return new GuildMember(member.getUuid(), xp);
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
