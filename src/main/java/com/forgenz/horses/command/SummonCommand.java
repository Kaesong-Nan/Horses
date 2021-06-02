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
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.WeakHashMap;

public class SummonCommand extends ForgeCommand {
    private final WeakHashMap<Player, Long> summonTasks = new WeakHashMap<>();
    private final int pluginLoadCount;
    private final Location cacheLoc = new Location(null, 0.0D, 0.0D, 0.0D);

    public SummonCommand(ForgePlugin plugin) {
        super(plugin);

        this.pluginLoadCount = plugin.getLoadCount();

        registerAlias("summon", true);
        registerAlias("s", false);
        registerPermission("horses.command.summon");

        registerArgument(new ForgeCommandArgument(getPlugin().getHorsesConfig().forceEnglishCharacters ? "^[a-z0-9_&]+$" : "^[^ ]+$", 2, true, Messages.Misc_Command_Error_InvalidName.toString()));

        setAllowOp(true);
        setAllowConsole(false);
        setArgumentString(String.format("<%1$s%2$s>", Messages.Misc_Words_Horse, Messages.Misc_Words_Name));
        setDescription(Messages.Command_Summon_Description.toString());
    }

    protected void onCommand(CommandSender sender, ForgeArgs args) {
        final Player player = (Player) sender;

        HorsesConfig cfg = getPlugin().getHorsesConfig();
        HorsesPermissionConfig pcfg = cfg.getPermConfig(player);

        if (!pcfg.allowSummonCommand) {
            Messages.Misc_Command_Error_ConfigDenyPerm.sendMessage(sender, new Object[]{getMainCommand()});
            return;
        }

        Long lastSummon = this.summonTasks.get(player);
        if (lastSummon != null) {
            if (System.currentTimeMillis() - lastSummon > pcfg.summonDelay * 1000) {
                this.summonTasks.remove(player);
            } else {
                Messages.Command_Summon_Error_AlreadySummoning.sendMessage(player);
                return;
            }
        }

        Stable stable = getPlugin().getHorseDatabase().getPlayersStable(player);
        final PlayerHorse horse;
        if (args.getNumArgs() == 0) {
            horse = stable.getLastActiveHorse();

            if (horse == null) {
                Messages.Command_Summon_Error_NoLastActiveHorse.sendMessage(player);
                return;
            }

        } else {
            horse = stable.findHorse(args.getArg(0), false);

            if (horse == null) {
                Messages.Misc_Command_Error_NoHorseNamed.sendMessage(player, new Object[]{args.getArg(0)});
                return;
            }

        }


        long timeDiff = System.currentTimeMillis() - horse.getLastDeath();
        if (pcfg.deathCooldown > timeDiff) {
            Messages.Command_Summon_Error_OnDeathCooldown.sendMessage(player, new Object[]{horse.getDisplayName(), (pcfg.deathCooldown - timeDiff) / 1000L});
            return;
        }

        if ((cfg.worldGuardCfg != null) && (!cfg.worldGuardCfg.allowCommand(cfg.worldGuardCfg.commandSummonAllowedRegions, player.getLocation(this.cacheLoc)))) {
            Messages.Command_Summon_Error_WorldGuard_CantUseSummonHere.sendMessage(player);
            return;
        }

        int tickDelay = pcfg.summonDelay * 20;
        if (tickDelay <= 0) {
            horse.spawnHorse(player);
            Messages.Command_Summon_Success_SummonedHorse.sendMessage(player, new Object[]{horse.getDisplayName()});
        } else {
            final long startTime = System.currentTimeMillis();

            Bukkit.getScheduler().runTaskLater(getPlugin(), () -> {
                Long storedStartTime = SummonCommand.this.summonTasks.get(player);

                if ((storedStartTime == null) || (storedStartTime != startTime)) {
                    return;
                }

                if (SummonCommand.this.pluginLoadCount != SummonCommand.this.getPlugin().getLoadCount()) {
                    return;
                }

                SummonCommand.this.summonTasks.remove(player);

                if (player.isValid()) {
                    horse.spawnHorse(player);
                    Messages.Command_Summon_Success_SummonedHorse.sendMessage(player, new Object[]{horse.getDisplayName()});
                }
            }, tickDelay);
            this.summonTasks.put(player, startTime);
            Messages.Command_Summon_Success_SummoningHorse.sendMessage(player, new Object[]{horse.getDisplayName(), pcfg.summonDelay});
        }
    }

    public void cancelSummon(Player player) {
        if ((player != null) && (this.summonTasks.remove(player) != null))
            Messages.Command_Summon_Error_MovedWhileSummoning.sendMessage(player);
    }

    public boolean isSummoning(Player player) {
        return (player != null) && (this.summonTasks.containsKey(player));
    }

    public Horses getPlugin() {
        return (Horses) super.getPlugin();
    }
}