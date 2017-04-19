package com.forgenz.horses.command;

import com.forgenz.forgecore.v1_0.bukkit.ForgePlugin;
import com.forgenz.forgecore.v1_0.command.ForgeArgs;
import com.forgenz.forgecore.v1_0.command.ForgeCommand;
import com.forgenz.forgecore.v1_0.command.ForgeCommandArgument;
import com.forgenz.horses.Horses;
import com.forgenz.horses.Messages;
import com.forgenz.horses.PlayerHorse;
import com.forgenz.horses.Stable;
import com.forgenz.horses.pack.PackDatabase;
import com.voxmc.voxlib.ItemUtils;
import com.voxmc.voxlib.util.EssentialsItemBuilder;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.math.RoundingMode;
import java.text.DecimalFormat;

/**
 * Created by john on 8/12/15.
 */
public class PackCommand extends ForgeCommand {
    public PackCommand(ForgePlugin plugin) {
        super(plugin);

        registerAlias("pack", true);
        registerPermission("horses.command.pack");
        registerArgument(new ForgeCommandArgument(getPlugin().getHorsesConfig().forceEnglishCharacters ? "^[a-z0-9_&]+$" : "^[^ ]+$", 2, false, Messages.Misc_Command_Error_InvalidName.toString()));

        setAllowOp(true);
        setAllowConsole(false);
        setArgumentString(String.format("<%1$s%2$s>", new Object[]{Messages.Misc_Words_Horse, Messages.Misc_Words_Name}));
        setDescription(Messages.Command_Summon_Description.toString());
    }

    @Override
    protected void onCommand(CommandSender paramCommandSender, ForgeArgs paramForgeArgs) {
        final Player player = (Player) paramCommandSender;

        String horseName = paramForgeArgs.getArg(0);
        if (horseName == null) {
            return;
        }
        Stable stable = getPlugin().getHorseDatabase().getPlayersStable(player);
        if (stable == null)  {
            return;
        }
        PlayerHorse horse = stable.findHorse(paramForgeArgs.getArg(0), false);

        if (horse == null) {
            Messages.Misc_Command_Error_NoHorseNamed.sendMessage(player, new Object[]{paramForgeArgs.getArg(0)});
            return;
        }
        boolean tookItem = false;
        ItemStack baseItem = getPlugin().getHorsesConfig().getPackCost().getItemStack();
        if (baseItem != null) {
            for (ItemStack itemStack : player.getInventory()) {
                if (itemStack != null && itemStack.isSimilar(baseItem)) {
                    if (itemStack.getAmount() == 1) {
                        player.getInventory().remove(itemStack);
                    }else {
                        itemStack.setAmount(itemStack.getAmount()-1);
                    }
                    tookItem = true;
                    break;
                }
            }
        }
        if (!tookItem) {
            player.sendMessage(getPlugin().getHorsesConfig().getPackupItemNeededMessage());
            return;
        }
        ItemStack saddle = horse.getItem(0);
        ItemStack armor = horse.getItem(1);
        String saddleName = "NONE";
        String armorName = "NONE";
        if (saddle != null && saddle.getType() != Material.AIR) {
            saddleName = ItemUtils.getFriendlyName(saddle,true);
        }
        if (armor != null && armor.getType() != Material.AIR) {
            armorName = ItemUtils.getFriendlyName(armor,true);
        }
        PackDatabase packDatabase = getPlugin().getPackDatabase();
        DecimalFormat speedFormat = new DecimalFormat("#.###");
        speedFormat.setRoundingMode(RoundingMode.CEILING);
        String speed = speedFormat.format(horse.getSpeed());
        String jumpStrength = speedFormat.format(horse.getJumpStrength());
        String displayName = horse.getDisplayName();
        String breed = horse.getType().name();
        String maxHealth = ((int) (Math.round(player.getMaxHealth()))) + "";
        String health = ((int) (Math.round(player.getHealth()))) + "";
        String saddleBag = "NONE";
        if (horse.getItems().length > 2) {
            saddleBag = "[" + horse.getItems().length +"/" + "15]";
        }
        EssentialsItemBuilder essentialsItemBuilder = new EssentialsItemBuilder(getPlugin().getHorsesConfig().getHorseEggSyntax());
        essentialsItemBuilder = essentialsItemBuilder.replace("{display-name}",displayName)
                .replace("{breed}",breed).replace("{speed}",speed).replace("{jump-strength}",jumpStrength)
                .replace("{max-health}",maxHealth).replace("{saddle}",saddleName)
                .replace("{barding}",armorName).replace("{saddle-bag}",saddleBag)
                .replace("{health}", health);
        String horseId = packDatabase.savePlayerHorse(horse);
        essentialsItemBuilder = essentialsItemBuilder.replace("{horse-id}",horseId);
        ItemStack itemStack = essentialsItemBuilder.itemStack();
        if (itemStack != null) {
            player.getInventory().addItem(itemStack);
        }
    }
    public Horses getPlugin() {
        return (Horses) super.getPlugin();
    }

}
