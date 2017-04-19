package com.forgenz.horses.command;

import com.forgenz.forgecore.v1_0.bukkit.ForgePlugin;
import com.forgenz.forgecore.v1_0.command.ForgeArgs;
import com.forgenz.forgecore.v1_0.command.ForgeCommand;
import com.forgenz.forgecore.v1_0.command.ForgeCommandArgument;
import com.forgenz.horses.Horses;
import com.forgenz.horses.Messages;
import com.forgenz.horses.PlayerHorse;
import com.forgenz.horses.Stable;
import com.forgenz.horses.config.HorseTypeConfig;
import com.forgenz.horses.config.HorsesPermissionConfig;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ListCommand extends ForgeCommand {
    public ListCommand(ForgePlugin plugin) {
        super(plugin);

        registerAlias("list", true);
        registerAlias("l", false);
        registerPermission("horses.command.list");

        registerArgument(new ForgeCommandArgument("^[a-z0-9_]{0,16}$", 2, true, Messages.Command_List_Error_InvalidCharactersPlayerName.toString()));

        setAllowOp(true);
        setArgumentString(String.format("[%s]", new Object[]{Messages.Misc_Words_Player}));
        setDescription(Messages.Command_List_Description.toString());
    }

    protected void onCommand(CommandSender sender, ForgeArgs args) {
        boolean senderIsPlayer = sender instanceof Player;

        if (senderIsPlayer) {
            HorsesPermissionConfig pcfg = getPlugin().getHorsesConfig().getPermConfig((Player) sender);

            if (!pcfg.allowListCommand) {
                Messages.Misc_Command_Error_ConfigDenyPerm.sendMessage(sender, new Object[]{getMainCommand()});
                return;
            }
        }

        Player player = null;
        if (args.getNumArgs() >= 1) {
            if (!sender.hasPermission("horses.command.list.all")) {
                Messages.Command_List_Error_NoPermissionToListPlayersHorses.sendMessage(sender);
                return;
            }

            player = Bukkit.getPlayer(args.getArg(0));

            if (player == null) {
                Messages.Command_List_Error_CouldNotFindPlayer.sendMessage(sender, new Object[]{args.getArg(0)});
            }

        } else if ((sender instanceof Player)) {
            player = (Player) sender;
        } else {
            Messages.Misc_Command_Error_CantBeUsedFromConsole.sendMessage(sender);
            return;
        }

        Stable stable = getPlugin().getHorseDatabase().getPlayersStable(player);

        StringBuilder horses = new StringBuilder();

        for (PlayerHorse horse : stable) {
            if (horses.length() > 0) {
                horses.append(Messages.Command_List_Success_HorseListSeparator);
            }
            horses.append(Messages.Command_List_Success_HorseNamePrefix).append(horse.getDisplayName());

            HorseTypeConfig cfg = getPlugin().getHorsesConfig().getHorseTypeConfig(player, horse.getType());
            horses.append(":").append(Messages.Command_List_Success_HorseTypePrefix).append(cfg.displayName);
        }

        if (horses.length() == 0) {
            Messages.Command_List_Error_NoHorses.sendMessage(sender);
        } else {
            if (player != sender)
                horses.insert(0, String.format(Messages.Command_List_Success_InitialMessageOtherPlayer.toString(), new Object[]{player.getName()}));
            else
                horses.insert(0, Messages.Command_List_Success_InitialMessage.toString());
            sender.sendMessage(horses.toString());
        }
    }

    public Horses getPlugin() {
        return (Horses) super.getPlugin();
    }
}