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
import com.github.atomishere.skybanebot.config.ConfigContainer;
import com.github.atomishere.skybanebot.config.ConfigField;
import com.github.atomishere.skybanebot.service.AbstractService;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class ReputationManager extends AbstractService {
    private static final Logger log = Logger.getLogger(ReputationManager.class.getName());

    private final Map<UUID, Integer> reputation = new HashMap<>();

    @ConfigField
    private ConfigContainer config;

    public ReputationManager(SkybaneBot plugin) {
        super(plugin);
    }

    @Override
    public void onStart() {
        for(String key : config.getConfig().getKeys(false)) {
            if(config.getConfig().isConfigurationSection(key)) {
                ConfigurationSection reputationSection = config.getConfig().getConfigurationSection(key);

                if(verifySection(reputationSection)) {
                    reputation.put(UUID.fromString(reputationSection.getString("uuid")), reputationSection.getInt("reputation"));
                } else {
                    log.info("Invalid reputation data: " + key);
                }
            }
        }
    }

    @Override
    public void onStop() {
        config.getConfig()
                .getKeys(false)
                .forEach(k -> config.getConfig().set(k, null));

        for(Map.Entry<UUID, Integer> entry : reputation.entrySet()) {
            ConfigurationSection reputationSection = config.getConfig().createSection(entry.getKey().toString());

            reputationSection.set("uuid", entry.getKey().toString());
            reputationSection.set("reputation", entry.getValue());
        }

        reputation.clear();
    }

    public Integer getReputation(UUID player) {
        return Optional.ofNullable(reputation.get(player)).orElse(0);
    }

    public void setReputation(UUID player, Integer rep) {
        reputation.put(player, rep);
    }

    public void addReputation(UUID player, Integer rep) {
        if(reputation.containsKey(player)) {
            rep += reputation.get(player);
        }

        setReputation(player, rep);
    }

    public void subtractReputation(UUID player, Integer rep) {
        if(reputation.containsKey(player)) {
            rep = Math.max(0, reputation.get(player) - rep);
        }

        setReputation(player, rep);
    }

    private boolean verifySection(ConfigurationSection section) {
        String uuid = section.getString("uuid");
        if(uuid == null) {
            return false;
        }

        if(!section.isInt("reputation")) {
            return false;
        }

        Pattern uuidPattern = Pattern.compile("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$");
        return uuidPattern.matcher(uuid).matches();
    }
}
