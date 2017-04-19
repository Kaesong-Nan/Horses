package com.forgenz.horses.attribute;

import com.forgenz.horses.PlayerHorse;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.Horse;

/**
 * Created by john on 8/7/15.
 */
public class HorseHealthAttribute extends Attribute implements BuffAttribute {
    private int healthAmount;

    public HorseHealthAttribute(int healthAmount) {
        this.healthAmount = healthAmount;
    }

    @Override
    public void onAdd(PlayerHorse playerHorse) {
        AbstractHorse horse = playerHorse.getHorse();
        horse.setMaxHealth(horse.getMaxHealth() + healthAmount);
        horse.setHealth(horse.getHealth()+healthAmount);
    }

    @Override
    public void onRemove(PlayerHorse playerHorse) {
        AbstractHorse horse = playerHorse.getHorse();
        if (horse.getHealth() - healthAmount > 1) {
            horse.setHealth(horse.getHealth()- healthAmount);
        }
        horse.setMaxHealth(horse.getMaxHealth() - healthAmount);
    }
}
