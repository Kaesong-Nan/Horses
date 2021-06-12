package com.forgenz.forgecore.v1_0.bukkit;

import com.forgenz.forgecore.v1_0.ForgeCore;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public abstract class ForgeListener
        implements ForgeCore, Listener {
    private static final HashMap<String, Set<ForgeListener>> registeredListeners = new HashMap<>();
    protected final ForgePlugin plugin;
    private String registeredKey = null;

    public ForgeListener(ForgePlugin plugin) {
        this(plugin, false);
    }

    public ForgeListener(ForgePlugin plugin, boolean register) {
        if (plugin == null) {
            throw new IllegalArgumentException("Listener requires a valid plugin reference");
        }

        this.plugin = plugin;

        if (register) {
            register();
        }
    }

    public ForgePlugin getPlugin() {
        return this.plugin;
    }

    public final void register() {
        this.plugin.registerListener(this);
    }

    public final void register(String key) {
        if (this.registeredKey != null) {
            throw new IllegalStateException("Listener is already registered");
        }

        Bukkit.getPluginManager().registerEvents(this, this.plugin);

        getListeners(key).add(this);

        this.registeredKey = key;
    }

    private static Set<ForgeListener> getListeners(String key) {

        return registeredListeners.computeIfAbsent(key, k -> new HashSet<>());
    }

    public static void unregisterAll(String key) {
        Set<ForgeListener> listeners = getListeners(key);

        for (ForgeListener listener : listeners) {
            HandlerList.unregisterAll(listener);
        }

        listeners.clear();
        registeredListeners.remove(key);
    }

    public final void unregister() {
        if (this.registeredKey == null) {
            throw new IllegalStateException("Listener is not registered");
        }

        HandlerList.unregisterAll(this);

        Set<ForgeListener> listeners = getListeners(this.registeredKey);
        listeners.remove(this);
        if (listeners.isEmpty()) {
            registeredListeners.remove(this.registeredKey);
        }

        this.registeredKey = null;
    }
}