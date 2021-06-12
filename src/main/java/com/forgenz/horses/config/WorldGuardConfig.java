package com.forgenz.horses.config;

import com.forgenz.forgecore.v1_0.ForgeCore;
import com.forgenz.forgecore.v1_0.util.BukkitConfigUtil;
import com.forgenz.horses.Horses;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;

import java.util.*;

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

        Set<String> set = new HashSet<>();
        List<String> list;
        if (cfg.isList("Summon")) {
            list = cfg.getStringList("Summon");
            set.addAll(list);
        } else {
            list = Collections.emptyList();
        }
        this.commandSummonAllowedRegions = (set.isEmpty() ? Collections.emptySet() : Collections.unmodifiableSet(set));
        cfg.set("Summon", list);

        set.clear();
        this.commandBuyAllowedRegions = Collections.unmodifiableSet(set);
        if (cfg.isList("Buy")) {
            list = cfg.getStringList("Buy");
            set.addAll(list);
        } else {
            list = Collections.emptyList();
        }
        cfg.set("Buy", list);

        set.clear();
        if (cfg.isList("Dismiss")) {
            list = cfg.getStringList("Dismiss");
            set.addAll(list);
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
        RegionManager rm = WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(Objects.requireNonNull(location.getWorld())));

        if (rm == null) {
            return false;
        }
        for (ProtectedRegion region : rm.getApplicableRegions(BlockVector3.at(location.getX(), location.getY(), location.getZ()))) {
            if (allowedRegions.contains(region.getId())) {
                return true;
            }
        }

        return false;
    }
}