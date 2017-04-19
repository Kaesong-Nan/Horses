package com.forgenz.horses.command;

import com.forgenz.forgecore.v1_0.command.ForgeArgs;
import com.forgenz.forgecore.v1_0.command.ForgeCommand;
import com.forgenz.horses.Horses;
import com.forgenz.horses.Messages;
import org.bukkit.command.CommandSender;

import java.util.logging.Level;

public class ReloadCommand extends ForgeCommand {
    public ReloadCommand(Horses plugin) {
        super(plugin);

        registerAlias("reload", true);
        registerPermission("horses.command.reload");

        setAllowOp(true);
        setAllowConsole(true);

        setDescription(Messages.Command_Reload_Description.toString());
    }

    protected void onCommand(CommandSender sender, ForgeArgs args) {
        try {
            getPlugin().onDisable();
            getPlugin().onEnable();
        } catch (Throwable e) {
            Messages.Command_Reload_Error_FailedToReload.sendMessage(sender);
            getPlugin().log(Level.SEVERE, "Failed to reload Horses", e);
            return;
        }

        Messages.Command_Reload_Success.sendMessage(sender);
    }
}