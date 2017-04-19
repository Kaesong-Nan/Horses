package com.forgenz.horses.command;

import com.forgenz.forgecore.v1_0.bukkit.ForgePlugin;
import com.forgenz.forgecore.v1_0.command.ForgeArgs;
import com.forgenz.forgecore.v1_0.command.ForgeCommand;
import com.forgenz.horses.Horses;
import com.forgenz.horses.database.HorseDatabase;
import com.forgenz.horses.database.HorseDatabaseStorageType;
import com.forgenz.horses.database.YamlDatabase;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.logging.Level;

/**
 * Created by john on 7/29/15.
 */
public class ConvertHorsesCommand extends ForgeCommand{
    public ConvertHorsesCommand(ForgePlugin plugin) {
        super(plugin);

        registerAlias("converthorses", true);
        registerPermission("horses.set.admin");

        setAllowOp(true);
        setAllowConsole(true);
        setDescription("Converts undead / skeleton horse to normal horses for people who do not have tame permissions");
    }

    @Override
    protected void onCommand(CommandSender paramCommandSender, ForgeArgs paramForgeArgs) {
        HorseDatabase database = getPlugin().getHorseDatabase();
        if (database.getType() != HorseDatabaseStorageType.YAML) {
            paramCommandSender.sendMessage(ChatColor.RED +"Must be yaml db");
            return;
        }
        YamlDatabase yamlDb = (YamlDatabase) database;
        try {
            getPlugin().onDisable();
            yamlDb.fixUndeadSkel();
            getPlugin().onEnable();
        } catch (Throwable e) {
            getPlugin().log(Level.SEVERE, "Failed to reload Horses", e);
            return;
        }
        paramCommandSender.sendMessage("Fixed");
    }
    public Horses getPlugin() {
        return (Horses) super.getPlugin();
    }

}
