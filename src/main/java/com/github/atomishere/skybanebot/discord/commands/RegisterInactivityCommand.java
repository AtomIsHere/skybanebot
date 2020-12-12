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
import com.github.atomishere.skybanebot.inactivity.Inactive;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

import java.text.ParseException;
import java.util.Date;

public class RegisterInactivityCommand extends Command {
    private static final String USAGE = "Usage: <username> <date>";

    private final SkybaneBot plugin;
    private final GuildCache cache;

    public RegisterInactivityCommand(SkybaneBot plugin) {
        this.plugin = plugin;
        this.cache = plugin.getCacheManager().getCacheFromClass(GuildCache.class);

        this.name = "registerInactivity";
        this.help = "Register for inactivity";
        this.guildOnly = true;
    }

    @Override
    protected void execute(CommandEvent event) {
        String[] args = event.getArgs().split(" ");
        if(args.length != 2) {
            event.reply(USAGE);
            return;
        }

        String username = args[0];

        if(cache.getValues().stream().noneMatch(gm -> gm.getUsername().equalsIgnoreCase(username))) {
            event.reply("Could not find username! Try waiting at least an hour for the cache to update.");
            return;
        }

        Date endDate;
        try {
            endDate = plugin.getInactivityManager().getDateFormatter().parse(args[1]);
        } catch (ParseException e) {
            event.reply("Invalid date format, use: `yyyy-MM-dd`");
            return;
        }

        if(endDate.before(new Date(System.currentTimeMillis()))) {
            event.reply("Invalid date, make sure the date you entered is after the current date!");
            return;
        }

        if(plugin.getInactivityManager().isInactive(username)) {
            event.reply("You have already registered for inactivity!");
            return;
        }

        plugin.getInactivityManager().registerInactive(new Inactive(username, endDate));
        event.reply("Inactivity registered!");
    }
}
