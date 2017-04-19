package com.forgenz.horses.listeners;

import com.forgenz.forgecore.v1_0.bukkit.ForgeListener;
import com.forgenz.horses.*;
import com.forgenz.horses.config.HorseTypeConfig;
import com.forgenz.horses.config.HorsesPermissionConfig;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.HorseInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

public class InteractListener extends ForgeListener {
    public InteractListener(Horses plugin) {
        super(plugin);

        register();
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEntityEvent event) {
        if (event.getRightClicked().getType() != EntityType.HORSE) {
            return;
        }

        Horse horse = (Horse) event.getRightClicked();

        PlayerHorse horseData = PlayerHorse.getFromEntity(horse);

        if (horseData != null) {
            handleOwnedHorse(event, horseData, event.getPlayer());
        } else {
            handleUnownedHorse(event, horse, event.getPlayer());
        }
    }
    @EventHandler(ignoreCancelled = true)
    public void onInventory(InventoryClickEvent event) {
        if (event.getView() != null && event.getView().getTopInventory() != null && event.getView().getTopInventory() instanceof HorseInventory) {
            HorseInventory horseInventory = (HorseInventory) event.getView().getTopInventory();
            if (horseInventory.getHolder() instanceof Horse) {
                Horse horse = (Horse) horseInventory.getHolder();
                final PlayerHorse playerHorse = PlayerHorse.getFromEntity(horse);
                new BukkitRunnable(){
                    @Override
                    public void run() {
                        if (playerHorse != null) {
                            playerHorse.updateAttributes();
                        }
                    }
                }.runTaskLater(plugin,1);
            }
        }
    }

    private void handleOwnedHorse(PlayerInteractEntityEvent event, final PlayerHorse horseData, Player player) {
        if (!event.getPlayer().getName().equals(horseData.getStable().getOwner())) {
            Messages.Event_Interact_Error_CantInteractWithThisHorse.sendMessage(event.getPlayer(), new Object[]{horseData.getStable().getOwner()});
            event.setCancelled(true);
            return;
        }

        HorsesPermissionConfig cfg = getPlugin().getHorsesConfig().getPermConfig(event.getPlayer());
        Material matInHand = null;
        if (event.getPlayer().getItemInHand() != null) {
            matInHand = event.getPlayer().getItemInHand().getType();
        }
        if (matInHand == Material.SADDLE || (matInHand != null && matInHand.toString().contains("BARD"))) {
            new BukkitRunnable(){
                @Override
                public void run() {
                    if (horseData != null) {
                        horseData.updateAttributes();
                    }
                }
            }.runTaskLater(plugin,1);
        }
        if (event.getPlayer().getItemInHand().getType() == Material.NAME_TAG) {
            if (cfg.allowRenameFromNameTag && horseData.isRenamable()) {
                ItemMeta meta = event.getPlayer().getItemInHand().getItemMeta();
                String name = meta.getDisplayName();

                if (name == null) {
                    Messages.Event_Interact_Error_RenameWithTagMustSetAName.sendMessage(player);
                    event.setCancelled(true);
                } else if (getPlugin().getHorsesConfig().getBlackListNames().contains(name)) {
                    player.sendMessage(ChatColor.RED + "You can't rename a horse to that!");
                }else if (getPlugin().getHorsesConfig().rejectedHorseNamePattern.matcher(name).find()) {
                    Messages.Misc_Command_Error_IllegalHorseNamePattern.sendMessage(event.getPlayer());
                    event.setCancelled(true);
                } else {
                    String oldName = horseData.getDisplayName();
                    horseData.rename(name);
                    Messages.Command_Rename_Success_Renamed.sendMessage(event.getPlayer(), new Object[]{oldName, horseData.getDisplayName()});
                }
                return;
            }

            Messages.Event_Interact_Error_CantRenameWithTag.sendMessage(event.getPlayer());

            event.setCancelled(true);
        }
    }

    private void handleUnownedHorse(PlayerInteractEntityEvent event, Horse horse, Player player) {
        HorsesPermissionConfig cfg = getPlugin().getHorsesConfig().getPermConfig(event.getPlayer());

        if (event.getPlayer().getItemInHand().getType() == Material.NAME_TAG) {
            if (cfg.allowClaimingWithNameTag) {
                ItemMeta meta = event.getPlayer().getItemInHand().getItemMeta();
                String displayName;
                String name = displayName = meta.getDisplayName();

                event.setCancelled(true);

                if (name == null) {
                    Messages.Event_Interact_Error_ClaimWithTagMustSetAName.sendMessage(player);
                } else {
                    if (name.length() > cfg.maxHorseNameLength) {
                        Messages.Misc_Command_Error_HorseNameTooLong.sendMessage(player, new Object[]{Integer.valueOf(cfg.maxHorseNameLength)});
                        return;
                    }

                    Stable stable = getPlugin().getHorseDatabase().getPlayersStable(player);

                    if (stable.getHorseCount() >= cfg.maxHorses) {
                        Messages.Command_Buy_Error_TooManyHorses.sendMessage(player, new Object[]{Integer.valueOf(cfg.maxHorses)});
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
                    if (getPlugin().getHorsesConfig().getBlackListNames().contains(name)) {
                        player.sendMessage(ChatColor.RED + "You can't rename a horse to that!");
                        return;
                    }
                    if (stable.findHorse(name, true) != null) {
                        Messages.Command_Buy_Error_AlreadyHaveAHorseWithThatName.sendMessage(player, new Object[]{name});
                        return;
                    }

                    if (getPlugin().getHorsesConfig().rejectedHorseNamePattern.matcher(name).find()) {
                        Messages.Misc_Command_Error_IllegalHorseNamePattern.sendMessage(event.getPlayer());
                        return;
                    }

                    HorseType type = HorseType.valueOf(horse);
                    HorseTypeConfig typecfg = cfg.getHorseTypeConfig(type);
                    if (!player.hasPermission(type.getTamePermission())) {
                        Messages.NO_TAME_PERMISSION.sendMessage(player);
                        return;
                    }
                    if ((getPlugin().getEconomy() != null) && (typecfg.wildClaimCost > 0.0D)) {
                        EconomyResponse responce = getPlugin().getEconomy().withdrawPlayer(player.getName(), typecfg.wildClaimCost);

                        if (!responce.transactionSuccess()) {
                            Messages.Command_Buy_Error_CantAffordHorse.sendMessage(player, new Object[]{Double.valueOf(typecfg.wildClaimCost)});
                            return;
                        }

                        Messages.Command_Buy_Success_BoughtHorse.sendMessage(player, new Object[]{Double.valueOf(typecfg.wildClaimCost)});
                    }

                    PlayerHorse horseData = stable.createHorse(displayName, typecfg, horse);
                    horseData.adjustValuesToMax();
                    Messages.Command_Buy_Success_Completion.sendMessage(player, new Object[]{"horses", horseData.getDisplayName()});
                }
            } else if (cfg.blockRenamingOnWildHorses) {
                Messages.Event_Interact_Error_RenamingNaturalHorsesBlocked.sendMessage(player);
                event.setCancelled(true);
            }
            return;
        }
    }

    public Horses getPlugin() {
        return (Horses) super.getPlugin();
    }
}