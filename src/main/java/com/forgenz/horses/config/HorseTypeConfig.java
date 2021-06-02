package com.forgenz.horses.config;

import com.forgenz.forgecore.v1_0.ForgeCore;
import com.forgenz.forgecore.v1_0.bukkit.ForgePlugin;
import com.forgenz.forgecore.v1_0.util.BukkitConfigUtil;
import com.forgenz.horses.HorseType;
import org.bukkit.configuration.ConfigurationSection;

public class HorseTypeConfig
        implements ForgeCore {
    private final ForgePlugin plugin;
    public final HorseType type;
    public final String displayName;
    public final double horseHp;
    public final double horseMaxHp;
    public final double horseMaximumHpUpgrade;
    public final double speed;
    public final double jumpStrength;
    public final boolean protectFromDeletionOnDeath;
    public final double buyCost;
    public final double wildClaimCost;
    public final double healCost;
    public final double hpUpgradeCost;
    public final double renameCost;

    public HorseTypeConfig(ForgePlugin plugin, ConfigurationSection cfg, HorseType type) {
        this.plugin = plugin;
        this.type = type;

        this.displayName = BukkitConfigUtil.getAndSet(cfg, "DisplayName", String.class, type.toString());

        double horseHp = BukkitConfigUtil.getAndSet(cfg, "DefaultHealth", Number.class, 12.0D).doubleValue();
        if (horseHp < 0.0D) {
            horseHp = 12.0D;
            BukkitConfigUtil.set(cfg, "DefaultHealth", horseHp);
        }

        this.horseHp = horseHp;

        double horseMaxHp = BukkitConfigUtil.getAndSet(cfg, "DefaultMaxHealth", Number.class, horseHp).doubleValue();
        if (horseMaxHp < 0.0D) {
            horseMaxHp = horseHp;
            BukkitConfigUtil.set(cfg, "DefaultMaxHealth", horseMaxHp);
        } else if (horseHp > horseMaxHp) {
            horseMaxHp = horseHp;
            BukkitConfigUtil.set(cfg, "DefaultMaxHealth", horseMaxHp);
        }
        this.horseMaxHp = horseMaxHp;

        this.speed = BukkitConfigUtil.getAndSet(cfg, "Speed", Number.class, 0.225D).doubleValue();
        this.jumpStrength = BukkitConfigUtil.getAndSet(cfg, "JumpStrength", Number.class, 0.7D).doubleValue();

        this.horseMaximumHpUpgrade = BukkitConfigUtil.getAndSet(cfg, "MaxHpUpgrade", Number.class, 30.0D).doubleValue();

        this.protectFromDeletionOnDeath = BukkitConfigUtil.getAndSet(cfg, "ProtectFromDeletionOnDeath", Boolean.class, false);

        if (getPlugin().getEconomy() != null) {
            this.buyCost = BukkitConfigUtil.getAndSet(cfg, "BuyCost", Number.class, 10.0D).doubleValue();
            this.wildClaimCost = BukkitConfigUtil.getAndSet(cfg, "WildClaimCost", Number.class, 0.0D).doubleValue();
            this.healCost = BukkitConfigUtil.getAndSet(cfg, "HealCost", Number.class, 10.0D).doubleValue();
            this.hpUpgradeCost = BukkitConfigUtil.getAndSet(cfg, "HpUpgradeCost", Number.class, 10.0D).doubleValue();
            this.renameCost = BukkitConfigUtil.getAndSet(cfg, "RenameCost", Number.class, 5.0D).doubleValue();
        } else {
            this.buyCost = (this.wildClaimCost = this.healCost = this.hpUpgradeCost = this.renameCost = 0.0D);
        }
    }

    public ForgePlugin getPlugin() {
        return this.plugin;
    }
}