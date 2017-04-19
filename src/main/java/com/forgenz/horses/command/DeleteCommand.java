package com.forgenz.horses.command;

import com.forgenz.forgecore.v1_0.command.ForgeArgs;
import com.forgenz.forgecore.v1_0.command.ForgeCommand;
import com.forgenz.forgecore.v1_0.command.ForgeCommandArgument;
import com.forgenz.horses.Horses;
import com.forgenz.horses.Messages;
import com.forgenz.horses.PlayerHorse;
import com.forgenz.horses.Stable;
import com.forgenz.horses.config.HorsesPermissionConfig;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DeleteCommand extends ForgeCommand {
    public DeleteCommand(Horses plugin) {
        super(plugin);

        registerAlias("delete", true);
        registerAlias("del", true);
        registerPermission("horses.command.delete");

        registerArgument(new ForgeCommandArgument(getPlugin().getHorsesConfig().forceEnglishCharacters ? "^[a-z0-9_&]+$" : "^[^ ]+$", 2, false, Messages.Misc_Command_Error_InvalidName.toString()));

        setAllowOp(true);
        setAllowConsole(false);
        setArgumentString(String.format("<%1%s%2$s>", new Object[]{Messages.Misc_Words_Horse, Messages.Misc_Words_Name}));
        setDescription(Messages.Command_Delete_Description.toString());
    }

    protected void onCommand(CommandSender sender, ForgeArgs args) {
        Player player = (Player) sender;

        HorsesPermissionConfig pcfg = getPlugin().getHorsesConfig().getPermConfig(player);

        if (!pcfg.allowDeleteCommand) {
            Messages.Misc_Command_Error_ConfigDenyPerm.sendMessage(sender, new Object[]{getMainCommand()});
            return;
        }

        Stable stable = getPlugin().getHorseDatabase().getPlayersStable(player);

        PlayerHorse horse = stable.findHorse(args.getArg(0), false);

        if (horse == null) {
            Messages.Misc_Command_Error_NoHorseNamed.sendMessage(player, new Object[]{args.getArg(0)});
            return;
        }

        Messages.Command_Delete_Success_DeletedHorse.sendMessage(player, new Object[]{horse.getDisplayName()});
        horse.deleteHorse();
    }

    public Horses getPlugin() {
        return (Horses) super.getPlugin();
    }
}