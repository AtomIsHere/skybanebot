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

package com.github.atomishere.skybanebot.discord.rep;

import com.github.atomishere.skybanebot.SkybaneBot;
import com.github.atomishere.skybanebot.cache.guild.GuildCache;
import com.github.atomishere.skybanebot.cache.guild.GuildMember;
import com.github.atomishere.skybanebot.service.AbstractService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.requests.RestAction;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ReputationListener extends AbstractService implements EventListener {
    private static final Long CONFIRM_CHANNEL_ID = 793410833140678666L;

    private static final Long YES_EMOTE_ID = 764971241115090945L;
    private static final Long NO_EMOTE_ID = 764971240628682783L;

    private final Map<Long, RepRequest> awaitingRequests = new ConcurrentHashMap<>();

    private MessageChannel confirmationChannel;
    private GuildCache cache;

    private Emote yesEmote;
    private Emote noEmote;

    public ReputationListener(SkybaneBot plugin) {
        super(plugin);
    }

    @Override
    public void onStart() {
        plugin.getDiscordManager().getJda().addEventListener(this);

        confirmationChannel = plugin.getDiscordManager().getJda().getTextChannelById(CONFIRM_CHANNEL_ID);
        cache = plugin.getCacheManager().getCacheFromClass(GuildCache.class);

        yesEmote = plugin.getDiscordManager().getJda().getEmoteById(YES_EMOTE_ID);
        noEmote = plugin.getDiscordManager().getJda().getEmoteById(NO_EMOTE_ID);
    }

    @Override
    public void onStop() {
        awaitingRequests.keySet()
                .stream()
                .map(confirmationChannel::deleteMessageById)
                .forEach(RestAction::complete);
        awaitingRequests.clear();
    }

    public void addRequest(RepRequest request) {
        String username = cache.getValues()
                .stream()
                .filter(gm -> gm.getMemberUUID().equals(request.getTarget()))
                .map(GuildMember::getUsername)
                .findAny()
                .orElse(request.getTarget() + " (Could not find username in cache! Please copy and paste this into: https://namemc.com/ to get the username)");

        MessageEmbed embed = new EmbedBuilder()
                .setAuthor(request.getRequester().getUser().getName(), null, request.getRequester().getUser().getAvatarUrl())
                .setTitle("Reputation Request")
                .setDescription(request.getRequester().getEffectiveName() + " requests to add reputation to " + username)
                .addField("Original Message", "[Jump!](" + request.getMessage().getJumpUrl() + ")", false)
                .setFooter(request.getMessage().getId())
                .setTimestamp(Instant.now())
                .build();

        confirmationChannel.sendMessage(embed).queue(m -> {
            m.addReaction(yesEmote).queue();
            m.addReaction(noEmote).queue();

            awaitingRequests.put(m.getIdLong(), request);
        });
    }

    @Override
    public void onEvent(@NotNull GenericEvent event) {
        if(event instanceof MessageReactionAddEvent) {
            MessageReactionAddEvent reactionEvent = (MessageReactionAddEvent) event;

            RepRequest request = awaitingRequests.get(reactionEvent.getMessageIdLong());
            if(!reactionEvent.getMember().getUser().isBot() && request != null) {
                if(reactionEvent.getReactionEmote().getIdLong() == YES_EMOTE_ID) {
                    plugin.getReputationManager().addReputation(request.getTarget(), 1);

                    awaitingRequests.remove(reactionEvent.getMessageIdLong());
                    reactionEvent.getChannel().deleteMessageById(reactionEvent.getMessageId()).queue();
                } else if(reactionEvent.getReactionEmote().getIdLong() == NO_EMOTE_ID) {
                    awaitingRequests.remove(reactionEvent.getMessageIdLong());
                    reactionEvent.getChannel().deleteMessageById(reactionEvent.getMessageId()).queue();
                }
            }
        }
    }
}
