package com.forgenz.horses.config;

import com.forgenz.forgecore.v1_0.ForgeCore;
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

public class HorsesConfig extends AbstractConfig
        implements ForgeCore {
    public final WorldGuardConfig worldGuardCfg;
    private final HorsesWorldConfig globalCfg;
    private final Map<String, HorsesWorldConfig> worldConfigs;
    public final HorseDatabaseStorageType databaseType;
    public final HorseDatabaseStorageType importDatabaseType;
    public final boolean showAuthor;
    public final boolean forceEnglishCharacters;
    public final boolean fixZeroJumpStrength;
    public final Pattern rejectedHorseNamePattern;
    private final Map<String, VoxTrail> trails = new HashMap<String, VoxTrail>();
    private final Map<String, EssentialsItem> bardings = new HashMap<String, EssentialsItem>();
    private final Map<String, EssentialsItem> saddles = new HashMap<>();
    private VoxEffects spawnEffects,dismisEffects;
    private double speedPerLevel,jumpPerLevel;
    private String horseEggSyntax,horseIdSyntax;
    private List<String> blackListNames = new ArrayList<>();
    private EssentialsItem packCost;
    private String packupItemNeededMessage;
    private double cheatMaxJump = 10000000;
    private double cheatMaxSpeed = 1000000;
    private double cheatMaxHealth = 1000000;

    public HorsesConfig(Horses plugin) {
        super(plugin, (YamlConfiguration) plugin.getConfig(), null, null, null, false);

        YamlConfiguration cfg = loadConfiguration();

        initializeHeader();
        addResourseToHeader("header_main.txt");

        Map worldConfigs = new HashMap();
        this.worldConfigs = Collections.unmodifiableMap(worldConfigs);

        List<String> worlds = cfg.getStringList("WorldConfigs");
        for (String world : worlds) {
            worldConfigs.put(world, new HorsesWorldConfig(plugin, world.toLowerCase()));
        }
        cfg.set("WorldConfigs", null);
        cfg.set("WorldConfigs", worlds);
        horseEggSyntax = ConfigUtil.getColorizedString(cfg, "BaseHorseItem");
        horseIdSyntax = ConfigUtil.getColorizedString(cfg,"HorseIdSyntax");
        packCost = new EssentialsItem(cfg.getString("HorsePackCost"));
        packupItemNeededMessage = ConfigUtil.getColorizedString(cfg,"HorsePackItemNeededMessage");

        String dbString = ((String) getAndSet("DatabaseType", HorseDatabaseStorageType.YAML.toString(), String.class)).toUpperCase();
        HorseDatabaseStorageType databaseType = HorseDatabaseStorageType.getFromString(dbString);
        if (databaseType == null) {
            getPlugin().severe("Invalid database type %s", new Object[]{dbString});
            plugin.severe("#################################");
            plugin.severe("Falling back to a dummy database");
            plugin.severe("WARNING: No data will be saved");
            plugin.severe("#################################");
            databaseType = HorseDatabaseStorageType.DUMMY;
        }
        this.databaseType = databaseType;

        dbString = ((String) getAndSet("ImportDatabaseType", "NONE", String.class)).toUpperCase();
        databaseType = HorseDatabaseStorageType.getFromString(dbString);
        if (databaseType == this.databaseType) {
            databaseType = null;
        }
        set("ImportDatabaseType", "NONE");
        this.importDatabaseType = databaseType;

        if (((Boolean) getAndSet("EnableWorldGuardIntegration", Boolean.valueOf(false), Boolean.class)).booleanValue())
            this.worldGuardCfg = new WorldGuardConfig(plugin);
        else {
            this.worldGuardCfg = null;
        }
        this.showAuthor = ((Boolean) getAndSet("ShowAuthorInCommand", Boolean.valueOf(true), Boolean.class)).booleanValue();
        this.forceEnglishCharacters = ((Boolean) getAndSet("ForceEnglishCharacters", Boolean.valueOf(true), Boolean.class)).booleanValue();

        String defPattern = "f.?u.?c.?k|d.?[1i].?(c.?k?|c|k)|c.?u.?n.?t";
        String pattern = (String) getAndSet("RejectedHorseNamePattern", defPattern, String.class);

        Pattern testPattern = null;
        try {
            testPattern = Pattern.compile(pattern, 2);
        } catch (IllegalArgumentException e) {
            getPlugin().log(Level.WARNING, "Invalid pattern for name rejection", e);
            testPattern = Pattern.compile(defPattern);
        } finally {
            this.rejectedHorseNamePattern = testPattern;
        }

        this.fixZeroJumpStrength = ((Boolean) getAndSet("FixZeroJumpStrength", Boolean.valueOf(true), Boolean.class)).booleanValue();

        this.globalCfg = new HorsesWorldConfig(plugin, cfg);
        jumpPerLevel = cfg.getDouble("JumpPerLevel",0);
        speedPerLevel = cfg.getDouble("SpeedPerLevel",0);
        blackListNames = new ArrayList<>();
        if (cfg.contains("RenameBlacklist")) {
            blackListNames = cfg.getStringList("RenameBlacklist");
        }
        if (cfg.contains("Trails")) {
            ConfigurationSection trailsSection = cfg.getConfigurationSection("Trails");
            for (String key : trailsSection.getKeys(false)) {
                ConfigurationSection trailSection = trailsSection.getConfigurationSection(key);
                VoxTrail voxTrail = VoxTrail.fromConfig(trailSection);
                trails.put(key.toLowerCase(),voxTrail);
            }
        }
        if (cfg.contains("Bardings")) {
            ConfigurationSection bardingsSection = cfg.getConfigurationSection("Bardings");
            for (String key : bardingsSection.getKeys(false)) {
                String itemLine = bardingsSection.getString(key);
                if (itemLine.startsWith("[")) {
                    itemLine = itemLine.substring(1,itemLine.length()-1);
                }
                EssentialsItem essentialsItem = new EssentialsItem(itemLine);
                bardings.put(key.toLowerCase(),essentialsItem);
            }
        }
        if (cfg.contains("Saddles")) {
            ConfigurationSection saddlesSection = cfg.getConfigurationSection("Saddles");
            for (String key : saddlesSection.getKeys(false)) {
                String itemLine = saddlesSection.getString(key);
                if (itemLine.startsWith("[")) {
                    itemLine = itemLine.substring(1,itemLine.length()-1);
                }
                EssentialsItem essentialsItem = new EssentialsItem(itemLine);
                saddles.put(key.toLowerCase(), essentialsItem);
            }
        }
        if (cfg.contains("SpawnEffects")) {
            spawnEffects = VoxEffects.fromConfig(cfg.getConfigurationSection("SpawnEffects"));
        }
        if (cfg.contains("DismissEffects")) {
            dismisEffects = VoxEffects.fromConfig(cfg.getConfigurationSection("DismissEffects"));
        }
        if (cfg.contains("max-attributes")) {
            ConfigurationSection maxAS = cfg.getConfigurationSection("max-attributes");
            cheatMaxJump = maxAS.getDouble("jump-strength",100000);
            cheatMaxHealth = maxAS.getDouble("health",1000000);
            cheatMaxSpeed = maxAS.getDouble("speed",100000000);
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

    public HorsesWorldConfig getWorldConfig(World world) {
        if (world == null) {
            return this.globalCfg;
        }

        HorsesWorldConfig cfg = (HorsesWorldConfig) this.worldConfigs.get(world.getName().toLowerCase());
        return cfg != null ? cfg : this.globalCfg;
    }

    public HorsesPermissionConfig getPermConfig(Player player) {
        HorsesWorldConfig cfg = player != null ? getWorldConfig(player.getWorld()) : null;

        if (cfg == null) {
            cfg = this.globalCfg;
        }
        return cfg.getPermConfig(player);
    }

    public HorseTypeConfig getHorseTypeConfig(Player player, HorseType type) {
        return getPermConfig(player).getHorseTypeConfig(type);
    }

    public String getHorseEggSyntax() {
        return horseEggSyntax;
    }

    public String getHorseIdSyntax() {
        return horseIdSyntax;
    }

    public HorseTypeConfig getHorseTypeConfigLike(Player player, String like) {
        return getPermConfig(player).getHorseTypeConfigLike(like);
    }

    public String getStableGroup(World world) {
        return getWorldConfig(world).stableGroup;
    }

    public boolean isProtecting() {
        for (HorsesWorldConfig worldCfg : this.worldConfigs.values()) {
            if (worldCfg.isProtecting()) {
                return true;
            }
        }
        return this.globalCfg.isProtecting();
    }

    public List<String> getBlackListNames() {
        return blackListNames;
    }

    public boolean trackMovements() {
        if (trackMovements(this.globalCfg)) {
            return true;
        }
        for (HorsesWorldConfig cfg : this.worldConfigs.values()) {
            if (trackMovements(cfg)) {
                return true;
            }
        }
        return false;
    }

    public String getPackupItemNeededMessage() {
        return packupItemNeededMessage;
    }

    private boolean trackMovements(HorsesPermissionConfig cfg) {
        return (cfg.summonDelay > 0) && (cfg.cancelSummonOnMove);
    }

    private boolean trackMovements(HorsesWorldConfig cfg) {
        if (trackMovements(cfg.worldCfg))
            return true;
        for (HorsesPermissionConfig permCfg : cfg.permissionConfigs.values()) {
            if (trackMovements(permCfg))
                return true;
        }
        return false;
    }
}