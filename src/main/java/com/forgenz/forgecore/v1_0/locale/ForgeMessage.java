package com.forgenz.forgecore.v1_0.locale;

import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;

public class ForgeMessage {
    private final String configLocation;
    private final String defaultMessage;
    private String message;

    public ForgeMessage(String configLocation, String defaultMessage) {
        this.configLocation = configLocation;
        this.defaultMessage = defaultMessage;
    }

    public String getMessage() {
        return this.message;
    }

    protected void updateMessage(ConfigurationSection config) {
        String temp = config.getString(this.configLocation);
        if (temp == null) {
            temp = this.defaultMessage;
        }
        config.set(this.configLocation, temp);

        this.message = ChatColor.translateAlternateColorCodes('&', temp);
    }

    public String getConfigLocation() {
        return this.configLocation;
    }
}