package com.forgenz.horses;

import com.forgenz.forgecore.v1_0.ForgeCore;
import com.forgenz.horses.config.HorseTypeConfig;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

@SuppressWarnings({"unused", "NullableProblems"})
public class Stable
        implements ForgeCore, Iterable<PlayerHorse> {
    private final Horses plugin;
    private final String group;
    private final String player;
    private final List<PlayerHorse> horses = Collections.synchronizedList(new LinkedList<PlayerHorse>());
    private int id;
    private PlayerHorse activeHorse;
    private PlayerHorse lastActiveHorse;
    
    public Stable(final Horses plugin, final String group, final String player) {
        this(plugin, group, player, -1);
    }
    
    public Stable(final Horses plugin, final String group, final String player, final int id) {
        this.plugin = plugin;
        this.group = group;
        this.player = player;
        this.id = id;
    }
    
    public Horses getPlugin() {
        return plugin;
    }
    
    public String getOwner() {
        return player;
    }
    
    public Player getPlayerOwner() {
        return Bukkit.getPlayerExact(getOwner());
    }
    
    public int getId() {
        return id;
    }
    
    public void setId(final int id) {
        this.id = id;
    }
    
    public String getGroup() {
        return group;
    }
    
    public int getHorseCount() {
        return horses.size();
    }
    
    public void addHorse(final PlayerHorse horse) {
        horses.add(horse);
    }
    
    public PlayerHorse getLastActiveHorse() {
        return lastActiveHorse != null || horses.isEmpty() ? lastActiveHorse : horses.get(0);
    }
    
    public void setLastActiveHorse(final PlayerHorse horse) {
        if(!horses.contains(horse)) {
            return;
        }
        lastActiveHorse = horse;
    }
    
    public PlayerHorse findHorse(String name, final boolean exact) {
        PlayerHorse bestMatch = null;
        
        synchronized(horses) {
            final Iterator it = horses.iterator();
            
            name = name.toLowerCase();
            int length = 0;
            boolean startsWith = false;
            
            while(it.hasNext()) {
                final PlayerHorse horse = (PlayerHorse) it.next();
                
                final String horseName = horse.getName().toLowerCase();
                
                if(horseName.equals(name)) {
                    return horse;
                }
                if(!exact) {
                    if(horseName.startsWith(name)) {
                        if(length < name.length()) {
                            length = name.length();
                            bestMatch = horse;
                            startsWith = true;
                        }
                    } else //noinspection ConstantConditions
                        if(!startsWith && bestMatch != null && horseName.contains(name)) {
                            bestMatch = horse;
                        }
                }
            }
        }
        return bestMatch;
    }
    
    public PlayerHorse createHorse(final String name, final HorseTypeConfig typecfg, final boolean saddle) {
        return createHorse(name, typecfg, null, saddle, null);
    }
    
    public PlayerHorse createHorse(final String name, final HorseTypeConfig typecfg, final AbstractHorse horse) {
        return createHorse(name, typecfg, horse, false, null);
    }
    
    public PlayerHorse createHorse(final String name, final HorseTypeConfig typecfg, final AbstractHorse horse, final boolean saddle, final String trailName) {
        final PlayerHorse horseData = new PlayerHorse(plugin, this, name, typecfg.type, typecfg.horseHp, typecfg.horseMaxHp, typecfg.speed, typecfg.jumpStrength, horse);
        
        horses.add(horseData);
        
        if(horse == null && saddle) {
            horseData.setSaddle(new ItemStack(Material.SADDLE));
        }
        
        getPlugin().getHorseDatabase().saveHorse(horseData);
        
        return horseData;
    }
    
    public Iterator<PlayerHorse> iterator() {
        return horses.iterator();
    }
    
    public PlayerHorse getActiveHorse() {
        if(activeHorse != null) {
            if(activeHorse.getHorse() == null || !activeHorse.getHorse().isValid()) {
                activeHorse = null;
            }
        }
        
        return activeHorse;
    }
    
    void setActiveHorse(final PlayerHorse horseData) {
        if(activeHorse != null) {
            horseData.removeHorse();
            horseData.saveChanges();
            lastActiveHorse = horseData;
        }
        
        activeHorse = horseData;
    }
    
    void removeActiveHorse(final PlayerHorse horseData) {
        if(activeHorse == horseData) {
            activeHorse = null;
        }
    }
    
    public boolean deleteHorse(final PlayerHorse playerHorse) {
        if(lastActiveHorse == playerHorse) {
            lastActiveHorse = null;
        }
        if(getPlugin().getHorseDatabase().deleteHorse(playerHorse)) {
            horses.remove(playerHorse);
            return true;
        }
        
        return false;
    }
}