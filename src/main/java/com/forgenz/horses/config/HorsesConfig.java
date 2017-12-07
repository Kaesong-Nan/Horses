package com.forgenz.horses.config;

import com.forgenz.horses.HorseType;
import com.forgenz.horses.Horses;
import com.forgenz.horses.database.HorseDatabaseStorageType;
import com.voxmc.voxlib.EssentialsItem;
import com.voxmc.voxlib.util.ConfigUtil;
import com.voxmc.voxlib.util.VoxEffects;
import com.voxmc.voxlib.util.VoxTrail;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.logging.Level;
import java.util.regex.Pattern;

@SuppressWarnings({"unused", "PublicField"})
public class HorsesConfig extends AbstractConfig {
    public final WorldGuardConfig worldGuardCfg;
    public final HorseDatabaseStorageType databaseType;
    public final HorseDatabaseStorageType importDatabaseType;
    public final boolean showAuthor;
    public final boolean forceEnglishCharacters;
    public final boolean fixZeroJumpStrength;
    public final Pattern rejectedHorseNamePattern;
    private final HorsesWorldConfig globalCfg;
    private final Map<String, HorsesWorldConfig> worldConfigs;
    private final Map<String, VoxTrail> trails = new HashMap<>();
    private final Map<String, EssentialsItem> bardings = new HashMap<>();
    private final Map<String, EssentialsItem> saddles = new HashMap<>();
    private VoxEffects spawnEffects;
    private VoxEffects dismisEffects;
    private final double speedPerLevel;
    private final double jumpPerLevel;
    private final String horseEggSyntax;
    private final String horseIdSyntax;
    private List<String> blackListNames = new ArrayList<>();
    private final EssentialsItem packCost;
    private final String packupItemNeededMessage;
    private double cheatMaxJump = 10000000;
    private double cheatMaxSpeed = 1000000;
    private double cheatMaxHealth = 1000000;
    
    public HorsesConfig(final Horses plugin) {
        super(plugin, (YamlConfiguration) plugin.getConfig(), null, null, null, false);
        
        final YamlConfiguration cfg = loadConfiguration();
        
        initializeHeader();
        addResourseToHeader("header_main.txt");
        
        final Map worldConfigs = new HashMap();
        this.worldConfigs = Collections.unmodifiableMap(worldConfigs);
        
        final List<String> worlds = cfg.getStringList("WorldConfigs");
        for(final String world : worlds) {
            worldConfigs.put(world, new HorsesWorldConfig(plugin, world.toLowerCase()));
        }
        cfg.set("WorldConfigs", null);
        cfg.set("WorldConfigs", worlds);
        horseEggSyntax = ConfigUtil.getColorizedString(cfg, "BaseHorseItem");
        horseIdSyntax = ConfigUtil.getColorizedString(cfg, "HorseIdSyntax");
        packCost = new EssentialsItem(cfg.getString("HorsePackCost"));
        packupItemNeededMessage = ConfigUtil.getColorizedString(cfg, "HorsePackItemNeededMessage");
        
        String dbString = getAndSet("DatabaseType", HorseDatabaseStorageType.YAML.toString(), String.class).toUpperCase();
        HorseDatabaseStorageType databaseType = HorseDatabaseStorageType.getFromString(dbString);
        if(databaseType == null) {
            getPlugin().severe("Invalid database type %s", new Object[] {dbString});
            plugin.severe("#################################");
            plugin.severe("Falling back to a dummy database");
            plugin.severe("WARNING: No data will be saved");
            plugin.severe("#################################");
            databaseType = HorseDatabaseStorageType.DUMMY;
        }
        this.databaseType = databaseType;
        
        dbString = getAndSet("ImportDatabaseType", "NONE", String.class).toUpperCase();
        databaseType = HorseDatabaseStorageType.getFromString(dbString);
        if(databaseType == this.databaseType) {
            databaseType = null;
        }
        set("ImportDatabaseType", "NONE");
        importDatabaseType = databaseType;
        
        if(getAndSet("EnableWorldGuardIntegration", Boolean.FALSE, Boolean.class)) {
            worldGuardCfg = new WorldGuardConfig(plugin);
        } else {
            worldGuardCfg = null;
        }
        showAuthor = getAndSet("ShowAuthorInCommand", Boolean.TRUE, Boolean.class);
        forceEnglishCharacters = getAndSet("ForceEnglishCharacters", Boolean.TRUE, Boolean.class);
        
        final String defPattern = "f.?u.?c.?k|d.?[1i].?(c.?k?|c|k)|c.?u.?n.?t";
        final String pattern = getAndSet("RejectedHorseNamePattern", defPattern, String.class);
        
        Pattern testPattern = null;
        try {
            testPattern = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
        } catch(final IllegalArgumentException e) {
            getPlugin().log(Level.WARNING, "Invalid pattern for name rejection", e);
            testPattern = Pattern.compile(defPattern);
        } finally {
            rejectedHorseNamePattern = testPattern;
        }
        
        fixZeroJumpStrength = getAndSet("FixZeroJumpStrength", Boolean.TRUE, Boolean.class);
        
        globalCfg = new HorsesWorldConfig(plugin, cfg);
        jumpPerLevel = cfg.getDouble("JumpPerLevel", 0);
        speedPerLevel = cfg.getDouble("SpeedPerLevel", 0);
        blackListNames = new ArrayList<>();
        if(cfg.contains("RenameBlacklist")) {
            blackListNames = cfg.getStringList("RenameBlacklist");
        }
        if(cfg.contains("Trails")) {
            final ConfigurationSection trailsSection = cfg.getConfigurationSection("Trails");
            for(final String key : trailsSection.getKeys(false)) {
                final ConfigurationSection trailSection = trailsSection.getConfigurationSection(key);
                final VoxTrail voxTrail = VoxTrail.fromConfig(trailSection);
                trails.put(key.toLowerCase(), voxTrail);
            }
        }
        if(cfg.contains("Bardings")) {
            final ConfigurationSection bardingsSection = cfg.getConfigurationSection("Bardings");
            for(final String key : bardingsSection.getKeys(false)) {
                String itemLine = bardingsSection.getString(key);
                if(itemLine.startsWith("[")) {
                    itemLine = itemLine.substring(1, itemLine.length() - 1);
                }
                final EssentialsItem essentialsItem = new EssentialsItem(itemLine);
                bardings.put(key.toLowerCase(), essentialsItem);
            }
        }
        if(cfg.contains("Saddles")) {
            final ConfigurationSection saddlesSection = cfg.getConfigurationSection("Saddles");
            for(final String key : saddlesSection.getKeys(false)) {
                String itemLine = saddlesSection.getString(key);
                if(itemLine.startsWith("[")) {
                    itemLine = itemLine.substring(1, itemLine.length() - 1);
                }
                final EssentialsItem essentialsItem = new EssentialsItem(itemLine);
                saddles.put(key.toLowerCase(), essentialsItem);
            }
        }
        if(cfg.contains("SpawnEffects")) {
            spawnEffects = VoxEffects.fromConfig(cfg.getConfigurationSection("SpawnEffects"));
        }
        if(cfg.contains("DismissEffects")) {
            dismisEffects = VoxEffects.fromConfig(cfg.getConfigurationSection("DismissEffects"));
        }
        if(cfg.contains("max-attributes")) {
            final ConfigurationSection maxAS = cfg.getConfigurationSection("max-attributes");
            cheatMaxJump = maxAS.getDouble("jump-strength", 100000);
            cheatMaxHealth = maxAS.getDouble("health", 1000000);
            cheatMaxSpeed = maxAS.getDouble("speed", 100000000);
        }
        
        saveConfiguration();
    }
    
