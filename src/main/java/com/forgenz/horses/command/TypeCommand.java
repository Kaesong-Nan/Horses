package com.forgenz.horses.command;

import com.forgenz.forgecore.v1_0.bukkit.ForgePlugin;
import com.forgenz.forgecore.v1_0.command.ForgeArgs;
import com.forgenz.forgecore.v1_0.command.ForgeCommand;
import com.forgenz.forgecore.v1_0.command.ForgeCommandArgument;
import com.forgenz.horses.HorseType;
import com.forgenz.horses.Horses;
import com.forgenz.horses.Messages;
import com.forgenz.horses.config.HorseTypeConfig;
import com.forgenz.horses.config.HorsesPermissionConfig;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TypeCommand extends ForgeCommand {
    public TypeCommand(ForgePlugin plugin) {
        super(plugin);

        registerAlias("types", true);
        registerAlias("type", false);
        registerAlias("t", false);
        registerPermission("horses.command.types");

        registerArgument(new ForgeCommandArgument("^.+$", 0, true, new StringBuilder().append(ChatColor.RED).append("[Horses] This should never be seen").toString()));

        setAllowOp(true);
        setArgumentString(String.format("[%s%s]", new Object[]{Messages.Misc_Words_Horse, Messages.Misc_Words_Type}));
        setDescription(Messages.Command_Type_Description.toString());
    }

    protected void onCommand(CommandSender sender, ForgeArgs args) {
        boolean player = sender instanceof Player;

        HorsesPermissionConfig pcfg = getPlugin().getHorsesConfig().getPermConfig((Player) (player ? sender : null));

        if (!pcfg.allowTypesCommand) {
            Messages.Misc_Command_Error_ConfigDenyPerm.sendMessage(sender, new Object[]{getMainCommand()});
            return;
        }

        if (args.getNumArgs() > 0) {
            HorseTypeConfig cfg = pcfg.getHorseTypeConfigLike(args.getArg(0));

            if (cfg == null) {
                Messages.Command_Buy_Error_InvalidHorseType.sendMessage(sender, new Object[]{args.getArg(0)});
            } else if ((player) && (!sender.hasPermission(cfg.type.getPermission()))) {
                Messages.Command_Type_Error_NoPermForHorse.sendMessage(sender);
            } else {
                sender.sendMessage(String.format((getPlugin().getEconomy() != null ? Messages.Command_Type_SingleTypeFormatEco : Messages.Command_Type_SingleTypeFormat).toString(), new Object[]{cfg.displayName, Double.valueOf(cfg.horseHp), Double.valueOf(cfg.horseMaxHp), Double.valueOf(cfg.jumpStrength), Double.valueOf(cfg.buyCost)}));
            }
            return;
        }

        StringBuilder bldr = new StringBuilder();
        int size = bldr.append(Messages.Command_Type_BeginWith).length();

        for (HorseType type : HorseType.values()) {
            if ((!player) || (sender.hasPermission(type.getPermission()))) {
                if (bldr.length() != 0) {
                    bldr.append(Messages.Command_Type_TypeSeparator);
                }

                HorseTypeConfig cfg = pcfg.getHorseTypeConfig(type);

                bldr.append(Messages.Command_Type_HorseTypePrefix).append(cfg.displayName);
            }
        }
        if (size == bldr.length())
            Messages.Command_Type_Error_NoHorsePerms.sendMessage(sender);
        else
            sender.sendMessage(bldr.toString());
    }

    public Horses getPlugin() {
        return (Horses) super.getPlugin();
    }
}