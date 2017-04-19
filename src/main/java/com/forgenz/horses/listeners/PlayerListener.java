package com.forgenz.horses.listeners;

import com.forgenz.forgecore.v1_0.bukkit.ForgeListener;
import com.forgenz.horses.Horses;
import com.forgenz.horses.PlayerHorse;
import com.forgenz.horses.Stable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public class PlayerListener extends ForgeListener {
    public PlayerListener(Horses plugin) {
        super(plugin);

        register();
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Stable stable = getPlugin().getHorseDatabase().getPlayersStable(event.getPlayer(), false);

        if (stable != null) {
            PlayerHorse horse = stable.getActiveHorse();

            if (horse != null) {
                horse.removeHorse();
            }
        }
    }

    @EventHandler
    public void onPlayerKick(PlayerKickEvent event) {
        handleQuit(event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        handleQuit(event.getPlayer());
    }

    public void handleQuit(Player player) {
        Stable stable = getPlugin().getHorseDatabase().getPlayersStable(player, false);

        if (stable != null) {
            getPlugin().getHorseDatabase().unload(stable);
        }
    }

    public Horses getPlugin() {
        return (Horses) super.getPlugin();
    }
}