    public double getCheatMaxJump() {
        return cheatMaxJump;
    }
    
    public double getCheatMaxSpeed() {
        return cheatMaxSpeed;
    }
    
    public double getCheatMaxHealth() {
        return cheatMaxHealth;
    }
    
    public Map<String, EssentialsItem> getSaddles() {
        return saddles;
    }
    
    public VoxEffects getSpawnEffects() {
        return spawnEffects;
    }
    
    public double getSpeedPerLevel() {
        return speedPerLevel;
    }
    
    public double getJumpPerLevel() {
        return jumpPerLevel;
    }
    
    public VoxEffects getDismisEffects() {
        return dismisEffects;
    }
    
    public Map<String, EssentialsItem> getBardings() {
        return bardings;
    }
    
    public EssentialsItem getPackCost() {
        return packCost;
    }
    
    public Map<String, VoxTrail> getTrails() {
        return trails;
    }
    
    private HorsesWorldConfig getWorldConfig(final World world) {
        if(world == null) {
            return globalCfg;
        }
        
        final HorsesWorldConfig cfg = worldConfigs.get(world.getName().toLowerCase());
        return cfg != null ? cfg : globalCfg;
    }
    
    public HorsesPermissionConfig getPermConfig(final Player player) {
        HorsesWorldConfig cfg = player != null ? getWorldConfig(player.getWorld()) : null;
        
        if(cfg == null) {
            cfg = globalCfg;
        }
        return cfg.getPermConfig(player);
    }
    
    public HorseTypeConfig getHorseTypeConfig(final Player player, final HorseType type) {
        return getPermConfig(player).getHorseTypeConfig(type);
    }
    
    public String getHorseEggSyntax() {
        return horseEggSyntax;
    }
    
    public String getHorseIdSyntax() {
        return horseIdSyntax;
    }
    
    public HorseTypeConfig getHorseTypeConfigLike(final Player player, final String like) {
        return getPermConfig(player).getHorseTypeConfigLike(like);
    }
    
    public String getStableGroup(final World world) {
        return getWorldConfig(world).stableGroup;
    }
    
    public boolean isProtecting() {
        for(final HorsesWorldConfig worldCfg : worldConfigs.values()) {
            if(worldCfg.isProtecting()) {
                return true;
            }
        }
        return globalCfg.isProtecting();
    }
    
    public List<String> getBlackListNames() {
        return blackListNames;
    }
    
    public boolean trackMovements() {
        if(trackMovements(globalCfg)) {
            return true;
        }
        for(final HorsesWorldConfig cfg : worldConfigs.values()) {
            if(trackMovements(cfg)) {
                return true;
            }
        }
        return false;
    }
    
    public String getPackupItemNeededMessage() {
        return packupItemNeededMessage;
    }
    
    private boolean trackMovements(final HorsesPermissionConfig cfg) {
        return cfg.summonDelay > 0 && cfg.cancelSummonOnMove;
    }
    
    private boolean trackMovements(final HorsesWorldConfig cfg) {
        if(trackMovements(cfg.worldCfg)) {
            return true;
        }
        for(final HorsesPermissionConfig permCfg : cfg.permissionConfigs.values()) {
            if(trackMovements(permCfg)) {
                return true;
            }
        }
        return false;
    }
}