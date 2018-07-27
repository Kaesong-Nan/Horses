package com.forgenz.horses.listeners;

import com.forgenz.forgecore.v1_0.bukkit.ForgeListener;
import com.forgenz.horses.Horses;
import com.forgenz.horses.Messages;
import com.forgenz.horses.PlayerHorse;
import com.forgenz.horses.config.HorseTypeConfig;
import com.forgenz.horses.config.HorsesConfig;
import com.forgenz.horses.config.HorsesPermissionConfig;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;

public class HorseDeathListener extends ForgeListener {
    public HorseDeathListener(final Horses plugin) {
        super(plugin);
        
        register();
    }
    
    private void debug(String msg) {
        final Player zombiemold = Bukkit.getPlayer("Zombiemold");
        if(zombiemold != null) {
            zombiemold.sendMessage(ChatColor.RED + "HORSES DEBUG: " + ChatColor.GRAY + msg);
        }
    }
    
    @SuppressWarnings("TypeMayBeWeakened")
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onHorseDie(final EntityDeathEvent event) {
        if(event.getEntityType() != EntityType.HORSE) {
            debug("Entity is not horse");
            return;
        }
        debug("Handling horse death");
    
        final Horse horse = (Horse) event.getEntity();
    
        final PlayerHorse horseData = PlayerHorse.getFromEntity(horse);
    
        if(horseData == null) {
            debug("Horse data is null");
            return;
        }
        
        final HorsesConfig cfg = getPlugin().getHorsesConfig();
        final HorsesPermissionConfig pcfg = cfg.getPermConfig(horseData.getStable().getPlayerOwner());
        final HorseTypeConfig typeCfg = pcfg.getHorseTypeConfig(horseData.getType());
        
        if(!typeCfg.protectFromDeletionOnDeath && (pcfg.deleteHorseOnDeath || pcfg.deleteHorseOnDeathByPlayer)) {
            boolean delete = pcfg.deleteHorseOnDeath;
            if(!delete) {
                if(event.getEntity().getLastDamageCause().getClass() == EntityDamageByEntityEvent.class) {
                    final EntityDamageByEntityEvent e = (EntityDamageByEntityEvent) event.getEntity().getLastDamageCause();
                    final Player killer = DamageListener.getPlayerDamager(e.getDamager());
                    
                    if(killer != null) {
                        delete = true;
                    }
                }
            }
            
            if(delete) {
                Messages.Event_Death_HorseDiedAndWasDeleted.sendMessage(Bukkit.getPlayerExact(horseData.getStable().getOwner()), new Object[] {horseData.getName()});
                horseData.deleteHorse();
                debug("Deleting horse on death");
                return;
            }
        }
        
        event.setDroppedExp(0);
        event.getDrops().clear();
        debug("Supposedly cleared drops");
        
        horseData.removeHorse();
        
        horseData.setMaxHealth(typeCfg.horseMaxHp);
        horseData.setHealth(typeCfg.horseHp);
        
        horseData.setLastDeath(System.currentTimeMillis());
        debug("All done handling");
    }
    
    public Horses getPlugin() {
        return (Horses) super.getPlugin();
    }
}