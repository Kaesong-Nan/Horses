package com.forgenz.forgecore.v1_0.bukkit;

import com.forgenz.forgecore.v1_0.ForgeCoreEntity;
import com.forgenz.forgecore.v1_0.util.RandomUtil;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;

public abstract class ForgePlugin extends JavaPlugin
        implements ForgeCoreEntity {
    private int loadCount = 0;
    private final String listenerKey;
    private WorldGuardPlugin worldGuard;
    private Economy econ;

    protected ForgePlugin() {
        byte[] chars = new byte[16];
        RandomUtil.get().nextBytes(chars);
        this.listenerKey = new String(chars);
    }

    public abstract void onLoad();

    public void onEnable() {
        this.loadCount += 1;
    }

    public abstract void onDisable();

    protected boolean setupWorldGuard(boolean setup) {
        if (!setup) {
            this.worldGuard = null;
            return false;
        }

        Plugin plugin = getServer().getPluginManager().getPlugin("WorldGuard");

        if ((plugin == null) || (!(plugin instanceof WorldGuardPlugin))) {
            this.worldGuard = null;
            return false;
        }

        this.worldGuard = ((WorldGuardPlugin) plugin);
        return true;
    }

    public WorldGuardPlugin getWorldGuard() {
        return this.worldGuard;
    }

    protected boolean setupEconomy() {
        try {
            RegisteredServiceProvider economyProvider = getServer().getServicesManager().getRegistration(Economy.class);

            if (economyProvider != null) {
                this.econ = ((Economy) economyProvider.getProvider());
            }

            return this.econ != null;
        } catch (Throwable ignored) {
        }
        return false;
    }

    public Economy getEconomy() {
        return this.econ;
    }

    public void getResourseString(String resourse, StringBuilder resourseStr) {
        if (resourse == null) {
            getPlugin().severe("Resourse file name was not given", new NullPointerException(), new Object[0]);
            return;
        }

        InputStream resourseStream = getPlugin().getResource(resourse);

        if (resourseStream == null) {
            getPlugin().severe("The resourse '%s' was not found inside the %s's jar", new Object[]{resourse, getPlugin().getName()});
            return;
        }

        try {
            copyStream(resourseStream, resourseStr);
        } catch (IOException e) {
            getPlugin().severe("Error when trying to read resourse '%s'", e, new Object[]{resourse});
        }
    }

    public String getResourseString(String resourse) {
        if (resourse == null) {
            getPlugin().severe("Resourse file name was not given", new NullPointerException(), new Object[0]);
            return "";
        }

        InputStream headerStream = getPlugin().getResource(resourse);

        if (headerStream == null) {
            getPlugin().severe("The resourse '%s' was not found inside the %s's jar", new Object[]{resourse, getPlugin().getName()});
            return "";
        }

        StringBuilder resourseStr = new StringBuilder();
        try {
            copyStream(headerStream, resourseStr);
        } catch (IOException e) {
            getPlugin().severe("Error when trying to read resourse '%s'", e, new Object[]{resourse});
        }

        return resourseStr.toString();
    }

    public void copyStream(InputStream stream, StringBuilder copyTo) throws IOException {
        byte[] bytes = new byte[64];
        int size = 0;

        while ((size = stream.read(bytes)) != -1) {
            copyTo.ensureCapacity(copyTo.length() + size);

            for (int i = 0; i < size; i++) {
                copyTo.append((char) bytes[i]);
            }
        }
    }

    public ForgePlugin getPlugin() {
        return this;
    }

    public int getLoadCount() {
        return this.loadCount;
    }

    public void registerListener(ForgeListener listener) {
        listener.register(this.listenerKey);
    }

    public void unregisterListeners() {
        ForgeListener.unregisterAll(this.listenerKey);
    }

    public String getAuthors() {
        StringBuilder b = new StringBuilder();

        for (String author : getDescription().getAuthors()) {
            if (b.length() != 0) {
                b.append(", ");
            }

            b.append(author);
        }

        return b.toString();
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

    public void severe(String message, Throwable e, Object[] args) {
        log(Level.SEVERE, message, e, args);
    }
}