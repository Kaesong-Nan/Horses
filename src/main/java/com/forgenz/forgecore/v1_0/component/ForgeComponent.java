package com.forgenz.forgecore.v1_0.component;

import com.forgenz.forgecore.v1_0.ForgeCoreEntity;
import com.forgenz.forgecore.v1_0.bukkit.ForgeListener;
import com.forgenz.forgecore.v1_0.bukkit.ForgePlugin;
import com.forgenz.forgecore.v1_0.util.RandomUtil;

import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class ForgeComponent
        implements ForgeCoreEntity {
    private final ForgePlugin plugin;
    private final String name;
    private final ForgeComponentLogger logger;
    private final String listenerKey;
    private boolean isEnabled = false;

    protected ForgeComponent(ForgePlugin plugin, String name) {
        this.plugin = plugin;
        this.name = name;

        this.logger = new ForgeComponentLogger(plugin, getClass(), name);

        byte[] chars = new byte[8];
        RandomUtil.get().nextBytes(chars);

        this.listenerKey = String.format("%s-%s", new Object[]{new String(chars), name});

        onLoad();
    }

    public abstract boolean onLoad();

    public abstract boolean onEnable();

    public abstract boolean onDisable();

    protected final boolean setEnabled(boolean enabled) {
        if (this.isEnabled != enabled) {
            this.isEnabled = enabled;
            try {
                if (enabled) {
                    if (!onEnable()) {
                        this.isEnabled = false;
                    }

                } else if (!onDisable()) {
                    this.isEnabled = true;
                }

            } catch (Throwable e) {
                log(Level.SEVERE, "Error occured when %s component", e, new Object[]{enabled ? "enabling" : "disabling"});
                this.isEnabled = (!enabled);
            }
        }

        return this.isEnabled == enabled;
    }

    public final ForgePlugin getPlugin() {
        return this.plugin;
    }

    public final void registerListener(ForgeListener listener) {
        listener.register(this.name);
    }

    public final void unregisterListeners() {
        ForgeListener.unregisterAll(this.listenerKey);
    }

    public final Logger getLogger() {
        return this.logger;
    }

    public void log(Level level, String message) {
        getLogger().log(level, message);
    }

    public void log(Level level, String message, Object[] args) {
        log(level, String.format(message, args));
    }

    public void log(Level level, String message, Throwable e) {
        getLogger().log(level, message, e);
    }

    public void log(Level level, String message, Throwable e, Object[] args) {
        log(level, String.format(message, args), e);
    }

    public void info(String message) {
        log(Level.INFO, message);
    }

    public void info(String message, Object[] args) {
        log(Level.INFO, message, args);
    }

    public void warning(String message) {
        log(Level.WARNING, message);
    }

    public void warning(String message, Object[] args) {
        log(Level.WARNING, message, args);
    }

    public void severe(String message) {
        log(Level.SEVERE, message);
    }

    public void severe(String message, Object[] args) {
        log(Level.SEVERE, message, args);
    }
}