package com.forgenz.forgecore.v1_0.util;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class BukkitConfigUtil {
    public static YamlConfiguration getConfig(File file) {
        YamlConfiguration cfg = new YamlConfiguration();
        try {
            cfg.load(file);
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
        } catch (InvalidConfigurationException e) {
            file.renameTo(new File(file.getPath() + ".broken"));
        }

        return cfg;
    }

    public static <T> T getAndSet(ConfigurationSection cfg, String path, Class<T> clazz, T def) {
        T obj = (T) cfg.get(path, def);

        if ((obj == null) || (!clazz.isAssignableFrom(obj.getClass()))) {
            obj = def;
        }

        cfg.set(path, null);
        cfg.set(path, obj);

        return obj;
    }

    public static ConfigurationSection getAndSetConfigurationSection(ConfigurationSection cfg, String path) {
        ConfigurationSection sect = cfg.getConfigurationSection(path);

        if (sect == null) {
            sect = cfg.createSection(path);
        }

        cfg.set(path, null);
        cfg.set(path, sect);

        return sect;
    }

    public static void set(ConfigurationSection cfg, String path, Object value) {
        cfg.set(path, null);
        cfg.set(path, value);
    }
}