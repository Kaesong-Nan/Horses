package com.forgenz.horses.attribute;

import com.forgenz.horses.PlayerHorse;
import com.forgenz.horses.util.HorseSpeedUtil;
import org.bukkit.entity.AbstractHorse;

/**
 * Created by john on 8/7/15.
 */
public class HorseSpeedAttribute extends Attribute implements BuffAttribute {
    private final double speedChange;

    public HorseSpeedAttribute(double speedChange) {
        this.speedChange = speedChange;
    }

    @Override
    public void onAdd(PlayerHorse playerHorse) {
        AbstractHorse horse = playerHorse.getHorse();
        HorseSpeedUtil.setHorseSpeed(horse,HorseSpeedUtil.getHorseSpeed(horse)+speedChange);
    }

    @Override
    public void onRemove(PlayerHorse playerHorse) {
        AbstractHorse horse = playerHorse.getHorse();
        HorseSpeedUtil.setHorseSpeed(horse,HorseSpeedUtil.getHorseSpeed(horse)-speedChange);
    }
}
