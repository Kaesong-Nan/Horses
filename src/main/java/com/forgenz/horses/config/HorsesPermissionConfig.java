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

        this.bypassSpawnProtection = getAndSet("BypassSpawnProtection", false, Boolean.class);

        this.dismissHorseOnTeleport = getAndSet("DismissHorseOnTeleport", true, Boolean.class);
        this.startWithSaddle = getAndSet("StartWithSaddle", true, Boolean.class);

        this.maxHorses = getAndSet("MaxHorses", 5, Number.class).intValue();
        this.summonDelay = getAndSet("SummonDelay", 10, Number.class).intValue();
        this.cancelSummonOnMove = getAndSet("CancelSummonOnMove", true, Boolean.class);

        ConfigurationSection sect = getConfigSect("Commands");

        this.allowBuyCommand = getAndSet(sect, "AllowBuyCommand", config.getBoolean("AllowBuyCommand", true), Boolean.class);
        config.set("AllowBuyCommand", null);
        this.allowDeleteCommand = getAndSet(sect, "AllowDeleteCommand", config.getBoolean("AllowDeleteCommand", true), Boolean.class);
        config.set("AllowDeleteCommand", null);
        this.allowDismissCommand = getAndSet(sect, "AllowDismissCommand", config.getBoolean("AllowDismissCommand", true), Boolean.class);
        config.set("AllowDismissCommand", null);
        this.allowHealCommand = getAndSet(sect, "AllowHealCommand", config.getBoolean("AllowHealCommand", true), Boolean.class);
        config.set("AllowHealCommand", null);
        this.allowListCommand = getAndSet(sect, "AllowListCommand", config.getBoolean("AllowListCommand", true), Boolean.class);
        config.set("AllowListCommand", null);
        this.allowRenameCommand = getAndSet(sect, "AllowRenameCommand", config.getBoolean("AllowRenameCommand", true), Boolean.class);
        config.set("AllowRenameCommand", null);
        this.allowSummonCommand = getAndSet(sect, "AllowSummonCommand", config.getBoolean("AllowSummonCommand", true), Boolean.class);
        config.set("AllowSummonCommand", null);
        this.allowTypesCommand = getAndSet(sect, "AllowTypesCommand", config.getBoolean("AllowTypesCommand", true), Boolean.class);
        config.set("AllowTypesCommand", null);

        sect = getConfigSect("Renaming");

        this.blockRenamingOnWildHorses = getAndSet(sect, "BlockRenamingOnWildHorses", false, Boolean.class);
        this.allowClaimingWithNameTag = getAndSet(sect, "AllowClaimingWithNameTag", false, Boolean.class);
        this.allowRenameFromNameTag = getAndSet(sect, "AllowRenamingFromNameTag", false, Boolean.class);
        this.requireNameTagForRenaming = getAndSet(sect, "RequireNameTagForRenaming", false, Boolean.class);

        int maxHorseNameLength = getAndSet(sect, "MaxHorseNameLength", 20, Number.class).intValue();
        if (maxHorseNameLength > 30)
            maxHorseNameLength = 30;
        this.maxHorseNameLength = maxHorseNameLength;

        sect = getConfigSect("Damage");

        HashSet<EntityDamageEvent.DamageCause> protectedDamageCauses = new HashSet<>();

        List<String> causeList = sect.getStringList("ProtectedDamageCauses");
        for (int i = 0; i < causeList.size(); i++) {
            String causeStr = causeList.get(i);
            for (EntityDamageEvent.DamageCause cause : EntityDamageEvent.DamageCause.values()) {
                if (cause.toString().equalsIgnoreCase(causeStr)) {
                    causeList.set(i, cause.toString());
                    protectedDamageCauses.add(cause);
                }
            }
        }

        this.protectedDamageCauses = Collections.unmodifiableSet(protectedDamageCauses);

        sect.set("ProtectedDamageCauses", causeList);

        this.invincibleHorses = getAndSet(sect, "InvincibleHorses", false, Boolean.class);
        this.protectFromOwner = getAndSet(sect, "ProtectFromOwner", true, Boolean.class);
        this.protectFromPlayers = getAndSet(sect, "ProtectFromPlayers", true, Boolean.class);
        this.protectFromMobs = getAndSet(sect, "ProtectFromMobs", true, Boolean.class);
        this.onlyHurtHorseIfOwnerCanBeHurt = getAndSet(sect, "OnlyHurtHorseIfOwnerCanBeHurt", true, Boolean.class);
        this.transferDamageToRider = getAndSet(sect, "TransferDamageToRider", true, Boolean.class);

        this.deleteHorseOnDeath = getAndSet(sect, "DeleteHorseOnDeath", false, Boolean.class);
        this.deleteHorseOnDeathByPlayer = getAndSet(sect, "DeleteHorseOnDeathByPlayer", false, Boolean.class);
        this.keepEquipmentOnDeath = getAndSet(sect, "KeepEquipmentOnDeath", false, Boolean.class);

        this.deathCooldown = (getAndSet(sect, "DeathCooldown", 120, Number.class).longValue() * 1000L);

        Map<String, HorseTypeConfig> horseTypeConfigs = new HashMap<>();
        ConfigurationSection typeSect = getConfigSect("Types");

        set(typeSect, HorseType.PaintCreamy.toString(), getConfigSect(typeSect, "PaintCREAMY"));
        set(typeSect, "PaintCREAMY", null);

        for (HorseType type : HorseType.values()) {
            sect = getConfigSect(typeSect, type.toString());

            horseTypeConfigs.put(type.toString(), new HorseTypeConfig(plugin, sect, type));
        }

        this.horseTypeConfigs = Collections.unmodifiableMap(horseTypeConfigs);

        saveConfiguration();
    }

    public HorseTypeConfig getHorseTypeConfig(HorseType type) {
        return this.horseTypeConfigs.get(type.toString());
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