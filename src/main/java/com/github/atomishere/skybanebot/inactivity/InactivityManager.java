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

package com.github.atomishere.skybanebot.inactivity;

import com.github.atomishere.skybanebot.SkybaneBot;
import com.github.atomishere.skybanebot.config.ConfigContainer;
import com.github.atomishere.skybanebot.config.ConfigField;
import com.github.atomishere.skybanebot.config.ConfigurationValue;
import com.github.atomishere.skybanebot.service.AbstractService;
import org.bukkit.configuration.ConfigurationSection;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Logger;

public class InactivityManager extends AbstractService {
    private static final Logger logger = Logger.getLogger(InactivityManager.class.getName());

    private final Set<Inactive> inactivePeople = new HashSet<>();

    private SimpleDateFormat sdf;
    @ConfigurationValue
    private String dateFormat = "yyyy-MM-dd";

    @ConfigField
    private ConfigContainer config;

    public InactivityManager(SkybaneBot plugin) {
        super(plugin);
    }

    @Override
    public void onStart() {
        sdf = new SimpleDateFormat(dateFormat);
        config.getConfig()
                .getKeys(false)
                .stream()
                .filter(k -> config.getConfig().isConfigurationSection(k))
                .map(k -> config.getConfig().getConfigurationSection(k))
                .map(this::createInactiveFromConfig)
                .filter(Objects::nonNull)
                .forEach(inactivePeople::add);
    }

    @Override
    public void onStop() {
        config.getConfig()
                .getKeys(false)
                .forEach(k -> config.getConfig().set(k, null));

        for(Inactive inactive : inactivePeople) {
            ConfigurationSection section = config.getConfig().createSection(inactive.getUsername());
            section.set("username", inactive.getUsername());
            section.set("endData", sdf.format(inactive.getEndDate()));
        }
    }

    public boolean isInactive(String username) {
        Inactive inactive = inactivePeople.stream().filter(i -> i.getUsername().equals(username)).findFirst().orElse(null);
        if(inactive != null) {
            Date current = new Date(System.currentTimeMillis());
            if(inactive.getEndDate().after(current)) {
                inactivePeople.remove(inactive);
                return false;
            }
            return true;
        }
        return false;
    }

    private Inactive createInactiveFromConfig(ConfigurationSection config) {
        try {
            return new Inactive(config.getString("username"), sdf.parse(config.getString("endDate")));
        } catch (ParseException e) {
            logger.severe("Invalid date format in config: " + config.getName());
            e.printStackTrace();
            return null;
        }
    }
}
