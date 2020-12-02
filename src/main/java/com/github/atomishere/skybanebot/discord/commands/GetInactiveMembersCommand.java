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
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.Permission;

public class GetInactiveMembersCommand extends Command {
    private final int requiredXp;

    private final SkybaneBot plugin;
    private final GuildCache cache;

    public GetInactiveMembersCommand(int requiredXp, SkybaneBot plugin, GuildCache cache) {
        this.requiredXp = requiredXp;

        this.plugin = plugin;
        this.cache = cache;

        this.botPermissions = new Permission[]{Permission.MESSAGE_MANAGE};
        this.name = "getInactiveMembers";
        this.help = "Get all members who have not reached the required amount of xp";
        this.guildOnly = true;
    }

    @Override
    protected void execute(CommandEvent event) {
        StringBuilder messageBuilder = new StringBuilder();
        messageBuilder.append("Inactive Members:");

        cache.getValues()
                .stream()
                .filter(g -> g.getWeeklyXp() >= requiredXp)
                .filter(g -> !plugin.getInactivityManager().isInactive(g.getUsername()))
                .forEach(g -> messageBuilder.append("\n    ").append(g.getUsername()).append(": ").append(g.getWeeklyXp()));

        event.reply(messageBuilder.toString());
    }
}
