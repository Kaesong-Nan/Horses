package com.forgenz.horses.pack;

import com.forgenz.horses.Horses;
import com.forgenz.horses.PlayerHorse;
import com.forgenz.horses.Stable;
import com.forgenz.horses.database.YamlDatabase;
import com.voxmc.voxlib.util.CustomConfigAccessor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.io.IOException;

/**
 * Created by john on 8/12/15.
 */
public class PackDatabase {
    private Horses plugin;

    public PackDatabase(Horses plugin) {
        this.plugin = plugin;
    }
    public File getPackFolder() {
        File packFolder = new File(plugin.getDataFolder(),"packs/");
        if (!packFolder.exists()) {
            packFolder.mkdirs();
        }
        return packFolder;
    }
    public File getHorseFile(String id) {
        return new File(getPackFolder(),id +".dat");
    }
    private int lastId = -1;
    public String getNextId() {
        if (lastId != -1) {
            lastId += 1;
            String id = lastId + "";
            while (getHorseFile(id).exists()) {
                lastId += 1;
                id = lastId + "";
            }
            return id;
        }
        lastId = 0;
        for (String name : getPackFolder().list()) {
            if (name.contains(".dat")) {
                name = name.replace(".dat","");
                try {
                    int id = Integer.parseInt(name);
                    if (id > lastId) {
                        lastId = id;
                    }
                }catch (Exception ignored) {}
            }
        }
        return getNextId();
    }
    public PlayerHorse loadHorse(String id, Stable stable) {
        File file = getHorseFile(id);
        if (!file.exists()) {
            return null;
        }
        CustomConfigAccessor customConfigAccessor = new CustomConfigAccessor(file);
        FileConfiguration fileConfiguration = customConfigAccessor.getConfig();
        ConfigurationSection horseSect = null;
        for (String key : fileConfiguration.getKeys(false)) {
            horseSect = fileConfiguration.getConfigurationSection(key);
            break;
        }
        if (horseSect == null) {
            return null;
        }
        PlayerHorse playerHorse = YamlDatabase.configToHorse(stable, horseSect, plugin);
        if (playerHorse == null) {
            return null;
        }
        plugin.getHorseDatabase().saveHorse(playerHorse);
        return playerHorse;
    }

    public String savePlayerHorse(PlayerHorse playerHorse) {
        String id = getNextId();
        File file = getHorseFile(id);
        try {
            file.createNewFile();
        } catch (IOException e) {
            return null;
        }
        CustomConfigAccessor customConfigAccessor = new CustomConfigAccessor(file);
        YamlDatabase.saveHorseToSection(customConfigAccessor.getConfig(),playerHorse);
        customConfigAccessor.saveConfig();
        playerHorse.deleteHorse();
        return id;
    }
}
