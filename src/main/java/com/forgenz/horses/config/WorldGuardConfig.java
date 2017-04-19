package com.forgenz.horses.config;

import com.forgenz.forgecore.v1_0.ForgeCore;
import com.forgenz.forgecore.v1_0.util.BukkitConfigUtil;
import com.forgenz.horses.Horses;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class WorldGuardConfig
        implements ForgeCore {
    private final Horses plugin;
    public final Set<String> commandSummonAllowedRegions;
    public final Set<String> commandDismissAllowedRegions;
    public final Set<String> commandBuyAllowedRegions;

    public WorldGuardConfig(Horses plugin) {
        this.plugin = plugin;

        ConfigurationSection cfg = BukkitConfigUtil.getAndSetConfigurationSection(plugin.getConfig(), "WorldGuard");
        cfg = BukkitConfigUtil.getAndSetConfigurationSection(cfg, "CommandAllowedRegions");

        Set set = new HashSet();
        List<String> list;
        if (cfg.isList("Summon")) {
            list = cfg.getStringList("Summon");
            for (String region : list)
                set.add(region);
        } else {
            list = Collections.emptyList();
        }
        this.commandSummonAllowedRegions = (set.isEmpty() ? Collections.emptySet() : Collections.unmodifiableSet(set));
        cfg.set("Summon", list);

        set = new HashSet();
        this.commandBuyAllowedRegions = Collections.unmodifiableSet(set);
        if (cfg.isList("Buy")) {
            list = cfg.getStringList("Buy");
            for (String region : list)
                set.add(region);
        } else {
            list = Collections.emptyList();
        }
        cfg.set("Buy", list);

        set = new HashSet();
        if (cfg.isList("Dismiss")) {
            list = cfg.getStringList("Dismiss");
            for (String region : list)
                set.add(region);
        } else {
            list = Collections.emptyList();
        }
        this.commandDismissAllowedRegions = (set.isEmpty() ? Collections.emptySet() : Collections.unmodifiableSet(set));
        cfg.set("Dismiss", list);
    }

    public Horses getPlugin() {
        return this.plugin;
    }

    public boolean allowCommand(Set<String> allowedRegions, Location location) {
        if (getPlugin().getWorldGuard() == null) {
            return true;
        }
        if (allowedRegions.isEmpty()) {
            return false;
        }
        RegionManager rm = getPlugin().getWorldGuard().getRegionManager(location.getWorld());

        if (rm == null) {
            return false;
        }
        for (ProtectedRegion region : rm.getApplicableRegions(location)) {
            if (allowedRegions.contains(region.getId())) {
                return true;
            }
        }

        return false;
    }
}