package com.forgenz.horses.listeners;

import com.forgenz.forgecore.v1_0.bukkit.ForgeListener;
import com.forgenz.forgecore.v1_0.bukkit.ForgePlugin;
import com.forgenz.horses.Horses;
import com.forgenz.horses.PlayerHorse;
import com.forgenz.horses.Stable;
import com.voxmc.voxlib.ItemUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

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
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void interact(PlayerInteractEvent event) {
        Player p = event.getPlayer();
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || p == null) {
            return;
        }
        ItemStack itemStack = p.getItemInHand();
        if (itemStack == null || itemStack.getType() == Material.AIR) {
            return;
        }
        String horseId = null;
        try {
            horseId = ItemUtils.extractSingleVariableFromItem(itemStack,getPlugin().getHorsesConfig().getHorseIdSyntax()).get();
        }catch (Exception ignore) {return;}
        if (horseId == null) {
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
