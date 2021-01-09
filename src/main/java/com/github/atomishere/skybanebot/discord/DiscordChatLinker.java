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
import com.github.atomishere.skybanebot.config.ConfigurationValue;
import com.github.atomishere.skybanebot.service.AbstractService;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.logging.Logger;

public class DiscordChatLinker extends AbstractService implements Listener, EventListener {
    private static final Logger logger = Logger.getLogger(DiscordChatLinker.class.getName());

    @ConfigurationValue
    private String guildId = "628246551881580585";
    @ConfigurationValue
    private String channelId = "746671552929726474";

    private TextChannel chatChannel = null;

    public DiscordChatLinker(SkybaneBot plugin) {
        super(plugin);
    }

    @Override
    public void onStart() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            Guild guild;
            try {
                guild = plugin.getDiscordManager().getJda().awaitReady().getGuildById(guildId);
            } catch (InterruptedException e) {
                e.printStackTrace();
                return;
            }

            if(guild == null) {
                logger.warning("Could not find guild, not enabling chat linking.");
                return;
            }

            TextChannel chatChannel = guild.getTextChannelById(channelId);
            if(chatChannel == null) {
                logger.warning("Could not find chat channel, not enabling chat linking.");
                return;
            }

            this.chatChannel = chatChannel;
            plugin.getDiscordManager().getJda().addEventListener(this);

            chatChannel.sendMessage("Server has started!").complete();
        });
    }

    @Override
    public void onStop() {
        chatChannel.sendMessage("Server is stopping!").complete();
        chatChannel = null;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        if(!event.isCancelled() && chatChannel != null && !event.getMessage().contains("@")) {
            chatChannel.sendMessage(ChatColor.stripColor(event.getPlayer().getDisplayName() + " > " + event.getMessage())).submit();
        }
    }

    @Override
    public void onEvent(@NotNull GenericEvent event) {
        if(event instanceof GuildMessageReceivedEvent) {
            GuildMessageReceivedEvent messageEvent = (GuildMessageReceivedEvent) event;

            if(!messageEvent.isWebhookMessage() && !messageEvent.getMember().getUser().isBot() && messageEvent.getChannel().equals(chatChannel)) {
                Bukkit.getServer().broadcastMessage(ChatColor.GRAY + "[" + ChatColor.BLUE + "DISCORD" + ChatColor.GRAY + "] <" + ChatColor.stripColor(messageEvent.getMember().getEffectiveName() + "> " + messageEvent.getMessage().getContentDisplay()));
            }
        }
    }
}
