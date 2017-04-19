package com.forgenz.horses.config;

import com.forgenz.horses.HorseType;
import com.forgenz.horses.Horses;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.*;

public class HorsesPermissionConfig extends AbstractConfig {
    public final boolean bypassSpawnProtection;
    public final boolean dismissHorseOnTeleport;
    public final boolean startWithSaddle;
    public final int maxHorses;
    public final int summonDelay;
    public final boolean cancelSummonOnMove;
    public final Map<String, HorseTypeConfig> horseTypeConfigs;
    public final boolean allowBuyCommand;
    public final boolean allowDeleteCommand;
    public final boolean allowDismissCommand;
    public final boolean allowHealCommand;
    public final boolean allowListCommand;
    public final boolean allowRenameCommand;
    public final boolean allowSummonCommand;
    public final boolean allowTypesCommand;
    public final boolean blockRenamingOnWildHorses;
    public final boolean allowClaimingWithNameTag;
    public final boolean allowRenameFromNameTag;
    public final boolean requireNameTagForRenaming;
    public final int maxHorseNameLength;
    public final boolean invincibleHorses;
    public final Set<EntityDamageEvent.DamageCause> protectedDamageCauses;
    public final boolean protectFromOwner;
    public final boolean protectFromPlayers;
    public final boolean protectFromMobs;
    public final boolean onlyHurtHorseIfOwnerCanBeHurt;
    public final boolean transferDamageToRider;
    public final boolean deleteHorseOnDeath;
    public final boolean deleteHorseOnDeathByPlayer;
    public final boolean keepEquipmentOnDeath;
    public final long deathCooldown;

    public HorsesPermissionConfig(Horses plugin, YamlConfiguration cfg) {
        this(plugin, cfg, null, null, false);
    }

    public HorsesPermissionConfig(Horses plugin, AbstractConfig parent, String permission) {
        this(plugin, null, parent, permission, true);
    }

