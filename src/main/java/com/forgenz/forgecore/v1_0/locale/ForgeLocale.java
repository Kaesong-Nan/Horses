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
import java.io.InputStreamReader;
import java.util.logging.Level;

@SuppressWarnings("unused")
public final class ForgeLocale implements ForgeCore {
    @SuppressWarnings("WeakerAccess")
    public static final String LOCALE_CONFIG_LOCATION = String.format("locale%s%s.yml", File.separatorChar, "%s");
    private final ForgePlugin plugin;
    private ForgeMessage[] messages;
    
    public ForgeLocale(final ForgePlugin plugin) {
        this.plugin = plugin;
    }
    
    public final void registerEnumMessages(final Class<? extends Enum<?>> clazz) {
        final Enum[] constants = clazz.getEnumConstants();
        
        if(!(constants[0] instanceof ForgeLocaleEnum)) {
            throw new IllegalArgumentException("Enum must implement ForgeLocaleEnum");
        }
        
        final ForgeLocaleEnum[] locale = (ForgeLocaleEnum[]) constants;
    
        messages = new ForgeMessage[locale.length];
        
        for(int i = 0; i < messages.length; i++) {
            messages[i] = locale[i].getMessage();
        }
    }
    
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public final void updateMessages() {
        final String localeStr = BukkitConfigUtil.getAndSet(plugin.getConfig(), "locale", String.class, "en");
        
        final File locale = new File(plugin.getDataFolder(), String.format(LOCALE_CONFIG_LOCATION, localeStr));
        
        final FileConfiguration cfg = new YamlConfiguration();
        try {
            if(!locale.exists()) {
                try {
                    final InputStream fin = plugin.getResource(localeStr);
                    
                    if(fin != null) {
                        cfg.load(new InputStreamReader(fin));
                    }
                    
                    cfg.save(locale);
                } catch(final InvalidConfigurationException e) {
                    plugin.log(Level.WARNING, "Locale embeded in plugin is invalid", e);
                    return;
                }
            } else {
                try {
                    cfg.load(locale);
                } catch(final IllegalArgumentException ignored) {
                } catch(final InvalidConfigurationException e) {
                    plugin.log(Level.WARNING, "Your Locale file %s is invalid", e, new Object[] {locale.getName()});
                    locale.delete();
                    updateMessages();
                    return;
                }
                
                final InputStream fin = plugin.getResource(localeStr);
                
                if(fin != null) {
                    final FileConfiguration defaults = new YamlConfiguration();
                    defaults.load(new InputStreamReader(fin));
                    cfg.addDefaults(defaults);
                    cfg.options().copyDefaults(true);
                }
            }
        } catch(final IOException e) {
            e.printStackTrace();
        } catch(final InvalidConfigurationException e) {
            plugin.log(Level.WARNING, "Locale embeded in plugin is invalid", e);
            return;
        }
        
        for(final ForgeMessage m : messages) {
            m.updateMessage(cfg);
        }
        
        try {
            cfg.save(locale);
        } catch(final IOException e) {
            getPlugin().log(Level.WARNING, "Failed to save locale file %s", e, new Object[] {locale.getName()});
        }
    }
    
    public final String getMessage(final Enum<?> instance) {
        return messages[instance.ordinal()].getMessage();
    }
    
    public ForgePlugin getPlugin() {
        return plugin;
    }
}