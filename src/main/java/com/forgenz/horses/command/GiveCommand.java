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
import com.forgenz.horses.config.HorsesConfig;
import com.forgenz.horses.config.HorsesPermissionConfig;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class GiveCommand extends ForgeCommand {
    public GiveCommand(ForgePlugin plugin) {
        super(plugin);

        registerAlias("give", true);
        registerPermission("horses.command.give");

        registerArgument(new ForgeCommandArgument("^[a-z0-9_&]+$", 2, false, Messages.Command_List_Error_InvalidCharactersPlayerName.toString()));
        registerArgument(new ForgeCommandArgument(getPlugin().getHorsesConfig().forceEnglishCharacters ? "^[a-z0-9_&]+$" : "^[^ ]+$", 2, false, Messages.Misc_Command_Error_NameValidCharacters.toString()));
        registerArgument(new ForgeCommandArgument(getPlugin().getHorsesConfig().forceEnglishCharacters ? "^[a-z0-9_&]+$" : "^[^ ]+$", 2, false, Messages.Command_Buy_Error_Type.toString()));
        for (int i = 0; i < 10; i++) {
            registerArgument(new ForgeCommandArgument(".*?",true,""));
        }
        setAllowOp(true);
        setAllowConsole(true);
        setArgumentString(String.format("<%1$s%3$s> <%2$s%3$s> <%2$s%4$s>", new Object[]{Messages.Misc_Words_Player, Messages.Misc_Words_Horse, Messages.Misc_Words_Name, Messages.Misc_Words_Type}));
        setDescription(Messages.Command_Give_Description.toString());
    }
    private ItemStack getBarding(String name) {
        name = name.toLowerCase();
        if (getPlugin().getHorsesConfig().getBardings().containsKey(name)) {
            return getPlugin().getHorsesConfig().getBardings().get(name).getItemStack();
        }
        if (name.equalsIgnoreCase("diamond")) {
            return new ItemStack(Material.DIAMOND_BARDING);
        }else if (name.equalsIgnoreCase("gold")) {
            return new ItemStack(Material.GOLD_BARDING);
        }else if (name.equalsIgnoreCase("iron")) {
            return new ItemStack(Material.IRON_BARDING);
        }else {
            return null;
        }
    }
    private ItemStack getSaddle(String name) {
        name = name.toLowerCase();
        if (getPlugin().getHorsesConfig().getSaddles().containsKey(name)) {
            return getPlugin().getHorsesConfig().getSaddles().get(name).getItemStack();
        }
        return null;
    }



    protected void onCommand(CommandSender sender, ForgeArgs args) {
        Player player = Bukkit.getPlayerExact(args.getArg(0));

        if ((player == null) || (!player.isOnline())) {
            Messages.Command_List_Error_InvalidCharactersPlayerName.sendMessage(sender);
            return;
        }

        HorsesConfig cfg = getPlugin().getHorsesConfig();
        HorsesPermissionConfig pcfg = cfg.getPermConfig(player);

        HorseTypeConfig typecfg = pcfg.getHorseTypeConfigLike(args.getArg(2));

        if (typecfg == null) {
            Messages.Command_Buy_Error_InvalidHorseType.sendMessage(sender, new Object[]{args.getArg(2)});
            return;
        }
        ItemStack barding = null;
        String trailName = null;
        ItemStack saddle = null;
        double maxHealth = -1;
        double health = -1;
        double speed = -1;
        double jumpstrength = -1;
        boolean renameable = true;

        String displayName = ChatColor.translateAlternateColorCodes('&', args.getArg(1).replace("_", " "));
        if (args.getNumArgs() > 2) {
            for (int i = 2; i < args.getNumArgs(); i++) {
                String arg = args.getArg(i);
                if (arg.contains(":")) {
                    String[] split = arg.split(":");
                    if (split.length > 1) {
                        String modifier = split[0];
                        String value = split[1];
                        if (modifier.equalsIgnoreCase("armor")) {
                            barding = getBarding(value);
                        }else if (modifier.equalsIgnoreCase("trail")) {
                            trailName = value;
                        }else if (modifier.equalsIgnoreCase("maxhealth")) {
                            maxHealth = Double.parseDouble(value);
                        }else if (modifier.equalsIgnoreCase("health")) {
                            health = Double.parseDouble(value);
                        }else if (modifier.equalsIgnoreCase("speed")) {
                            speed = Double.parseDouble(value);
                        }else if (modifier.equalsIgnoreCase("jumpstrength")) {
                            jumpstrength = Double.parseDouble(value);
                        }else if (modifier.equalsIgnoreCase("renameable")) {
                            renameable = Boolean.parseBoolean(value);
                        }else if (modifier.equalsIgnoreCase("saddle")) {
                            saddle = getSaddle(value);
                        }
                    }
                }
            }
        }
        String name = ChatColor.stripColor(displayName);
        if (name.length() > pcfg.maxHorseNameLength) {
            Messages.Misc_Command_Error_HorseNameTooLong.sendMessage(sender, new Object[]{Integer.valueOf(pcfg.maxHorseNameLength)});
            return;
        }

        if (name.length() == 0) {
            Messages.Misc_Command_Error_HorseNameEmpty.sendMessage(sender);
            return;
        }

        Stable stable = getPlugin().getHorseDatabase().getPlayersStable(player);
        if (stable.findHorse(name, true) != null) {
            stable.deleteHorse(stable.findHorse(name,true));
        }


        PlayerHorse horse = stable.createHorse(displayName, typecfg,null, pcfg.startWithSaddle,trailName);
        if (barding != null) {
            horse.setArmour(barding);
        }
        if (maxHealth != -1) {
            horse.setMaxHealth(maxHealth);
        }
        if (health != -1) {
            if (horse.getMaxHealth() < health) {
                horse.setMaxHealth(health);
            }
            horse.setHealth(health);
        }
        if (speed != -1) {
            horse.setSpeed(speed);
        }
        if (jumpstrength != -1) {
            horse.setJumpStrength(jumpstrength);
        }
        if (saddle != null) {
            horse.setSaddle(saddle);
        }
        horse.setRenamable(renameable);

        getPlugin().getHorseDatabase().saveHorse(horse);
        Messages.Command_Give_Success_Completion.sendMessage(sender, new Object[]{player.getName(), horse.getName()});
        Messages.Command_Give_Success_Completion_Player.sendMessage(player, new Object[]{args.getCommandUsed(), horse.getName()});
    }

    public Horses getPlugin() {
        return (Horses) super.getPlugin();
    }
}