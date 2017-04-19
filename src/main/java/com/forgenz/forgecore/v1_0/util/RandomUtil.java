package com.forgenz.forgecore.v1_0.util;

import java.util.Random;

public final class RandomUtil {
    private static final Random r = new Random();

    public static Random get() {
        return r;
    }
}