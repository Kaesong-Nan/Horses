package com.forgenz.horses.config;

import com.forgenz.horses.HorseType;
import com.forgenz.horses.Horses;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class HorsesWorldConfig extends AbstractConfig {
    private static final String WORLDS_FOLDER = "worlds" + File.separator;
    protected final HorsesPermissionConfig worldCfg;
    protected final Map<String, HorsesPermissionConfig> permissionConfigs;
    public final String stableGroup;

    public HorsesWorldConfig(Horses plugin, YamlConfiguration cfg) {
        this(plugin, cfg, null, false);
    }

    public HorsesWorldConfig(Horses plugin, String world) {
        this(plugin, null, world, true);
    }

    private HorsesWorldConfig(Horses plugin, YamlConfiguration cfg, String world, boolean standalone) {
        super(plugin, cfg, null, WORLDS_FOLDER + world, "config", standalone);

        cfg = loadConfiguration();

        addResourseToHeader("header_world.txt");

        LinkedHashMap permissionConfigs = new LinkedHashMap();
        this.permissionConfigs = Collections.unmodifiableMap(permissionConfigs);

        List permissions = cfg.getStringList("PermissionConfigs");
        for (String permission : cfg.getStringList("PermissionConfigs")) {
            permissionConfigs.put(permission, new HorsesPermissionConfig(plugin, this, permission));
        }
        set(cfg, "PermissionConfigs", permissions);

        if (standalone)
            this.stableGroup = ((String) getAndSet("StableGroup", "default", String.class)).toLowerCase();
        else {
            this.stableGroup = "default";
        }
        this.worldCfg = new HorsesPermissionConfig(plugin, cfg);

        saveConfiguration();
    }

    protected HorsesPermissionConfig getPermConfig(Player player) {
        if (player != null) {
            for (Map.Entry e : this.permissionConfigs.entrySet()) {
                if (player.hasPermission((String) e.getKey())) {
                    return (HorsesPermissionConfig) e.getValue();
                }
            }
        }

        return this.worldCfg;
    }

    public HorseTypeConfig getHorseTypeConfig(Player player, HorseType type) {
        return getPermConfig(player).getHorseTypeConfig(type);
    }

    public HorseTypeConfig getHorseTypeConfigLike(Player player, String like) {
        return getPermConfig(player).getHorseTypeConfigLike(like);
    }

    public HorseTypeConfig getHorseTypeConfig(Player player, String typeStr) {
        return getPermConfig(player).getHorseTypeConfig(typeStr);
    }

    public boolean isProtecting() {
        for (HorsesPermissionConfig permCfg : this.permissionConfigs.values()) {
            if (permCfg.isProtecting()) {
                return true;
            }
        }
        return this.worldCfg.isProtecting();
    }
}