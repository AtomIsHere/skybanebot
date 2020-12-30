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

package com.github.atomishere.skybanebot.discord.commands;

import com.github.atomishere.skybanebot.SkybaneBot;
import com.github.atomishere.skybanebot.cache.guild.GuildCache;
import com.github.atomishere.skybanebot.cache.guild.GuildMember;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.Permission;

import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class ReputationLeaderboardCommand extends Command {
    private final GuildCache cache;
    private final SkybaneBot plugin;

    public ReputationLeaderboardCommand(SkybaneBot plugin) {
        this.cache = plugin.getCacheManager().getCacheFromClass(GuildCache.class);
        this.plugin = plugin;

        this.name = "reputationLeaderboard";
        this.help = "Get the reputation leaderboard to find who has the highest reputation";
        this.guildOnly = true;

        this.userPermissions = new Permission[]{Permission.MESSAGE_MANAGE};
    }

    @Override
    protected void execute(CommandEvent event) {
        StringBuilder leaderboardBuilder = new StringBuilder();
        AtomicInteger count = new AtomicInteger();

        plugin.getReputationManager()
                .getReputations()
                .entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue())
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .forEach(e -> leaderboardBuilder.append(count.incrementAndGet())
                        .append(". ")
                        .append(cache.getValues()
                                .stream()
                                .filter(gm -> gm.getMemberUUID().equals(e.getKey()))
                                .map(GuildMember::getUsername)
                                .findAny()
                                .orElse(e.getKey().toString()))
                        .append(": ")
                        .append(e.getValue())
                        .append("\n"));

        event.reply(leaderboardBuilder.toString());
    }
}