    private HorsesPermissionConfig(Horses plugin, YamlConfiguration cfg, AbstractConfig parent, String permission, boolean standalone) {
        super(plugin, cfg, parent, null, permission, standalone);

        Configuration config = loadConfiguration();

        addResourseToHeader("header_permission.txt");

        this.bypassSpawnProtection = ((Boolean) getAndSet("BypassSpawnProtection", Boolean.valueOf(false), Boolean.class)).booleanValue();

        if (!((Boolean) getAndSet("DismissHorseOnTeleport", Boolean.valueOf(true), Boolean.class)).booleanValue()) ;
        this.dismissHorseOnTeleport = true;
        this.startWithSaddle = ((Boolean) getAndSet("StartWithSaddle", Boolean.valueOf(true), Boolean.class)).booleanValue();

        this.maxHorses = ((Number) getAndSet("MaxHorses", Integer.valueOf(5), Number.class)).intValue();
        this.summonDelay = ((Number) getAndSet("SummonDelay", Integer.valueOf(10), Number.class)).intValue();
        this.cancelSummonOnMove = ((Boolean) getAndSet("CancelSummonOnMove", Boolean.valueOf(true), Boolean.class)).booleanValue();

        ConfigurationSection sect = getConfigSect("Commands");

        this.allowBuyCommand = ((Boolean) getAndSet(sect, "AllowBuyCommand", Boolean.valueOf(config.getBoolean("AllowBuyCommand", true)), Boolean.class)).booleanValue();
        config.set("AllowBuyCommand", null);
        this.allowDeleteCommand = ((Boolean) getAndSet(sect, "AllowDeleteCommand", Boolean.valueOf(config.getBoolean("AllowDeleteCommand", true)), Boolean.class)).booleanValue();
        config.set("AllowDeleteCommand", null);
        this.allowDismissCommand = ((Boolean) getAndSet(sect, "AllowDismissCommand", Boolean.valueOf(config.getBoolean("AllowDismissCommand", true)), Boolean.class)).booleanValue();
        config.set("AllowDismissCommand", null);
        this.allowHealCommand = ((Boolean) getAndSet(sect, "AllowHealCommand", Boolean.valueOf(config.getBoolean("AllowHealCommand", true)), Boolean.class)).booleanValue();
        config.set("AllowHealCommand", null);
        this.allowListCommand = ((Boolean) getAndSet(sect, "AllowListCommand", Boolean.valueOf(config.getBoolean("AllowListCommand", true)), Boolean.class)).booleanValue();
        config.set("AllowListCommand", null);
        this.allowRenameCommand = ((Boolean) getAndSet(sect, "AllowRenameCommand", Boolean.valueOf(config.getBoolean("AllowRenameCommand", true)), Boolean.class)).booleanValue();
        config.set("AllowRenameCommand", null);
        this.allowSummonCommand = ((Boolean) getAndSet(sect, "AllowSummonCommand", Boolean.valueOf(config.getBoolean("AllowSummonCommand", true)), Boolean.class)).booleanValue();
        config.set("AllowSummonCommand", null);
        this.allowTypesCommand = ((Boolean) getAndSet(sect, "AllowTypesCommand", Boolean.valueOf(config.getBoolean("AllowTypesCommand", true)), Boolean.class)).booleanValue();
        config.set("AllowTypesCommand", null);

        sect = getConfigSect("Renaming");

        this.blockRenamingOnWildHorses = ((Boolean) getAndSet(sect, "BlockRenamingOnWildHorses", Boolean.valueOf(false), Boolean.class)).booleanValue();
        this.allowClaimingWithNameTag = ((Boolean) getAndSet(sect, "AllowClaimingWithNameTag", Boolean.valueOf(false), Boolean.class)).booleanValue();
        this.allowRenameFromNameTag = ((Boolean) getAndSet(sect, "AllowRenamingFromNameTag", Boolean.valueOf(false), Boolean.class)).booleanValue();
        this.requireNameTagForRenaming = ((Boolean) getAndSet(sect, "RequireNameTagForRenaming", Boolean.valueOf(false), Boolean.class)).booleanValue();

        int maxHorseNameLength = ((Number) getAndSet(sect, "MaxHorseNameLength", Integer.valueOf(20), Number.class)).intValue();
        if (maxHorseNameLength > 30)
            maxHorseNameLength = 30;
        this.maxHorseNameLength = maxHorseNameLength;

        sect = getConfigSect("Damage");

        HashSet protectedDamageCauses = new HashSet();
        this.protectedDamageCauses = Collections.unmodifiableSet(protectedDamageCauses);

        List causeList = sect.getStringList("ProtectedDamageCauses");
        for (int i = 0; i < causeList.size(); i++) {
            String causeStr = (String) causeList.get(i);
            for (EntityDamageEvent.DamageCause cause : EntityDamageEvent.DamageCause.values()) {
                if (cause.toString().equalsIgnoreCase(causeStr)) {
                    causeList.set(i, cause.toString());
                    protectedDamageCauses.add(cause);
                }
            }
        }
        sect.set("ProtectedDamageCauses", causeList);

        this.invincibleHorses = ((Boolean) getAndSet(sect, "InvincibleHorses", Boolean.valueOf(false), Boolean.class)).booleanValue();
        this.protectFromOwner = ((Boolean) getAndSet(sect, "ProtectFromOwner", Boolean.valueOf(true), Boolean.class)).booleanValue();
        this.protectFromPlayers = ((Boolean) getAndSet(sect, "ProtectFromPlayers", Boolean.valueOf(true), Boolean.class)).booleanValue();
        this.protectFromMobs = ((Boolean) getAndSet(sect, "ProtectFromMobs", Boolean.valueOf(true), Boolean.class)).booleanValue();
        this.onlyHurtHorseIfOwnerCanBeHurt = ((Boolean) getAndSet(sect, "OnlyHurtHorseIfOwnerCanBeHurt", Boolean.valueOf(true), Boolean.class)).booleanValue();
        this.transferDamageToRider = ((Boolean) getAndSet(sect, "TransferDamageToRider", Boolean.valueOf(true), Boolean.class)).booleanValue();

        this.deleteHorseOnDeath = ((Boolean) getAndSet(sect, "DeleteHorseOnDeath", Boolean.valueOf(false), Boolean.class)).booleanValue();
        this.deleteHorseOnDeathByPlayer = ((Boolean) getAndSet(sect, "DeleteHorseOnDeathByPlayer", Boolean.valueOf(false), Boolean.class)).booleanValue();
        this.keepEquipmentOnDeath = ((Boolean) getAndSet(sect, "KeepEquipmentOnDeath", Boolean.valueOf(false), Boolean.class)).booleanValue();

        this.deathCooldown = (((Number) getAndSet(sect, "DeathCooldown", Integer.valueOf(120), Number.class)).longValue() * 1000L);

        Map horseTypeConfigs = new HashMap();
        this.horseTypeConfigs = Collections.unmodifiableMap(horseTypeConfigs);

        ConfigurationSection typeSect = getConfigSect("Types");

        set(typeSect, HorseType.PaintCreamy.toString(), getConfigSect(typeSect, "PaintCREAMY"));
        set(typeSect, "PaintCREAMY", null);

        for (HorseType type : HorseType.values()) {
            sect = getConfigSect(typeSect, type.toString());

            horseTypeConfigs.put(type.toString(), new HorseTypeConfig(plugin, sect, type));
        }

        saveConfiguration();
    }

    public HorseTypeConfig getHorseTypeConfig(HorseType type) {
        return (HorseTypeConfig) this.horseTypeConfigs.get(type.toString());
    }

    public HorseTypeConfig getHorseTypeConfigLike(String like) {
        like = like.toLowerCase();

        for (HorseType type : HorseType.values()) {
            HorseTypeConfig cfg = getHorseTypeConfig(type);
            if (cfg.displayName.toLowerCase().startsWith(like)) {
                return cfg;
            }
        }
        return null;
    }

    public HorseTypeConfig getHorseTypeConfig(String typeStr) {
        for (HorseType type : HorseType.values()) {
            HorseTypeConfig cfg = getHorseTypeConfig(type);
            if (cfg.displayName.equalsIgnoreCase(typeStr)) {
                return cfg;
            }
        }
        return null;
    }

    public boolean isProtecting() {
        return (this.invincibleHorses) || (this.protectFromOwner) || (this.protectFromPlayers) || (this.protectFromMobs) || (this.protectedDamageCauses.size() > 0);
    }
}