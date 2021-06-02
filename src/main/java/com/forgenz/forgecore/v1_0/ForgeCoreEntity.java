package com.forgenz.forgecore.v1_0;

import com.forgenz.forgecore.v1_0.bukkit.ForgeListener;

import java.util.logging.Level;
import java.util.logging.Logger;

public interface ForgeCoreEntity extends ForgeCore {
    void registerListener(ForgeListener paramForgeListener);

    void unregisterListeners();

    String getAuthors();

    Logger getLogger();

    void log(Level paramLevel, String paramString);

    void log(Level paramLevel, String paramString, Object[] paramArrayOfObject);

    void log(Level paramLevel, String paramString, Throwable paramThrowable);

    void log(Level paramLevel, String paramString, Throwable paramThrowable, Object[] paramArrayOfObject);

    void info(String paramString);

    void info(String paramString, Object[] paramArrayOfObject);

    void warning(String paramString);

    void warning(String paramString, Object[] paramArrayOfObject);

    void severe(String paramString);

    void severe(String paramString, Object[] paramArrayOfObject);
}