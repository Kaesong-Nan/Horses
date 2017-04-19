package com.forgenz.horses.listeners;

import com.forgenz.forgecore.v1_0.bukkit.ForgeListener;
import com.forgenz.horses.Horses;
import com.forgenz.horses.Messages;
import com.forgenz.horses.PlayerHorse;
import com.forgenz.horses.config.HorseTypeConfig;
import com.forgenz.horses.config.HorsesConfig;
import com.forgenz.horses.config.HorsesPermissionConfig;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;

public class HorseDeathListener extends ForgeListener {
    public HorseDeathListener(Horses plugin) {
        super(plugin);

        register();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onHorseDie(EntityDeathEvent event) {
        if (event.getEntityType() != EntityType.HORSE) {
            return;
        }

        Horse horse = (Horse) event.getEntity();

        PlayerHorse horseData = PlayerHorse.getFromEntity(horse);

        if (horseData == null) {
            return;
        }

        HorsesConfig cfg = getPlugin().getHorsesConfig();
        HorsesPermissionConfig pcfg = cfg.getPermConfig(horseData.getStable().getPlayerOwner());
        HorseTypeConfig typeCfg = pcfg.getHorseTypeConfig(horseData.getType());

        if ((!typeCfg.protectFromDeletionOnDeath) && ((pcfg.deleteHorseOnDeath) || (pcfg.deleteHorseOnDeathByPlayer))) {
            boolean delete = pcfg.deleteHorseOnDeath;
            if ((!delete) && (pcfg.deleteHorseOnDeathByPlayer)) {
                if (event.getEntity().getLastDamageCause().getClass() == EntityDamageByEntityEvent.class) {
                    EntityDamageByEntityEvent e = (EntityDamageByEntityEvent) event.getEntity().getLastDamageCause();
                    Player killer = DamageListener.getPlayerDamager(e.getDamager());

                    if (killer != null) {
                        delete = true;
                    }
                }
            }

            if (delete) {
                Messages.Event_Death_HorseDiedAndWasDeleted.sendMessage(Bukkit.getPlayerExact(horseData.getStable().getOwner()), new Object[]{horseData.getName()});
                horseData.deleteHorse();
                return;
            }
        }

        event.setDroppedExp(0);
        event.getDrops().clear();

        horseData.removeHorse();

        horseData.setMaxHealth(typeCfg.horseMaxHp);
        horseData.setHealth(typeCfg.horseHp);

        horseData.setLastDeath(System.currentTimeMillis());
    }

    public Horses getPlugin() {
        return (Horses) super.getPlugin();
    }
}