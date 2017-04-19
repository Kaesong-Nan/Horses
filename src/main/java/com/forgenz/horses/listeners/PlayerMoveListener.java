package com.forgenz.horses.listeners;

import com.forgenz.forgecore.v1_0.bukkit.ForgeListener;
import com.forgenz.horses.Horses;
import com.forgenz.horses.config.HorsesPermissionConfig;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class PlayerMoveListener extends ForgeListener {
    public PlayerMoveListener(Horses plugin) {
        super(plugin);

        if ((getPlugin().getSummonCmd() != null) && (getPlugin().getHorsesConfig().trackMovements())) {
            register();
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        Location from = event.getFrom();
        Location to = event.getTo();

        if ((from.getBlockX() != to.getBlockX()) || (from.getBlockZ() != to.getBlockZ()) || (from.getBlockY() != to.getBlockY())) {
            handleMovement(event.getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        handleMovement(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerUsePortal(PlayerPortalEvent event) {
        handleMovement(event.getPlayer());
    }

    private void handleMovement(Player player) {
        if (!getPlugin().getSummonCmd().isSummoning(player)) {
            return;
        }
        HorsesPermissionConfig cfg = getPlugin().getHorsesConfig().getPermConfig(player);

        if (!cfg.cancelSummonOnMove) {
            return;
        }
        getPlugin().getSummonCmd().cancelSummon(player);
    }

    public Horses getPlugin() {
        return (Horses) super.getPlugin();
    }
}