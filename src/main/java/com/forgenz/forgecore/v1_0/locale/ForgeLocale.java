package com.forgenz.forgecore.v1_0.locale;

import com.forgenz.forgecore.v1_0.ForgeCore;
import com.forgenz.forgecore.v1_0.bukkit.ForgePlugin;
import com.forgenz.forgecore.v1_0.util.BukkitConfigUtil;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;

public final class ForgeLocale
        implements ForgeCore {
    public static final String LOCALE_CONFIG_LOCATION = String.format("locale%s%s.yml", new Object[]{Character.valueOf(File.separatorChar), "%s"});
    private ForgeMessage[] messages;
    private final ForgePlugin plugin;

    public ForgeLocale(ForgePlugin plugin) {
        this.plugin = plugin;
    }

    public final void registerEnumMessages(Class<? extends Enum<?>> clazz) {
        Enum[] constants = (Enum[]) clazz.getEnumConstants();

        if (!(constants[0] instanceof ForgeLocaleEnum)) {
            throw new IllegalArgumentException("Enum must implement ForgeLocaleEnum");
        }

        ForgeLocaleEnum[] locale = (ForgeLocaleEnum[]) constants;

        this.messages = new ForgeMessage[locale.length];

        for (int i = 0; i < this.messages.length; i++) {
            this.messages[i] = locale[i].getMessage();
        }
    }

    public final void updateMessages() {
        String localeStr = (String) BukkitConfigUtil.getAndSet(this.plugin.getConfig(), "locale", String.class, "en");

        File locale = new File(this.plugin.getDataFolder(), String.format(LOCALE_CONFIG_LOCATION, new Object[]{localeStr}));

        FileConfiguration cfg = new YamlConfiguration();
        try {
            if (!locale.exists()) {
                try {
                    InputStream fin = this.plugin.getResource(localeStr);

                    if (fin != null) {
                        cfg.load(fin);
                    }

                    cfg.save(locale);
                } catch (InvalidConfigurationException e) {
                    this.plugin.log(Level.WARNING, "Locale embeded in plugin is invalid", e);
                    return;
                }
            } else {
                try {
                    cfg.load(locale);
                } catch (IllegalArgumentException e) {
                } catch (InvalidConfigurationException e) {
                    this.plugin.log(Level.WARNING, "Your Locale file %s is invalid", e, new Object[]{locale.getName()});
                    locale.delete();
                    updateMessages();
                    return;
                }

                InputStream fin = this.plugin.getResource(localeStr);

                if (fin != null) {
                    FileConfiguration defaults = new YamlConfiguration();
                    defaults.load(fin);
                    cfg.addDefaults(defaults);
                    cfg.options().copyDefaults(true);
                }

            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidConfigurationException e) {
            this.plugin.log(Level.WARNING, "Locale embeded in plugin is invalid", e);
            return;
        }

        for (ForgeMessage m : this.messages) {
            m.updateMessage(cfg);
        }

        try {
            cfg.save(locale);
        } catch (IOException e) {
            getPlugin().log(Level.WARNING, "Failed to save locale file %s", e, new Object[]{locale.getName()});
        }
    }

    public final String getMessage(Enum<?> instance) {
        return this.messages[instance.ordinal()].getMessage();
    }

    public ForgePlugin getPlugin() {
        return this.plugin;
    }
}