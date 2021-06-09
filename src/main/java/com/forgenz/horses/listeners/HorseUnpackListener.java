package com.forgenz.horses.listeners;

import com.forgenz.forgecore.v1_0.bukkit.ForgeListener;
import com.forgenz.forgecore.v1_0.bukkit.ForgePlugin;
import com.forgenz.horses.Horses;
import com.forgenz.horses.PlayerHorse;
import com.forgenz.horses.Stable;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

/**
 * Created by john on 8/12/15.
 */
public class HorseUnpackListener extends ForgeListener {
    public HorseUnpackListener(ForgePlugin plugin) {
        super(plugin);
        register();
    }

    public Horses getPlugin() {
        return (Horses) super.getPlugin();
    }

    public static Optional<String> extractSingleVariableFromItem(ItemStack itemStack, String identifier) {
        return itemStack != null && identifier != null && !identifier.isEmpty() && itemStack.getItemMeta().hasLore() ? Optional.of(itemStack.getItemMeta().getLore().stream().filter((loreI) -> loreI.contains(identifier)).map((loreI) -> ChatColor.stripColor(loreI.replace(identifier, "")).trim()).findFirst().orElse(null)) : Optional.empty();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void interact(PlayerInteractEvent event) {
        Player p = event.getPlayer();
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        ItemStack itemStack = p.getInventory().getItemInMainHand();
        if (itemStack.getType() == Material.AIR) {
            return;
        }
        String horseId;
        try {
            horseId = extractSingleVariableFromItem(itemStack, getPlugin().getHorsesConfig().getHorseIdSyntax()).get();
        } catch (Exception exception) {
            exception.printStackTrace();
            return;
        }

        event.setCancelled(true);
        Stable stable = getPlugin().getHorseDatabase().getPlayersStable(p);
        PlayerHorse playerHorse = getPlugin().getPackDatabase().loadHorse(horseId,stable);
        if (playerHorse != null) {
            if (playerHorse.getHorse() != null) {
                playerHorse.removeHorse();
            }
            if (p.getItemInHand().getAmount() > 1) {
                p.getItemInHand().setAmount(p.getItemInHand().getAmount()-1);
            }else {
                p.setItemInHand(new ItemStack(Material.AIR));
            }
            p.updateInventory();
            playerHorse.spawnHorse(p);
        }
    }
}
