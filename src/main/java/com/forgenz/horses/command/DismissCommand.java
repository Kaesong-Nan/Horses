package com.forgenz.horses.command;

import com.forgenz.forgecore.v1_0.command.ForgeArgs;
import com.forgenz.forgecore.v1_0.command.ForgeCommand;
import com.forgenz.horses.Horses;
import com.forgenz.horses.Messages;
import com.forgenz.horses.PlayerHorse;
import com.forgenz.horses.Stable;
import com.forgenz.horses.config.HorsesConfig;
import com.forgenz.horses.config.HorsesPermissionConfig;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DismissCommand extends ForgeCommand {
    private final Location cacheLoc = new Location(null, 0.0D, 0.0D, 0.0D);

    public DismissCommand(Horses plugin) {
        super(plugin);

        registerAlias("dismiss", true);
        registerAlias("d", false);
        registerPermission("horses.command.dismiss");

        setAllowOp(true);
        setAllowConsole(false);
        setDescription(Messages.Command_Dismiss_Description.toString());
    }

    protected void onCommand(CommandSender sender, ForgeArgs args) {
        Player player = (Player) sender;

        HorsesConfig cfg = getPlugin().getHorsesConfig();
        HorsesPermissionConfig pcfg = cfg.getPermConfig(player);

        if (!pcfg.allowDismissCommand) {
            Messages.Misc_Command_Error_ConfigDenyPerm.sendMessage(sender, new Object[]{getMainCommand()});
            return;
        }

        Stable stable = getPlugin().getHorseDatabase().getPlayersStable(player);

        PlayerHorse horse = stable.getActiveHorse();

        if (horse == null) {
            Messages.Command_Dismiss_Error_NoActiveHorses.sendMessage(player);
            return;
        }

        if ((cfg.worldGuardCfg != null) && (!cfg.worldGuardCfg.allowCommand(cfg.worldGuardCfg.commandDismissAllowedRegions, player.getLocation(this.cacheLoc)))) {
            Messages.Command_Dismiss_Error_WorldGuard_CantUseDismissHere.sendMessage(player);
            return;
        }
        stable.getActiveHorse().removeHorse();

        Messages.Command_Dismiss_Success_DismissedHorse.sendMessage(player, new Object[]{horse.getDisplayName()});
    }

    public Horses getPlugin() {
        return (Horses) super.getPlugin();
    }
}