package com.forgenz.horses.attribute;

import com.forgenz.horses.PlayerHorse;

/**
 * Created by john on 8/7/15.
 */
public interface BuffAttribute {
    void onAdd(PlayerHorse playerHorse);

    void onRemove(PlayerHorse playerHorse);
}
