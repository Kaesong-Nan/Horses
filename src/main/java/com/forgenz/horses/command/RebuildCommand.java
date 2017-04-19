package com.forgenz.horses.command;

import com.forgenz.forgecore.v1_0.bukkit.ForgePlugin;
import com.forgenz.forgecore.v1_0.command.ForgeArgs;
import com.forgenz.forgecore.v1_0.command.ForgeCommand;
import com.forgenz.horses.Horses;
import com.forgenz.horses.Messages;
import com.forgenz.horses.database.HorseDatabase;
import com.forgenz.horses.database.HorseDatabaseStorageType;
import com.forgenz.horses.database.YamlDatabase;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.util.logging.Level;

public class RebuildCommand extends ForgeCommand {
    public RebuildCommand(ForgePlugin plugin) {
        super(plugin);

        registerAlias("rebuild", true);
        registerPermission("horses.command.rebuild");

        setAllowOp(true);
        setAllowConsole(true);
        setDescription("Rebuilds the database to use UUID's");
    }

    protected void onCommand(CommandSender sender, ForgeArgs args) {
        if (!Bukkit.getOnlineMode()) {
            sender.sendMessage("This command is not supported on offline servers");
            return;
        }

        HorseDatabase database = getPlugin().getHorseDatabase();

        if (database.getType() != HorseDatabaseStorageType.YAML) {
            sender.sendMessage("Rebuild only supports YAML database");
            return;
        }

        YamlDatabase yamlDb = (YamlDatabase) database;
        boolean success = false;
        try {
            getPlugin().onDisable();

            success = yamlDb.migrateToUuidDb();

            getPlugin().onEnable();
        } catch (Throwable e) {
            Messages.Command_Reload_Error_FailedToReload.sendMessage(sender);
            getPlugin().log(Level.SEVERE, "Failed to reload Horses", e);
            return;
        }

        sender.sendMessage(new StringBuilder().append("Rebuilt database with ").append(success ? "no errors" : "errors").toString());
        Messages.Command_Reload_Success.sendMessage(sender);
    }

    public Horses getPlugin() {
        return (Horses) super.getPlugin();
    }
}