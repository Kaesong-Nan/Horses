package com.forgenz.horses.listeners;

import com.forgenz.forgecore.v1_0.bukkit.ForgeListener;
import com.forgenz.horses.Horses;
import com.forgenz.horses.Messages;
import com.forgenz.horses.PlayerHorse;
import com.forgenz.horses.Stable;
import com.forgenz.horses.config.HorsesPermissionConfig;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerTeleportEvent;

public class TeleportListener extends ForgeListener {
    public TeleportListener(Horses plugin) {
        super(plugin);

        register();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Stable stable = getPlugin().getHorseDatabase().getPlayersStable(event.getPlayer(), false);

        if (stable == null) {
            return;
        }

        PlayerHorse horse = stable.getActiveHorse();
        if ((horse != null) && (horse.getHorse() != null) && (horse.getHorse().isValid())) {
            HorsesPermissionConfig cfg = getPlugin().getHorsesConfig().getPermConfig(event.getPlayer());

            if (cfg.dismissHorseOnTeleport) {
                if ((event.getFrom().getWorld() != event.getTo().getWorld()) || (event.getFrom().distanceSquared(event.getTo()) > 1024.0D)) {
                    Messages.Event_MovedTooFarAway.sendMessage(event.getPlayer(), new Object[]{horse.getDisplayName()});
                    horse.removeHorse();
                }

            } else {
                horse.getHorse().teleport(event.getTo());
            }
        }
    }

    public Horses getPlugin() {
        return (Horses) super.getPlugin();
    }
}