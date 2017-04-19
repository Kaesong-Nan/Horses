package com.forgenz.horses.database;

import com.forgenz.horses.Horses;
import com.forgenz.horses.PlayerHorse;
import com.forgenz.horses.Stable;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.Collections;
import java.util.List;

public class DummyDatabase extends HorseDatabase
        implements Listener {
    public DummyDatabase(Horses plugin) {
        super(plugin, HorseDatabaseStorageType.DUMMY);

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (event.getPlayer().isOp()) {
            event.getPlayer().sendMessage(ChatColor.RED + "Horses is running in DUMMY mode. Horses data will be lost upon disconnecting");
        }
    }

    protected List<Stable> loadEverything() {
        return Collections.emptyList();
    }

    protected void importStables(List<Stable> stables) {
        getPlugin().warning("Can not import horses into the Dummy database");
    }

    protected Stable loadStable(String player, String stableGroup) {
        return new Stable(getPlugin(), stableGroup, player);
    }

    protected void loadHorses(Stable stable, String stableGroup) {
    }

    protected void saveStable(Stable stable) {
    }

    public void saveHorse(PlayerHorse horse) {
    }

    public boolean deleteHorse(PlayerHorse horse) {
        return true;
    }
}