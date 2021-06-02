package com.forgenz.forgecore.v1_0.component;

import com.forgenz.forgecore.v1_0.bukkit.ForgePlugin;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class ForgeComponentLogger extends Logger {
    private final String name;

    protected ForgeComponentLogger(ForgePlugin plugin, Class<?> clazz, String name) {
        super(clazz.getCanonicalName(), null);
        this.name = String.format("[%s] ", name);
        setParent(plugin.getLogger());
        setLevel(Level.ALL);
    }

    public void log(LogRecord logRecord) {
        logRecord.setMessage(this.name + logRecord.getMessage());
        super.log(logRecord);
    }
}