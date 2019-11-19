package com.forgenz.horses.listeners;

import com.forgenz.forgecore.v1_0.bukkit.ForgeListener;
import com.forgenz.horses.Horses;
import com.forgenz.horses.PlayerHorse;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

public class HorseSpawnListener extends ForgeListener {
    private boolean spawning;

    public HorseSpawnListener(Horses plugin) {
        super(plugin);

        register();
    }

    public void setSpawning() {
        this.spawning = true;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onHorseSpawn(CreatureSpawnEvent event) {
        if (event.getEntityType() != EntityType.HORSE && event.getEntityType() != EntityType.SKELETON_HORSE && event.getEntityType() != EntityType.ZOMBIE_HORSE && event.getEntityType() != EntityType.DONKEY && event.getEntityType() != EntityType.MULE) {
            return;
        }
        if (this.spawning) {
            event.setCancelled(false);
            this.spawning = false;
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void chunkUnloadEvent(ChunkUnloadEvent event) {
        for (Entity e : event.getChunk().getEntities()) {
            if (e.getType() == EntityType.HORSE || e.getType() == EntityType.SKELETON_HORSE || e.getType() == EntityType.ZOMBIE_HORSE || e.getType() == EntityType.DONKEY || e.getType() == EntityType.MULE) {
                if (e.hasMetadata("Horses.Ownership")) {
                    PlayerHorse playerHorse = (PlayerHorse) e.getMetadata("Horses.Ownership").get(0).value();
                    getPlugin().getLogger().info("Removed un chunk loaded horse");
                    playerHorse.removeHorse();
                }
            }
        }
    }
}