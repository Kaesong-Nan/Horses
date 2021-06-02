package com.forgenz.horses.attribute;

import com.forgenz.horses.PlayerHorse;
import org.bukkit.entity.AbstractHorse;

/**
 * Created by john on 8/7/15.
 */
public class HorseHealthAttribute extends Attribute implements BuffAttribute {
    private final int healthAmount;

    public HorseHealthAttribute(int healthAmount) {
        this.healthAmount = healthAmount;
    }

    @Override
    public void onAdd(PlayerHorse playerHorse) {
        AbstractHorse horse = playerHorse.getHorse();
        horse.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).setBaseValue(
                horse.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue() + healthAmount);

        horse.setHealth(horse.getHealth() + healthAmount);
    }

    @Override
    public void onRemove(PlayerHorse playerHorse) {
        AbstractHorse horse = playerHorse.getHorse();
        if (horse.getHealth() - healthAmount > 1) {
            horse.setHealth(horse.getHealth() - healthAmount);
        }
        horse.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).setBaseValue(
                horse.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue() - healthAmount);
    }
}
