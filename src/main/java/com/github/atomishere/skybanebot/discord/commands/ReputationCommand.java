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
import com.github.atomishere.skybanebot.discord.rep.RepRequest;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

import java.util.UUID;

public class ReputationCommand extends Command {
    private static final String USAGE = "Usage: <ign> <reason>";

    private final SkybaneBot plugin;
    private final GuildCache cache;

    public ReputationCommand(SkybaneBot plugin) {
        this.plugin = plugin;
        this.cache = plugin.getCacheManager().getCacheFromClass(GuildCache.class);

        this.name = "reputation";
        this.help = "Submit a reputation request for staff";
        this.guildOnly = true;
    }

    @Override
    protected void execute(CommandEvent event) {
        String[] args = event.getArgs().split(" ");
        if(args.length < 2) {
            event.reply(USAGE);
            return;
        }

        UUID target = cache.getValues()
                .stream()
                .filter(gm -> gm.getUsername().equalsIgnoreCase(args[0]))
                .map(GuildMember::getMemberUUID)
                .findAny()
                .orElse(null);

        if(target == null) {
            event.reply("Could not find username! Please wait an hour for the cache to update.");
            return;
        }

        plugin.getReputationListener().addRequest(new RepRequest(target, event.getMember(), event.getMessage()));
        event.reply("Request Submitted!");
    }
}
