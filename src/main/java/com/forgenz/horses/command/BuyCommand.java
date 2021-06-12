package com.forgenz.horses.command;

import com.forgenz.forgecore.v1_0.bukkit.ForgePlugin;
import com.forgenz.forgecore.v1_0.command.ForgeArgs;
import com.forgenz.forgecore.v1_0.command.ForgeCommand;
import com.forgenz.forgecore.v1_0.command.ForgeCommandArgument;
import com.forgenz.horses.*;
import com.forgenz.horses.config.HorseTypeConfig;
import com.forgenz.horses.config.HorsesConfig;
import com.forgenz.horses.config.HorsesPermissionConfig;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BuyCommand extends ForgeCommand {
    private final Location cacheLoc = new Location(null, 0.0D, 0.0D, 0.0D);

    public BuyCommand(ForgePlugin plugin) {
        super(plugin);

        registerAlias("buy", true);
        registerAlias("b", false);
        registerPermission("horses.command.buy");

        registerArgument(new ForgeCommandArgument(getPlugin().getHorsesConfig().forceEnglishCharacters ? "^[a-z0-9_&]+$" : "^[^ ]+$", 2, false, Messages.Misc_Command_Error_NameValidCharacters.toString()));
        registerArgument(new ForgeCommandArgument(getPlugin().getHorsesConfig().forceEnglishCharacters ? "^[a-z0-9_&]+$" : "^[^ ]+$", 2, false, Messages.Command_Buy_Error_Type.toString()));

        setAllowOp(true);
        setAllowConsole(false);
        setArgumentString(String.format("<%1$s%2$s> <%1$s%3$s>", Messages.Misc_Words_Horse, Messages.Misc_Words_Name, Messages.Misc_Words_Type));
        setDescription(Messages.Command_Buy_Description.toString());
    }

    protected void onCommand(CommandSender sender, ForgeArgs args) {
        Player player = (Player) sender;

        HorsesConfig cfg = getPlugin().getHorsesConfig();
        HorsesPermissionConfig pcfg = cfg.getPermConfig(player);

        if (!pcfg.allowBuyCommand) {
            Messages.Misc_Command_Error_ConfigDenyPerm.sendMessage(sender, new Object[]{getMainCommand()});
            return;
        }

        String name = args.getArg(0);

        HorseTypeConfig typecfg = pcfg.getHorseTypeConfigLike(args.getArg(1));

        if (typecfg == null) {
            Messages.Command_Buy_Error_InvalidHorseType.sendMessage(player, new Object[]{args.getArg(1)});
            return;
        }

        HorseType type = typecfg.type;

        if (!player.hasPermission(type.getPermission())) {
            Messages.Command_Buy_Error_NoPermissionForThisType.sendMessage(player, new Object[]{typecfg.displayName});
            return;
        }


        if (name.length() > pcfg.maxHorseNameLength) {
            Messages.Misc_Command_Error_HorseNameTooLong.sendMessage(player, new Object[]{pcfg.maxHorseNameLength});
            return;
        }

        if (name.contains("&")) {
            if (!player.hasPermission("horses.colour")) {
                Messages.Misc_Command_Error_CantUseColor.sendMessage(player);
                return;
            }
            if ((!player.hasPermission("horses.formattingcodes")) && (PlayerHorse.FORMATTING_CODES_PATTERN.matcher(args.getArg(1)).find())) {
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

        if ((cfg.worldGuardCfg != null) && (!cfg.worldGuardCfg.allowCommand(cfg.worldGuardCfg.commandBuyAllowedRegions, player.getLocation(this.cacheLoc)))) {
            Messages.Command_Buy_Error_WorldGuard_CantUseBuyHere.sendMessage(player);
            return;
        }

        Stable stable = getPlugin().getHorseDatabase().getPlayersStable(player);

        if (stable.getHorseCount() >= pcfg.maxHorses) {
            Messages.Command_Buy_Error_TooManyHorses.sendMessage(player, new Object[]{pcfg.maxHorses});
            return;
        }

        if (stable.findHorse(name, true) != null) {
            Messages.Command_Buy_Error_AlreadyHaveAHorseWithThatName.sendMessage(player, new Object[]{args.getArg(0)});
            return;
        }

        if (cfg.rejectedHorseNamePattern.matcher(name).find()) {
            Messages.Misc_Command_Error_IllegalHorseNamePattern.sendMessage(player);
            return;
        }

        if ((getPlugin().getEconomy() != null) && (typecfg.buyCost > 0.0D)) {
            EconomyResponse responce = getPlugin().getEconomy().withdrawPlayer(player.getName(), typecfg.buyCost);

            if (!responce.transactionSuccess()) {
                Messages.Command_Buy_Error_CantAffordHorse.sendMessage(player, new Object[]{typecfg.buyCost});
                return;
            }

            Messages.Command_Buy_Success_BoughtHorse.sendMessage(player, new Object[]{typecfg.buyCost});
        }

        PlayerHorse horse = stable.createHorse(args.getArg(0), typecfg, pcfg.startWithSaddle);

        Messages.Command_Buy_Success_Completion.sendMessage(player, new Object[]{args.getCommandUsed(), horse.getName()});
    }

    public Horses getPlugin() {
        return (Horses) super.getPlugin();
    }
}