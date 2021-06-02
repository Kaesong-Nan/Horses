package com.forgenz.horses.attribute;

import com.forgenz.horses.PlayerHorse;
import org.bukkit.entity.AbstractHorse;

/**
 * Created by john on 8/7/15.
 */
public class HorseJumpAttribute extends Attribute implements BuffAttribute {
    private final double jumpChange;

    public HorseJumpAttribute(double jumpChange) {
        this.jumpChange = jumpChange;
    }

    @Override
    public void onAdd(PlayerHorse playerHorse) {
        AbstractHorse horse = playerHorse.getHorse();
        horse.setJumpStrength(horse.getJumpStrength() + jumpChange);
    }

    @Override
    public void onRemove(PlayerHorse playerHorse) {
        AbstractHorse horse = playerHorse.getHorse();
        horse.setJumpStrength(horse.getJumpStrength() - jumpChange);
    }
}
