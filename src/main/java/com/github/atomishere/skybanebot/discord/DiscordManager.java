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
import com.github.atomishere.skybanebot.discord.commands.GetInactiveMembersCommand;
import com.github.atomishere.skybanebot.discord.commands.GetReputationCommand;
import com.github.atomishere.skybanebot.discord.commands.RegisterInactivityCommand;
import com.github.atomishere.skybanebot.discord.commands.ReputationCommand;
import com.github.atomishere.skybanebot.service.AbstractService;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.command.CommandClientBuilder;
import com.neovisionaries.ws.client.DualStackMode;
import com.neovisionaries.ws.client.WebSocketFactory;
import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.events.ShutdownEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;

import javax.security.auth.login.LoginException;
import java.util.Collections;
import java.util.EnumSet;
import java.util.concurrent.*;
import java.util.logging.Logger;

public class DiscordManager extends AbstractService {
    private static final Logger logger =  Logger.getLogger(DiscordManager.class.getName());

    private static final String OWNER_ID = "332423993132974081";
    private static final EnumSet<GatewayIntent> intents = EnumSet.of(
            GatewayIntent.GUILD_MEMBERS,
            GatewayIntent.GUILD_EMOJIS,
            GatewayIntent.GUILD_MESSAGES,
            GatewayIntent.GUILD_MESSAGE_REACTIONS
    );

    @Getter
    private JDA jda;
    private CommandClient commandClient;

    @ConfigurationValue
    private String botToken = "bot-token-here";
    @ConfigurationValue
    private String commandPrefix = "+";

    @ConfigurationValue
    private int requiredXp = 25000;

    private ExecutorService callbackThreadPool;

    public DiscordManager(SkybaneBot plugin) {
        super(plugin);
    }

    @Override
    public void onStart() {
        callbackThreadPool = new ForkJoinPool(Runtime.getRuntime().availableProcessors(), pool -> {
            final ForkJoinWorkerThread worker = ForkJoinPool.defaultForkJoinWorkerThreadFactory.newThread(pool);
            worker.setName("SkybaneBot - JDA Callback " + worker.getPoolIndex());
            return worker;
        }, null, true);

        final ThreadFactory gatewayThreadFactory = new ThreadFactoryBuilder().setNameFormat("SkybaneBot - JDA Gateway").build();
        final ScheduledExecutorService gatewayThreadPool = Executors.newSingleThreadScheduledExecutor(gatewayThreadFactory);

        final ThreadFactory rateLimitThreadFactory = new ThreadFactoryBuilder().setNameFormat("DiscordSRV - JDA Rate Limit").build();
        final ScheduledExecutorService rateLimitThreadPool = new ScheduledThreadPoolExecutor(5, rateLimitThreadFactory);

        try {
            jda = JDABuilder.create(Sets.immutableEnumSet(intents))
                    .setCallbackPool(callbackThreadPool, false)
                    .setGatewayPool(gatewayThreadPool, true)
                    .setRateLimitPool(rateLimitThreadPool, true)
                    .setWebsocketFactory(new WebSocketFactory().setDualStackMode(DualStackMode.IPV4_ONLY))
                    .setAutoReconnect(true)
                    .setBulkDeleteSplittingEnabled(false)
                    .setToken(botToken)
                    .addEventListeners(new CommandClientBuilder()
                            .setPrefix(commandPrefix)
                            .setOwnerId(OWNER_ID)
                            .addCommands(new RegisterInactivityCommand(plugin), new GetInactiveMembersCommand(requiredXp, plugin), new ReputationCommand(plugin), new GetReputationCommand(plugin))
                            .build())
                    .setContextEnabled(false)
                    .build().awaitReady();
        } catch (LoginException | InterruptedException ignored) {
            // already logged by JDA
        }
    }

    @Override
    public void onStop() {
        final ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat("SkybaneBot - Shutdown").build();
        final ExecutorService executor = Executors.newSingleThreadExecutor(threadFactory);
        try {
            executor.invokeAll(Collections.singletonList(() -> {
                jda.getEventManager().getRegisteredListeners().forEach(l -> jda.getEventManager().unregister(l));

                CompletableFuture<Void> shutdownTask = new CompletableFuture<>();
                jda.addEventListener(new ListenerAdapter() {
                    @Override
                    public void onShutdown(ShutdownEvent event) {
                        shutdownTask.complete(null);
                    }
                });
                jda.shutdownNow();
                jda = null;
                try {
                    shutdownTask.get(5, TimeUnit.SECONDS);
                } catch(TimeoutException e) {
                    logger.warning("JDA took too long to shut down!");
                }

                callbackThreadPool.shutdownNow();
                return null;
            }));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        executor.shutdownNow();
    }
}
