package com.forgenz.horses.database;

import com.forgenz.forgecore.v1_0.ForgeCore;
import com.forgenz.horses.Horses;
import com.forgenz.horses.PlayerHorse;
import com.forgenz.horses.Stable;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

public abstract class HorseDatabase
        implements ForgeCore {
    public static final String DEFAULT_GROUP = "default";
    protected static final Pattern COLOUR_CHAR_REPLACE = Pattern.compile(Character.toString('\u00A7'), Pattern.LITERAL);
    private final Horses plugin;
    private final HorseDatabaseStorageType dbType;
    private final HashMap<String, Stable> playerStables = new HashMap<>();

    public HorseDatabase(Horses plugin, HorseDatabaseStorageType dbType) {
        this.plugin = plugin;
        this.dbType = dbType;
    }

    protected abstract List<Stable> loadEverything();

    protected abstract void importStables(List<Stable> paramList);

    protected abstract Stable loadStable(String paramString1, String paramString2);

    protected abstract void loadHorses(Stable paramStable, String paramString);

    protected abstract void saveStable(Stable paramStable);

    public abstract void saveHorse(PlayerHorse paramPlayerHorse);

    public abstract boolean deleteHorse(PlayerHorse paramPlayerHorse);

    public void importHorses(HorseDatabaseStorageType type) {
        if (type == HorseDatabaseStorageType.DUMMY) {
            type = null;
        }
        if (type == null) {
            return;
        }
        getPlugin().info("Attempting import of %s database into %s database", new Object[]{type, getType()});

        HorseDatabase db = null;
        try {
            db = type.create(getPlugin(), false);
        } catch (InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
        if (db == null) {
            return;
        }
        importStables(db.loadEverything());
    }

    public Stable getPlayersStable(Player player) {
        return getPlayersStable(player, true);
    }

    public Stable getPlayersStable(Player player, boolean load) {
        Stable stable = this.playerStables.get(player.getName());
        String stableGroup = getPlugin().getHorsesConfig().getStableGroup(player.getWorld());

        if ((stable != null) && (!stableGroup.equals(stable.getGroup()))) {
            unload(stable);
            stable = null;
            this.playerStables.remove(player.getName());
        }

        if ((stable == null) && (load)) {
            stable = loadStable(player.getName(), stableGroup);
            this.playerStables.put(player.getName(), stable);
        }

        return stable;
    }

    public void saveAll() {
        for (Stable stable : this.playerStables.values().toArray(new Stable[0])) {
            unload(stable);
        }
    }

    public Horses getPlugin() {
        return this.plugin;
    }

    public HorseDatabaseStorageType getType() {
        return this.dbType;
    }

    public void unload(Stable stable) {
        getPlugin().getSummonCmd().cancelSummon(stable.getPlayerOwner());

        if (stable.getActiveHorse() != null) {
            stable.getActiveHorse().removeHorse();
        }

        saveStable(stable);
        this.playerStables.remove(stable.getOwner());
    }
}