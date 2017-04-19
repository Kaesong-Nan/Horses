package com.forgenz.forgecore.v1_0;

import com.forgenz.forgecore.v1_0.bukkit.ForgeListener;

import java.util.logging.Level;
import java.util.logging.Logger;

public abstract interface ForgeCoreEntity extends ForgeCore {
    public abstract void registerListener(ForgeListener paramForgeListener);

    public abstract void unregisterListeners();

    public abstract String getAuthors();

    public abstract Logger getLogger();

    public abstract void log(Level paramLevel, String paramString);

    public abstract void log(Level paramLevel, String paramString, Object[] paramArrayOfObject);

    public abstract void log(Level paramLevel, String paramString, Throwable paramThrowable);

    public abstract void log(Level paramLevel, String paramString, Throwable paramThrowable, Object[] paramArrayOfObject);

    public abstract void info(String paramString);

    public abstract void info(String paramString, Object[] paramArrayOfObject);

    public abstract void warning(String paramString);

    public abstract void warning(String paramString, Object[] paramArrayOfObject);

    public abstract void severe(String paramString);

    public abstract void severe(String paramString, Object[] paramArrayOfObject);
}