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
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class HealCommand extends ForgeCommand {
    public HealCommand(ForgePlugin plugin) {
        super(plugin);

        registerAlias("heal", true);
        registerPermission("horses.command.heal");

        registerArgument(new ForgeCommandArgument("^[0-9]+$", 2, false, Messages.Command_Heal_Error_HealAmountInvalid.toString()));

        setAllowOp(true);
        setAllowConsole(false);
        setArgumentString(String.format("[%s]", Messages.Misc_Words_Amount));
        setDescription(Messages.Command_Heal_Description.toString());
    }

    protected void onCommand(CommandSender sender, ForgeArgs args) {
        Player player = (Player) sender;

        HorsesPermissionConfig pcfg = getPlugin().getHorsesConfig().getPermConfig(player);

        if (!pcfg.allowHealCommand) {
            Messages.Misc_Command_Error_ConfigDenyPerm.sendMessage(sender, new Object[]{getMainCommand()});
            return;
        }

        Stable stable = getPlugin().getHorseDatabase().getPlayersStable(player, false);

        PlayerHorse horse = null;

        if (stable != null) {
            horse = stable.getActiveHorse();
        }

        if (horse == null) {
            Messages.Command_Heal_Error_NoActiveHorses.sendMessage(player);
            return;
        }

        int healAmount = Integer.parseInt(args.getArg(0));
        double actualHealAmount = horse.getHealEstimate(healAmount);

        if (getPlugin().getEconomy() != null) {
            HorseTypeConfig cfg = pcfg.getHorseTypeConfig(horse.getType());

            double cost = cfg.healCost * actualHealAmount;

            EconomyResponse result = getPlugin().getEconomy().withdrawPlayer(player, cost);

            if (!result.transactionSuccess()) {
                Messages.Command_Heal_Error_CantAffordHeal.sendMessage(player, new Object[]{actualHealAmount, cost});
                return;
            }

            Messages.Command_Heal_Success_HealedForCost.sendMessage(player, new Object[]{horse.getDisplayName(), actualHealAmount, cost});
        } else {
            Messages.Command_Heal_Success_HealedWithoutCost.sendMessage(player, new Object[]{horse.getDisplayName(), actualHealAmount});
        }

        horse.addHealth(actualHealAmount);
    }

    public Horses getPlugin() {
        return (Horses) super.getPlugin();
    }
}