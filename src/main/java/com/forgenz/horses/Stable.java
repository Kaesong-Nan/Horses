package com.forgenz.horses;

import com.forgenz.forgecore.v1_0.ForgeCore;
import com.forgenz.horses.config.HorseTypeConfig;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class Stable
        implements ForgeCore, Iterable<PlayerHorse> {
    private final Horses plugin;
    private final String group;
    private final String player;
    private int id;
    private List<PlayerHorse> horses = Collections.synchronizedList(new LinkedList());
    private PlayerHorse activeHorse;
    private PlayerHorse lastActiveHorse;

    public Stable(Horses plugin, String group, String player) {
        this(plugin, group, player, -1);
    }

    public Stable(Horses plugin, String group, String player, int id) {
        this.plugin = plugin;
        this.group = group;
        this.player = player;
        this.id = id;
    }

    public Horses getPlugin() {
        return this.plugin;
    }

    public String getOwner() {
        return this.player;
    }

    public Player getPlayerOwner() {
        return Bukkit.getPlayerExact(getOwner());
    }

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getGroup() {
        return this.group;
    }

    public int getHorseCount() {
        return this.horses.size();
    }

    public void addHorse(PlayerHorse horse) {
        this.horses.add(horse);
    }

    protected void setActiveHorse(PlayerHorse horseData) {
        if (this.activeHorse != null) {
            horseData.removeHorse();
            horseData.saveChanges();
            this.lastActiveHorse = horseData;
        }

        this.activeHorse = horseData;
    }

    public PlayerHorse getLastActiveHorse() {
        return (this.lastActiveHorse != null) || (this.horses.isEmpty()) ? this.lastActiveHorse : (PlayerHorse) this.horses.get(0);
    }

    public void setLastActiveHorse(PlayerHorse horse) {
        if (!this.horses.contains(horse)) {
            return;
        }
        this.lastActiveHorse = horse;
    }

    public PlayerHorse findHorse(String name, boolean exact) {
        PlayerHorse bestMatch = null;

        synchronized (this.horses) {
            Iterator it = this.horses.iterator();

            name = name.toLowerCase();
            int length = 0;
            boolean startsWith = false;

            while (it.hasNext()) {
                PlayerHorse horse = (PlayerHorse) it.next();

                String horseName = horse.getName().toLowerCase();

                if (horseName.equals(name))
                    return horse;
                if (!exact) {
                    if (horseName.startsWith(name)) {
                        if (length < name.length()) {
                            length = name.length();
                            bestMatch = horse;
                            startsWith = true;
                        }
                    } else if ((!startsWith) && (bestMatch != null) && (horseName.contains(name)))
                        bestMatch = horse;
                }
            }
        }
        return bestMatch;
    }

    public PlayerHorse createHorse(String name, HorseTypeConfig typecfg, boolean saddle) {
        return createHorse(name, typecfg, null, saddle,null);
    }


    public PlayerHorse createHorse(String name, HorseTypeConfig typecfg, Horse horse) {
        return createHorse(name, typecfg, horse, false,null);
    }

    public PlayerHorse createHorse(String name, HorseTypeConfig typecfg, Horse horse, boolean saddle, String trailName) {
        PlayerHorse horseData = new PlayerHorse(this.plugin, this, name, typecfg.type, typecfg.horseHp, typecfg.horseMaxHp, typecfg.speed, typecfg.jumpStrength, horse);

        this.horses.add(horseData);

        if ((horse == null) && (saddle)) {
            horseData.setSaddle(new ItemStack(Material.SADDLE));
        }

        getPlugin().getHorseDatabase().saveHorse(horseData);

        return horseData;
    }

    public Iterator<PlayerHorse> iterator() {
        return this.horses.iterator();
    }

    public PlayerHorse getActiveHorse() {
        if (this.activeHorse != null) {
            if ((this.activeHorse.getHorse() == null) || (!this.activeHorse.getHorse().isValid())) {
                this.activeHorse = null;
            }
        }

        return this.activeHorse;
    }

    protected void removeActiveHorse(PlayerHorse horseData) {
        if (this.activeHorse == horseData) {
            this.activeHorse = null;
        }
    }

    public boolean deleteHorse(PlayerHorse playerHorse) {
        if (this.lastActiveHorse == playerHorse) {
            this.lastActiveHorse = null;
        }
        if (getPlugin().getHorseDatabase().deleteHorse(playerHorse)) {
            this.horses.remove(playerHorse);
            return true;
        }

        return false;
    }
}