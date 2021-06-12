package com.forgenz.horses.util;

import org.bukkit.attribute.Attribute;
import org.bukkit.entity.AbstractHorse;

import java.lang.reflect.Method;

public class HorseSpeedUtil {
    private static final int version = 1;

    private static final int spam = 0;
    private static Object speedAttribute;
    private static Method getHandle;
    private static Method getAttributeInstance;
    private static Method setValue;
    private static Method getValue;

    public static double getHorseSpeed(AbstractHorse horse) {
        return horse.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).getValue();
    }

    public static void setHorseSpeed(AbstractHorse horse, double speed) {
        horse.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(speed);
    }
}