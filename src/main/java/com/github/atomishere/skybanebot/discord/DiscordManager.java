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

package com.github.atomishere.skybanebot.discord;

import com.github.atomishere.skybanebot.SkybaneBot;
import com.github.atomishere.skybanebot.cache.guild.GuildCache;
import com.github.atomishere.skybanebot.config.ConfigurationValue;
import com.github.atomishere.skybanebot.discord.commands.GetInactiveMembersCommand;
import com.github.atomishere.skybanebot.discord.commands.RegisterInactivityCommand;
import com.github.atomishere.skybanebot.service.AbstractService;
import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.command.CommandClientBuilder;
import github.scarsz.discordsrv.DiscordSRV;

public class DiscordManager extends AbstractService {
    private static final String OWNER_ID = "332423993132974081";

    private CommandClient client;

    @ConfigurationValue
    private int requiredXp = 25000;

    public DiscordManager(SkybaneBot plugin) {
        super(plugin);
    }

    @Override
    public void onStart() {
        GuildCache cache = plugin.getCacheManager().getCacheFromClass(GuildCache.class);

        client = new CommandClientBuilder()
                .setOwnerId(OWNER_ID)
                .addCommands(new RegisterInactivityCommand(plugin, cache), new GetInactiveMembersCommand(requiredXp, plugin, cache))
                .build();

        DiscordSRV.getPlugin().getJda().addEventListener(client);
    }

    @Override
    public void onStop() {
        DiscordSRV.getPlugin().getJda().removeEventListener(client);
    }
}
