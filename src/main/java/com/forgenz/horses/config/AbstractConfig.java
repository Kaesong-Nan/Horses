package com.forgenz.horses.config;

import com.forgenz.forgecore.v1_0.ForgeCore;
import com.forgenz.horses.Horses;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.file.YamlConfigurationOptions;
import org.bukkit.plugin.PluginDescriptionFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;

public abstract class AbstractConfig
        implements ForgeCore {
    private final Horses plugin;
    private final AbstractConfig parentCfg;
    private final String folder;
    private final String fileName;
    private YamlConfiguration cfg;

    protected AbstractConfig(Horses plugin, YamlConfiguration cfg) {
        this.plugin = plugin;
        this.cfg = cfg;

        this.parentCfg = null;
        this.folder = (this.fileName = null);
    }

    protected AbstractConfig(Horses plugin, AbstractConfig parentCfg, String folder, String fileName) {
        this.plugin = plugin;
        this.parentCfg = parentCfg;
        this.folder = folder;
        this.fileName = (fileName + ".yml");
    }

    protected AbstractConfig(Horses plugin, YamlConfiguration cfg, AbstractConfig parent, String folder, String fileName, boolean standalone) {
        this.plugin = plugin;

        if (standalone) {
            this.parentCfg = parent;
            this.folder = folder;
            this.fileName = (fileName + ".yml");
        } else {
            this.cfg = cfg;
            this.parentCfg = null;
            this.folder = (this.fileName = null);
        }
    }

    private String getFolder() {
        String folder = this.parentCfg != null ? this.parentCfg.getFolder() : "";

        if (this.folder != null) {
            folder = folder + File.separatorChar + this.folder;
        }

        return folder;
    }

    private String getPath() {
        return String.format("%s%s%s", new Object[]{getFolder(), File.separator, this.fileName});
    }

    protected void addResourseToHeader(String resourse) {
        if (this.cfg == null) {
            return;
        }
        addStringToHeader(getPlugin().getResourseString(resourse));
    }

    protected void addStringToHeader(String header) {
        if (this.cfg == null) {
            return;
        }
        YamlConfigurationOptions options = this.cfg.options();

        if (options.header() != null) {
            header = options.header() + header;
        }
        options.header(header);
        options.copyHeader(true);
    }

    protected void initializeHeader() {
        this.cfg.options().header(null);

        PluginDescriptionFile pdf = getPlugin().getDescription();
        addStringToHeader(String.format("%s v%s by %s\n%s\n\n", new Object[]{getPlugin().getName(), pdf.getVersion(), getPlugin().getAuthors(), pdf.getWebsite()}));
    }

    protected YamlConfiguration loadConfiguration() {
        if (this.cfg == null) {
            this.cfg = new YamlConfiguration();

            String path = getPath();

            File cfgFile = new File(getPlugin().getDataFolder(), path);

            if (cfgFile.exists()) {
                try {
                    this.cfg.load(cfgFile);
                } catch (FileNotFoundException e) {
                } catch (IOException e) {
                } catch (InvalidConfigurationException e) {
                    getPlugin().log(Level.SEVERE, "Failed to load configuration %1$s. Saving as %1$s.broken", e, new Object[]{path});
                    cfgFile.renameTo(new File(getPlugin().getDataFolder(), path + ".broken"));
                }
            }

            initializeHeader();
        }

        return this.cfg;
    }

    protected void saveConfiguration() {
        if (this.cfg == null) {
            return;
        }
        if (this.fileName == null) {
            this.cfg = null;
            return;
        }

        String path = getPath();

        File cfgFile = new File(getPlugin().getDataFolder(), path);
        try {
            this.cfg.save(cfgFile);
        } catch (IOException e) {
            getPlugin().log(Level.WARNING, "Error when attempting to save the config file %s to disk", e, new Object[]{path});
        }

        this.cfg = null;
    }

    protected <T> T getAndSet(String path, T def, Class<T> clazz) {
        return getAndSet(this.cfg, path, def, clazz);
    }

    protected <T> T getAndSet(ConfigurationSection cfg, String path, T def, Class<T> clazz) {
        T obj = (T) cfg.get(path);

        if ((obj == null) || (!clazz.isAssignableFrom(obj.getClass()))) {
            obj = def;
        }

        set(cfg, path, obj);

        return obj;
    }

    protected ConfigurationSection getConfigSect(String path) {
        return getConfigSect(this.cfg, path);
    }

    protected ConfigurationSection getConfigSect(ConfigurationSection cfg, String path) {
        ConfigurationSection sect = cfg.getConfigurationSection(path);

        if (sect == null) {
            sect = cfg.createSection(path);
        }
        set(cfg, path, sect);

        return sect;
    }

    protected void set(String path, Object value) {
        set(this.cfg, path, value);
    }

    protected void set(ConfigurationSection sect, String path, Object value) {
        sect.set(path, null);
        sect.set(path, value);
    }

    public Horses getPlugin() {
        return this.plugin;
    }
}