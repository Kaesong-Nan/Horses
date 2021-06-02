package com.forgenz.horses.command;

import com.forgenz.forgecore.v1_0.bukkit.ForgePlugin;
import com.forgenz.forgecore.v1_0.command.ForgeArgs;
import com.forgenz.forgecore.v1_0.command.ForgeCommand;
import com.forgenz.horses.Horses;
import com.forgenz.horses.PlayerHorse;
import com.forgenz.horses.Stable;
import com.forgenz.horses.database.HorseDatabase;
import com.forgenz.horses.database.HorseDatabaseStorageType;
import com.forgenz.horses.database.YamlDatabase;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.logging.Level;

/**
 * Created by john on 10/29/15.
 */
public class FixMaxCommand extends ForgeCommand{
    public FixMaxCommand(ForgePlugin plugin) {
        super(plugin);

        registerAlias("fixmaxhorses", true);
        registerPermission("horses.set.admin");

        setAllowOp(true);
        setAllowConsole(true);
        setDescription("Fixes exploited horses with too high of stats");
    }

    @Override
    protected void onCommand(final CommandSender paramCommandSender, ForgeArgs paramForgeArgs) {
        HorseDatabase database = getPlugin().getHorseDatabase();
        if (database.getType() != HorseDatabaseStorageType.YAML) {
            paramCommandSender.sendMessage(ChatColor.RED + "Must be yaml db");
            return;
        }
        final YamlDatabase yamlDb = (YamlDatabase) database;
        Bukkit.getScheduler().runTaskAsynchronously(getPlugin(), () -> {
            try {
                for (Stable stable : yamlDb.loadEverything()) {
                    try {
                        for (PlayerHorse playerHorse : stable) {
                            playerHorse.adjustValuesToMax();
                        }
                    } catch (Exception e2) {
                        e2.printStackTrace();
                    }
                }
            } catch (Throwable e) {
                getPlugin().log(Level.SEVERE, "Failed to reload Horses", e);
                return;
            }
            paramCommandSender.sendMessage("Fixed");
        });
    }
    public Horses getPlugin() {
        return (Horses) super.getPlugin();
    }

}
