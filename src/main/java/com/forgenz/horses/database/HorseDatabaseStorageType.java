package com.forgenz.horses.database;

import com.forgenz.horses.Horses;

import java.lang.reflect.InvocationTargetException;

public enum HorseDatabaseStorageType {
    DUMMY(DummyDatabase.class),

    YAML(YamlDatabase.class),

    MYSQL(MysqlDatabase.class);

    private final Class<? extends HorseDatabase> clazz;

    private HorseDatabaseStorageType(Class<? extends HorseDatabase> clazz) {
        this.clazz = clazz;
    }

    public HorseDatabase create(Horses plugin, boolean fallback) throws InvocationTargetException, NoSuchMethodException {
        try {
            return (HorseDatabase) this.clazz.getConstructor(new Class[]{Horses.class}).newInstance(new Object[]{plugin});
        } catch (NoSuchMethodException e) {
            plugin.severe("Failed to find constructor for the %s database type", e, new Object[]{toString()});
            throw e;
        } catch (InvocationTargetException e) {
            plugin.severe("Error occured when attempting to create the database of type %s", e.getTargetException(), new Object[]{toString()});
            throw e;
        } catch (Throwable e) {
            if (fallback) {
                plugin.severe("#################################");
                plugin.severe("Falling back to a dummy database");
                plugin.severe("WARNING: No data will be saved");
                plugin.severe("#################################");
            }
            if (fallback) return DUMMY.create(plugin, false);
        }
        return null;
    }

    public static HorseDatabaseStorageType getFromString(String str) {
        for (HorseDatabaseStorageType type : values()) {
            if (type.toString().equals(str)) {
                return type;
            }
        }
        return null;
    }
}