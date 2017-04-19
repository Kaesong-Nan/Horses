package com.forgenz.horses.config;

import com.forgenz.forgecore.v1_0.ForgeCore;
import com.forgenz.forgecore.v1_0.bukkit.ForgePlugin;
import com.forgenz.forgecore.v1_0.util.BukkitConfigUtil;
import com.forgenz.horses.HorseType;
import org.bukkit.configuration.ConfigurationSection;

public class HorseTypeConfig
        implements ForgeCore {
    private ForgePlugin plugin;
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

        this.displayName = ((String) BukkitConfigUtil.getAndSet(cfg, "DisplayName", String.class, type.toString()));

        double horseHp = ((Number) BukkitConfigUtil.getAndSet(cfg, "DefaultHealth", Number.class, Double.valueOf(12.0D))).doubleValue();
        if (horseHp < 0.0D) {
            horseHp = 12.0D;
            BukkitConfigUtil.set(cfg, "DefaultHealth", Double.valueOf(horseHp));
        }

        this.horseHp = horseHp;

        double horseMaxHp = ((Number) BukkitConfigUtil.getAndSet(cfg, "DefaultMaxHealth", Number.class, Double.valueOf(horseHp))).doubleValue();
        if (horseMaxHp < 0.0D) {
            horseMaxHp = horseHp;
            BukkitConfigUtil.set(cfg, "DefaultMaxHealth", Double.valueOf(horseMaxHp));
        } else if (horseHp > horseMaxHp) {
            horseMaxHp = horseHp;
            BukkitConfigUtil.set(cfg, "DefaultMaxHealth", Double.valueOf(horseMaxHp));
        }
        this.horseMaxHp = horseMaxHp;

        this.speed = ((Number) BukkitConfigUtil.getAndSet(cfg, "Speed", Number.class, Double.valueOf(0.225D))).doubleValue();
        this.jumpStrength = ((Number) BukkitConfigUtil.getAndSet(cfg, "JumpStrength", Number.class, Double.valueOf(0.7D))).doubleValue();

        this.horseMaximumHpUpgrade = ((Number) BukkitConfigUtil.getAndSet(cfg, "MaxHpUpgrade", Number.class, Double.valueOf(30.0D))).doubleValue();

        this.protectFromDeletionOnDeath = ((Boolean) BukkitConfigUtil.getAndSet(cfg, "ProtectFromDeletionOnDeath", Boolean.class, Boolean.valueOf(false))).booleanValue();

        if (getPlugin().getEconomy() != null) {
            this.buyCost = ((Number) BukkitConfigUtil.getAndSet(cfg, "BuyCost", Number.class, Double.valueOf(10.0D))).doubleValue();
            this.wildClaimCost = ((Number) BukkitConfigUtil.getAndSet(cfg, "WildClaimCost", Number.class, Double.valueOf(0.0D))).doubleValue();
            this.healCost = ((Number) BukkitConfigUtil.getAndSet(cfg, "HealCost", Number.class, Double.valueOf(10.0D))).doubleValue();
            this.hpUpgradeCost = ((Number) BukkitConfigUtil.getAndSet(cfg, "HpUpgradeCost", Number.class, Double.valueOf(10.0D))).doubleValue();
            this.renameCost = ((Number) BukkitConfigUtil.getAndSet(cfg, "RenameCost", Number.class, Double.valueOf(5.0D))).doubleValue();
        } else {
            this.buyCost = (this.wildClaimCost = this.healCost = this.hpUpgradeCost = this.renameCost = 0.0D);
        }
    }

    public ForgePlugin getPlugin() {
        return this.plugin;
    }
}