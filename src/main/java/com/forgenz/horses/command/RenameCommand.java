package com.forgenz.horses.command;

import com.forgenz.forgecore.v1_0.bukkit.ForgePlugin;
import com.forgenz.forgecore.v1_0.command.ForgeArgs;
import com.forgenz.forgecore.v1_0.command.ForgeCommand;
import com.forgenz.forgecore.v1_0.command.ForgeCommandArgument;
import com.forgenz.horses.Horses;
import com.forgenz.horses.Messages;
import com.forgenz.horses.PlayerHorse;
import com.forgenz.horses.Stable;
import com.forgenz.horses.config.HorsesConfig;
import com.forgenz.horses.config.HorsesPermissionConfig;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class RenameCommand extends ForgeCommand {
    public RenameCommand(ForgePlugin plugin) {
        super(plugin);

        registerAlias("rename", true);
        registerPermission("horses.command.rename");

        registerArgument(new ForgeCommandArgument(getPlugin().getHorsesConfig().forceEnglishCharacters ? "^[a-z0-9_&]+$" : "^[^ ]+$", 2, false, Messages.Misc_Command_Error_InvalidName.toString()));
        registerArgument(new ForgeCommandArgument(getPlugin().getHorsesConfig().forceEnglishCharacters ? "^[a-z0-9_&]+$" : "^[^ ]+$", 2, false, Messages.Misc_Command_Error_NameValidCharacters.toString()));

        setAllowOp(true);
        setAllowConsole(false);
        setArgumentString(String.format("<%1%s%2$s> <%3$s%4$s>", new Object[]{Messages.Misc_Words_Horse, Messages.Misc_Words_Name, Messages.Misc_Words_New, Messages.Misc_Words_Name}));
        setDescription(Messages.Command_Rename_Description.toString());
    }

    protected void onCommand(CommandSender sender, ForgeArgs args) {
        Player player = (Player) sender;

        HorsesConfig cfg = getPlugin().getHorsesConfig();
        HorsesPermissionConfig pcfg = cfg.getPermConfig(player);

        if (!pcfg.allowRenameCommand) {
            Messages.Misc_Command_Error_ConfigDenyPerm.sendMessage(sender, new Object[]{getMainCommand()});
            return;
        }

        Stable stable = getPlugin().getHorseDatabase().getPlayersStable(player);

        String name = args.getArg(1);

        PlayerHorse horse = stable.findHorse(args.getArg(0), false);

        if (horse == null) {
            Messages.Misc_Command_Error_NoHorseNamed.sendMessage(player, new Object[]{args.getArg(0)});
            return;
        }

        if (name.contains("&")) {
            if (!player.hasPermission("horses.colour")) {
                Messages.Misc_Command_Error_CantUseColor.sendMessage(player);
                return;
            }
            if ((!player.hasPermission("horses.formattingcodes")) && (PlayerHorse.FORMATTING_CODES_PATTERN.matcher(name).find())) {
                Messages.Misc_Command_Error_CantUseFormattingCodes.sendMessage(player);
                return;
            }

            name = ChatColor.translateAlternateColorCodes('&', name);
            name = ChatColor.stripColor(name);
        }

        if (name.length() == 0) {
            Messages.Misc_Command_Error_HorseNameEmpty.sendMessage(player);
            return;
        }

        if (cfg.rejectedHorseNamePattern.matcher(name).find()) {
            Messages.Misc_Command_Error_IllegalHorseNamePattern.sendMessage(player);
            return;
        }

        if (pcfg.requireNameTagForRenaming) {
            if (player.getItemInHand().getType() != Material.NAME_TAG) {
                Messages.Command_Rename_Error_RequireNametag.sendMessage(player);
                return;
            }

            player.setItemInHand(new ItemStack(Material.AIR));
        }
        if (!horse.isRenamable()) {
            player.sendMessage(ChatColor.RED + "This horse is not renameable!");
            return;
        }
        if (getPlugin().getHorsesConfig().getBlackListNames().contains(name)) {
            player.sendMessage(ChatColor.RED + "You can't rename a horse to that!");
            return;
        }

        String oldDisplayName = horse.getDisplayName();
        horse.rename(args.getArg(1));
        Messages.Command_Rename_Success_Renamed.sendMessage(player, new Object[]{oldDisplayName, horse.getDisplayName()});
    }

    public Horses getPlugin() {
        return (Horses) super.getPlugin();
    }
}