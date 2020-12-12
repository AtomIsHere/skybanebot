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

package com.github.atomishere.skybanebot.config;

import lombok.RequiredArgsConstructor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class ConfigHandler {
    private static final Logger log = Logger.getLogger(ConfigHandler.class.getName());

    private final Map<String, ConfigContainer> loadedConfigs = new HashMap<>();

    private final File configFolder;

    public void init() {
        if(!configFolder.exists()) {
            configFolder.mkdir();
        }
    }

    public void saveConfigs() {
        for(ConfigContainer config : loadedConfigs.values()) {
            if(!config.getConfigFile().exists()) {
                try {
                    if(!config.getConfigFile().createNewFile()) {
                        throw new IOException("Could not create file: " + config.getConfigFile().getName());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    continue;
                }
            }

            try {
                config.getConfig().save(config.getConfigFile());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void injectConfigValues(Object object, String configName) throws IOException, InvalidConfigurationException {
        Optional<Field> configFieldOpt = Arrays.stream(object.getClass().getDeclaredFields()).filter(f -> f.isAnnotationPresent(ConfigField.class)).findAny();
        Collection<Field> fields = findAllFieldsWithAnnotation(object.getClass(), ConfigurationValue.class);

        if(!configFieldOpt.isPresent() && fields.size() == 0) {
            return;
        }

        ConfigContainer config = getOrCreateConfig(configName);

        for(Field configField : fields) {
            configField.setAccessible(true);

            ConfigurationValue configValue = configField.getAnnotation(ConfigurationValue.class);

            String name = configValue.value();
            if(name.equals("")) {
                name = configField.getName();
            }

            Class<?> fieldType = configField.getType();
            Object value = config.getConfig().getValues(true).get(name);

            if(value == null) {
                log.warning("Could not find config value " + name);
            } else if(fieldType.isInstance(value)) {
                try {
                    configField.set(object, fieldType.cast(value));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            } else {
                log.warning("Found field: " + name + ", in config is the incorrect type. Looking for: " + fieldType.getName() + ", found: " + value.getClass().getName() + "!");
            }

            configField.setAccessible(false);
        }

        if(configFieldOpt.isPresent()) {
            Field configField = configFieldOpt.get();

            configField.setAccessible(true);

            try {
                configField.set(object, config);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }

            configField.setAccessible(false);
        }
    }

    public void saveConfigValues(Object toSave, String configName) throws IOException, InvalidConfigurationException {
        Collection<Field> fields = findAllFieldsWithAnnotation(toSave.getClass(), ConfigurationValue.class);
        if(fields.size() == 0) {
            return;
        }

        ConfigContainer config = getOrCreateConfig(configName);

        for(Field configField : findAllFieldsWithAnnotation(toSave.getClass(), ConfigurationValue.class)) {
            configField.setAccessible(true);
            ConfigurationValue configValue = configField.getAnnotation(ConfigurationValue.class);

            String name = configValue.value();
            if(name.equals("")) {
                name = configField.getName();
            }

            try {
                config.getConfig().set(name, configField.get(toSave));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    private Collection<Field> findAllFieldsWithAnnotation(Class<?> target, Class<? extends Annotation> annotation) {
        return Arrays.stream(target.getDeclaredFields())
                .filter(f -> f.isAnnotationPresent(annotation))
                .collect(toImmutableList());
    }

    private static <T> Collector<T, List<T>, List<T>> toImmutableList() {
        return Collector.of(ArrayList::new, List::add,
                (left, right) -> {
                    left.addAll(right);
                    return left;
                }, Collections::unmodifiableList);
    }

    private ConfigContainer getOrCreateConfig(String configName) throws IOException, InvalidConfigurationException {
        if(loadedConfigs.containsKey(configName)) {
            return loadedConfigs.get(configName);
        }

        File configFile = new File(configFolder, configName + ".yml");

        YamlConfiguration yamlConfig = new YamlConfiguration();
        if(configFile.exists()) {
            yamlConfig.load(configFile);
        }

        ConfigContainer config = new ConfigContainer(configFile, yamlConfig);

        loadedConfigs.put(configName, config);

        return config;
    }
}
