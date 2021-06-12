package com.forgenz.horses.tasks;

import com.forgenz.forgecore.v1_0.ForgeCore;
import com.forgenz.horses.Horses;
import com.forgenz.horses.Messages;
import com.forgenz.horses.PlayerHorse;
import com.forgenz.horses.Stable;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class HorseDismissTask extends BukkitRunnable
        implements ForgeCore {
    private final Location horseLocCache = new Location(null, 0.0D, 0.0D, 0.0D);
    private final Location playerLocCache = new Location(null, 0.0D, 0.0D, 0.0D);
    private final Horses plugin;
    private Player[] players;
    private int index;

    public HorseDismissTask(Horses plugin) {
        this.plugin = plugin;
    }

    public Horses getPlugin() {
        return this.plugin;
    }

    public void run() {
        if (this.players == null) {
            this.players = Bukkit.getOnlinePlayers().toArray(new Player[0]);
            this.index = 0;
        }

        long start = System.currentTimeMillis();

        while ((this.index < this.players.length) && (System.currentTimeMillis() - start <= 2L)) {
            Player player = this.players[(this.index++)];

            if (player.isValid()) {
                Stable stable = getPlugin().getHorseDatabase().getPlayersStable(player, false);

                if (stable != null) {
                    PlayerHorse horseData = stable.getActiveHorse();

                    if (horseData != null) {
                        if (horseData.getHorse().getWorld() != player.getWorld()) {
                            horseData.removeHorse();
                        } else if (horseData.getHorse().getLocation(this.horseLocCache).distanceSquared(player.getLocation(this.playerLocCache)) > 1024.0D) {
                            Messages.Event_MovedTooFarAway.sendMessage(player, new Object[]{horseData.getDisplayName()});
                            horseData.removeHorse();
                        }
                    }
                }
            }
        }
        if (this.players.length == this.index) {
            this.players = null;
        }
    }
}