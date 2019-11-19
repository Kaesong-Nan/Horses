package com.forgenz.horses.listeners;

import com.forgenz.forgecore.v1_0.bukkit.ForgeListener;
import com.forgenz.horses.Horses;
import com.forgenz.horses.Messages;
import com.forgenz.horses.PlayerHorse;
import com.forgenz.horses.config.HorsesConfig;
import com.forgenz.horses.config.HorsesPermissionConfig;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.hooks.NCPExemptionManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.projectiles.ProjectileSource;

public class DamageListener extends ForgeListener {
    public DamageListener(Horses plugin) {
        super(plugin);

        register();
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntityType() != EntityType.HORSE && event.getEntityType() != EntityType.SKELETON_HORSE && event.getEntityType() != EntityType.ZOMBIE_HORSE && event.getEntityType() != EntityType.DONKEY && event.getEntityType() != EntityType.MULE) {
            return;
        }

        AbstractHorse horse = (AbstractHorse) event.getEntity();

        PlayerHorse horseData = PlayerHorse.getFromEntity(horse);

        if (horseData == null) {
            return;
        }

        Player player = horseData.getStable().getPlayerOwner();

        HorsesConfig cfg = getPlugin().getHorsesConfig();
        HorsesPermissionConfig pcfg = cfg.getPermConfig(player);

        if (pcfg.invincibleHorses) {
            event.setCancelled(true);
            return;
        }

        if (pcfg.protectedDamageCauses.contains(event.getCause())) {
            event.setCancelled(true);
            return;
        }

        if (event.getClass() == EntityDamageByEntityEvent.class) {
            onEntityDamageByEntity((EntityDamageByEntityEvent) event, horse, horseData, pcfg);
        }

        if ((!event.isCancelled()) && ((pcfg.onlyHurtHorseIfOwnerCanBeHurt) || (pcfg.transferDamageToRider))) {
            if (player == null) {
                return;
            }

            EntityDamageEvent e = null;
            double damage = pcfg.transferDamageToRider ? event.getDamage() / horse.getMaxHealth() * player.getMaxHealth() : 0.0D;

            Player exemptedPlayer = null;

            if (event.getClass() == EntityDamageEvent.class) {
                e = new EntityDamageEvent(player, event.getCause(), damage);
            } else if (event.getClass() == EntityDamageByEntityEvent.class) {
                Entity damager = ((EntityDamageByEntityEvent) event).getDamager();
                e = new EntityDamageByEntityEvent(damager, player, event.getCause(), damage);

                if ((damager.getType() == EntityType.PLAYER) && (getPlugin().isNoCheatPlusEnabled())) {
                    exemptedPlayer = (Player) damager;
                    if (!NCPExemptionManager.isExempted(player, CheckType.ALL)) {
                        NCPExemptionManager.exemptPermanently(exemptedPlayer, CheckType.ALL);
                    } else {
                        exemptedPlayer = null;
                    }
                }
            } else if (event.getClass() == EntityDamageByBlockEvent.class) {
                e = new EntityDamageByBlockEvent(((EntityDamageByBlockEvent) event).getDamager(), player, event.getCause(), damage);
            } else {
                return;
            }
            Bukkit.getPluginManager().callEvent(e);

            if ((getPlugin().isNoCheatPlusEnabled()) && (exemptedPlayer != null)) {
                NCPExemptionManager.unexempt(player, CheckType.ALL);
            }

            if ((!e.isCancelled()) && (pcfg.transferDamageToRider) && (horse.getPassenger() == player)) {
                event.setCancelled(true);
                player.damage(e.getDamage());
            } else {
                event.setCancelled(e.isCancelled());
            }
        }
    }

    public void onEntityDamageByEntity(EntityDamageByEntityEvent event, AbstractHorse horse, PlayerHorse horseData, HorsesPermissionConfig cfg) {
        Player player = getPlayerDamager(event.getDamager());

        if (player != null) {
            if ((cfg.protectFromOwner) && (horseData.getStable().getOwner().equals(player.getName()))) {
                event.setCancelled(true);
            } else if (cfg.protectFromPlayers && !horseData.getStable().getOwner().equals(player.getName())) {
                Messages.Event_Damage_Error_CantHurtOthersHorses.sendMessage(player);
                event.setCancelled(true);
            }

        } else if (cfg.protectFromMobs) {
            event.setCancelled(true);
        }
    }

    public static Player getPlayerDamager(Entity entity) {
        if (entity == null) {
            return null;
        }
        if (entity.getType() == EntityType.PLAYER) {
            return (Player) entity;
        }
        if (entity.getType() == EntityType.PRIMED_TNT) {
            return castPlayer(((TNTPrimed) entity).getSource());
        }
        if ((entity instanceof Projectile)) {
            ProjectileSource source = ((Projectile) entity).getShooter();
            if ((source instanceof Player)) {
                castPlayer((Entity) source);
            }
        }
        return null;
    }

    public static Player castPlayer(Entity entity) {
        if (entity == null) {
            return null;
        }
        if (entity.getType() == EntityType.PLAYER) {
            return (Player) entity;
        }
        return null;
    }

    public Horses getPlugin() {
        return (Horses) super.getPlugin();
    }
}