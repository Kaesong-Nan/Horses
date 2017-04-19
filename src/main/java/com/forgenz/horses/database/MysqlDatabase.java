package com.forgenz.horses.database;

import com.forgenz.horses.HorseType;
import com.forgenz.horses.Horses;
import com.forgenz.horses.PlayerHorse;
import com.forgenz.horses.Stable;
import com.forgenz.horses.config.AbstractConfig;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.sql.*;
import java.util.*;
import java.util.logging.Level;

public class MysqlDatabase extends HorseDatabase {
    private final YamlConfiguration cacheCfg = new YamlConfiguration();
    private final ArrayList<?> cacheItemList = new ArrayList();
    private final MysqlSettings settings;
    private Connection conn;
    private long lastCheck;
    private int spam;

    public MysqlDatabase(Horses plugin)
            throws DatabaseConnectException, SQLException {
        super(plugin, HorseDatabaseStorageType.MYSQL);

        this.settings = new MysqlSettings(plugin);

        if (!connect()) {
            throw new DatabaseConnectException("Failed to connect to MySQL database");
        }

        try {
            createTables(new String[]{"Horses", "Stables"});

            checkColumn("Stables", "user", "VARCHAR(16) NOT NULL");
            checkColumn("Stables", "lastactive", "VARCHAR(30) NOT NULL");
            addUniqueIndex("Stables", "user");

            checkColumn("Horses", "stableid", "INT NOT NULL DEFAULT '0' AFTER `id`");
            checkColumn("Horses", "stablegroup", "VARCHAR(30) NOT NULL DEFAULT 'default' COLLATE utf8_general_ci AFTER `stableid`");
            checkColumn("Horses", "name", "VARCHAR(30) NOT NULL DEFAULT '' COLLATE utf8_general_ci AFTER `stablegroup`");
            checkColumn("Horses", "type", "VARCHAR(16) NOT NULL DEFAULT '' COLLATE utf8_general_ci AFTER `name`");
            checkColumn("Horses", "lastDeath", "BIGINT NOT NULL DEFAULT '0' AFTER `type`");
            checkColumn("Horses", "maxhealth", "DOUBLE NOT NULL DEFAULT '20' AFTER `lastDeath`");
            checkColumn("Horses", "health", "DOUBLE NOT NULL DEFAULT '20' AFTER `maxhealth`");
            checkColumn("Horses", "speed", "DOUBLE NOT NULL DEFAULT '0.225' AFTER `health`");
            checkColumn("Horses", "jumpstrength", "DOUBLE NOT NULL DEFAULT '0.7' AFTER `speed`");
            checkColumn("Horses", "chested", "TINYINT NOT NULL DEFAULT '0' AFTER `jumpstrength`");
            checkColumn("Horses", "inventory", "VARCHAR(10000) NOT NULL DEFAULT 'i: []' COLLATE utf8_general_ci AFTER `chested`");
        } catch (SQLException e) {
            plugin.severe("Failed to create MySQL Tables");
            throw e;
        }
    }

    private void createTables(String[] tables) throws SQLException {
        for (String table : tables) {
            Statement stmt = this.conn.createStatement();
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS `" + table + "` (" + "`id` INT NOT NULL AUTO_INCREMENT," + "PRIMARY KEY (`id`)) ENGINE=InnoDB");
        }
    }

    private void checkColumn(String table, String column, String settings)
            throws SQLException {
        try {
            Statement stmt = this.conn.createStatement();
            stmt.executeUpdate(String.format(Locale.US, "ALTER TABLE `%1$s` CHANGE `%2$s` `%2$s` %3$s", new Object[]{table, column, settings}));
        } catch (SQLException e) {
            Statement stmt = this.conn.createStatement();
            stmt.executeUpdate(String.format(Locale.US, "ALTER TABLE `%1$s` ADD `%2$s` %3$s", new Object[]{table, column, settings}));
        }
    }

    private void addUniqueIndex(String table, String column) throws SQLException {
        Statement stmt = this.conn.createStatement();
        ResultSet result = stmt.executeQuery(String.format(Locale.US, "SELECT * FROM INFORMATION_SCHEMA.STATISTICS WHERE table_schema=DATABASE() AND table_name='%s' AND index_name='%s'", new Object[]{table, column}));

        if (result.next()) {
            return;
        }
        stmt = this.conn.createStatement();
        stmt.executeUpdate(String.format(Locale.US, "ALTER TABLE `%s` ADD UNIQUE (`%s`)", new Object[]{table, column}));
    }

    private boolean connect() {
        if (this.conn != null) {
            try {
                if (System.currentTimeMillis() - this.lastCheck > 60000L) {
                    if (!this.conn.isClosed()) {
                        this.lastCheck = System.currentTimeMillis();
                        return true;
                    }
                }
            } catch (SQLException e) {
            }
        }
        String connection = String.format(Locale.US, "jdbc:mysql://%s/%s", new Object[]{this.settings.host, this.settings.database});
        try {
            Class.forName("com.mysql.jdbc.Driver");

            this.conn = DriverManager.getConnection(connection, this.settings.user, this.settings.password);
        } catch (SQLException e) {
            if (this.spam++ < 20)
                getPlugin().severe("Failed to connect to the MySQL database '%s'", e, new Object[]{connection});
            else
                getPlugin().severe("Failed to connect to the MySQL database '%s'. See above for error", new Object[]{connection});
        } catch (ClassNotFoundException e) {
            if (this.spam++ < 20)
                getPlugin().severe("Couldn't find MySQL driver", e, new Object[0]);
            else {
                getPlugin().severe("Couldn't find MySQL driver. See above for error");
            }
        }
        this.lastCheck = System.currentTimeMillis();
        return this.conn != null;
    }

    protected List<Stable> loadEverything() {
        if (!connect()) {
            getPlugin().severe("Failed to connect to database to copy contents");
            return Collections.emptyList();
        }

        List<String> stableGroups = new ArrayList();
        List<Stable> stables = new ArrayList();
        try {
            Statement stmt = this.conn.createStatement();
            ResultSet result = stmt.executeQuery("SELECT DISTINCT(`stablegroup`) FROM `Horses`");

            while (result.next()) {
                stableGroups.add(result.getString("stablegroup"));
            }

            stmt = this.conn.createStatement();
            result = stmt.executeQuery("SELECT * FROM `Stables`");
            int id;
            String playerName;
            String lastActive;
            while (result.next()) {
                id = result.getInt("id");
                playerName = result.getString("user");
                lastActive = result.getString("lastactive");

                for (String group : stableGroups) {
                    Stable stable = new Stable(getPlugin(), group, playerName, id);
                    loadHorses(stable, group);

                    if (stable.getHorseCount() > 0) {
                        stable.setLastActiveHorse(stable.findHorse(lastActive, true));
                        stables.add(stable);
                    }
                }
            }

            return stables;
        } catch (SQLException e) {
            getPlugin().severe("Failed to fetch horse data from MySQL database", e, new Object[0]);
        }
        return Collections.emptyList();
    }

    protected void importStables(List<Stable> stables) {
        HashMap stableIds = new HashMap();

        for (Stable stable : stables) {
            Integer id = (Integer) stableIds.get(stable.getOwner());

            if (id != null) {
                stable.setId(id.intValue());
            }
            saveStable(stable);

            if (id == null) {
                stableIds.put(stable.getOwner(), id);
            }
            for (PlayerHorse horse : stable) {
                saveHorse(horse);
            }
        }
    }

    protected Stable loadStable(String player, String stableGroup) {
        if (!connect()) {
            return null;
        }
        try {
            Statement stmt = this.conn.createStatement();

            ResultSet result = stmt.executeQuery(String.format(Locale.US, "SELECT * FROM `Stables` WHERE `user`='%s'", new Object[]{player}));

            int id = -1;
            String lastActive = null;

            while (result.next()) {
                id = result.getInt("id");
                lastActive = result.getString("lastactive");
            }

            Stable stable = new Stable(getPlugin(), stableGroup, player, id);

            loadHorses(stable, stableGroup);

            if (lastActive != null) {
                PlayerHorse horse = stable.findHorse(lastActive, true);
                stable.setLastActiveHorse(horse);
            }

            return stable;
        } catch (SQLException e) {
            getPlugin().severe("Failed to load players Stable: '%s'", e, new Object[]{player});
        }

        return null;
    }

    protected void loadHorses(Stable stable, String stableGroup) {
        try {
            Statement stmt = this.conn.createStatement();
            ResultSet result = stmt.executeQuery(String.format(Locale.US, "SELECT * FROM `Horses` WHERE `stableid`='%d' AND `stablegroup`='%s'", new Object[]{Integer.valueOf(stable.getId()), stableGroup}));

            while (result.next()) {
                try {
                    int horseId = result.getInt("id");

                    String name = result.getString("name");

                    HorseType type = HorseType.exactValueOf(result.getString("type"));

                    long lastDeath = result.getLong("lastdeath");
                    double maxHealth = result.getDouble("maxhealth");
                    double health = result.getDouble("health");
                    double speed = result.getDouble("speed");
                    double jumpStrength = result.getDouble("jumpstrength");
                    boolean hasChest = (type == HorseType.Mule) || (type == HorseType.Donkey) ? result.getBoolean("chested") : false;

                    YamlConfiguration itemCfg = this.cacheCfg;
                    ArrayList items = null;
                    try {
                        itemCfg.loadFromString(result.getString("inventory"));
                    } catch (InvalidConfigurationException e) {
                        getPlugin().severe("Error when loading player %s's horses inventory", e, new Object[]{stable.getOwner()});
                    }

                    for (Map itemMap : itemCfg.getMapList("i")) {
                        int slot = -1;
                        try {
                            slot = ((Integer) itemMap.get("slot")).intValue();
                        } catch (NullPointerException e) {
                            getPlugin().log(Level.SEVERE, "Player '%s' mysql data is corrupt: Inventory slot number was missing", e, new Object[]{stable.getOwner()});
                            continue;
                        } catch (ClassCastException e) {
                            getPlugin().log(Level.SEVERE, "Player '%s' mysql data is corrupt: Inventory slot number was not a number", e, new Object[]{stable.getOwner()});
                            continue;
                        }

                        ItemStack item = ItemStack.deserialize(itemMap);

                        if (items == null) {
                            items = this.cacheItemList;
                            items.clear();
                        }

                        while (items.size() <= slot) {
                            items.add(null);
                        }
                        items.set(slot, item);
                    }

                    PlayerHorse horseData = new PlayerHorse(getPlugin(), stable, name, type, maxHealth, health, speed, jumpStrength, null, horseId);

                    horseData.setLastDeath(lastDeath);

                    if (items != null) {
                        horseData.setItems((ItemStack[]) items.toArray(new ItemStack[items.size()]));
                        items.clear();
                    }

                    horseData.setHasChest(hasChest);

                    stable.addHorse(horseData);
                } catch (SQLException e) {
                    getPlugin().severe("Failed to load one of the player %s's Horses", e, new Object[]{stable.getOwner()});
                }
            }
        } catch (SQLException e) {
            getPlugin().severe("Failed to load the player %s's Horses", e, new Object[]{stable.getOwner()});
        }
    }

    protected void saveStable(Stable stable) {
        if ((stable.getHorseCount() == 0) && (stable.getId() == -1)) {
            return;
        }

        if (!connect()) {
            return;
        }

        Statement stmt = null;
        try {
            stmt = this.conn.createStatement();
        } catch (SQLException e) {
            getPlugin().severe("Failed to save the player %s's stable data", e, new Object[]{stable.getOwner()});
            return;
        }

        if (stable.getId() == -1) {
            try {
                String query = String.format(Locale.US, "INSERT INTO `Stables` (`user`, `lastactive`) VALUES ('%s', '%s')", new Object[]{stable.getOwner(), stable.getLastActiveHorse() != null ? stable.getLastActiveHorse().getName() : ""});
                stmt.executeUpdate(query, 1);

                ResultSet result = stmt.getGeneratedKeys();
                while (result.next()) {
                    stable.setId(result.getInt(1));
                }
            } catch (SQLException e) {
                getPlugin().severe("Failed to insert the player %s's stable into the Database", new Object[]{stable.getOwner()});
            }

        } else if (stable.getHorseCount() > 0) {
            try {
                String query = String.format(Locale.US, "UPDATE `Stables` SET `lastactive`='%s' WHERE `id`='%d'", new Object[]{stable.getLastActiveHorse() != null ? stable.getLastActiveHorse().getName() : "", Integer.valueOf(stable.getId())});
                stmt.executeUpdate(query);
            } catch (SQLException e) {
                getPlugin().severe("Failed to update the player %s's stable in the database", e, new Object[]{stable.getOwner()});
            }

        } else {
            try {
                String query = String.format(Locale.US, "DELETE FROM `Stables` WHERE `id`='%d'", new Object[]{Integer.valueOf(stable.getId())});
                stmt.executeUpdate(query);
            } catch (SQLException e) {
                getPlugin().severe("Failed to delete the player %s's stable in the database", e, new Object[]{stable.getOwner()});
            }
        }
    }

    public void saveHorse(PlayerHorse horse) {
        if (!connect()) {
            return;
        }
        Statement stmt;
        try {
            stmt = this.conn.createStatement();
        } catch (SQLException e) {
            getPlugin().severe("Failed to save the player %s's horse data to the database", e, new Object[]{horse.getStable().getOwner()});
            return;
        }

        if (horse.getStable().getId() == -1) {
            saveStable(horse.getStable());
        }

        String name = COLOUR_CHAR_REPLACE.matcher(horse.getDisplayName()).replaceAll("&");
        HorseType type = horse.getType();
        long lastDeath = horse.getLastDeath();
        double maxhealth = horse.getMaxHealth();
        double health = horse.getHealth();
        double speed = horse.getSpeed();
        double jumpstrength = horse.getJumpStrength();
        boolean chested = horse.hasChest();
        String inventoryString = getInventoryString(horse);

        if (horse.getId() == -1) {
            try {
                String query = String.format(Locale.US, "INSERT INTO `Horses` (`stableid`, `stablegroup`, `name`, `type`, `lastdeath`, `maxhealth`, `health`, `speed`, `jumpstrength`, `chested`, `inventory`) VALUES ('%d', '%s', '%s', '%s', '%d', '%f', '%f', '%f', '%f', '%d', '%s')", new Object[]{Integer.valueOf(horse.getStable().getId()), horse.getStable().getGroup(), name, type.toString(), Long.valueOf(lastDeath), Double.valueOf(maxhealth), Double.valueOf(health), Double.valueOf(speed), Double.valueOf(jumpstrength), Integer.valueOf(chested ? 1 : 0), inventoryString});

                stmt.executeUpdate(query, 1);

                ResultSet result = stmt.getGeneratedKeys();
                while (result.next()) {
                    horse.setId(result.getInt(1));
                }
            } catch (SQLException e) {
                getPlugin().severe("Failed to insert the player %s's horse '%s' into the database", e, new Object[]{horse.getStable().getOwner(), horse.getName()});
            }

        } else {
            try {
                String query = String.format(Locale.US, "UPDATE `Horses` SET `name`='%s', `type`='%s', `lastdeath`='%d', `maxhealth`='%f', `health`='%f', `speed`='%f', `jumpstrength`='%f', `chested`='%d', `inventory`='%s' WHERE `id`='%d'", new Object[]{name, type.toString(), Long.valueOf(lastDeath), Double.valueOf(maxhealth), Double.valueOf(health), Double.valueOf(speed), Double.valueOf(jumpstrength), Integer.valueOf(chested ? 1 : 0), inventoryString, Integer.valueOf(horse.getId())});

                stmt.executeUpdate(query);
            } catch (SQLException e) {
                getPlugin().severe("Failed to update the player %s's horse '%s' in the database", e, new Object[]{horse.getStable().getOwner(), horse.getName()});
            }
        }
    }

    public boolean deleteHorse(PlayerHorse horse) {
        if (horse.getId() == -1) {
            return true;
        }
        if (!connect()) {
            return false;
        }
        try {
            Statement stmt = this.conn.createStatement();

            String query = String.format(Locale.US, "DELETE FROM `Horses` WHERE `id`='%d'", new Object[]{Integer.valueOf(horse.getId())});
            stmt.executeUpdate(query);
            return true;
        } catch (SQLException e) {
            getPlugin().severe("Failed to delete the player %s's horse '%s' from the database", new Object[]{horse.getStable().getOwner(), horse.getName()});
        }
        return false;
    }

    private String getInventoryString(PlayerHorse horse) {
        ArrayList itemList = this.cacheItemList;
        itemList.clear();

        ItemStack[] items = horse.getItems();
        for (int i = 0; i < items.length; i++) {
            if (items[i] != null) {
                Map itemMap = items[i].serialize();
                itemMap.put("slot", Integer.valueOf(i));
                itemList.add(itemMap);
            }
        }
        YamlConfiguration itemCfg = this.cacheCfg;

        itemCfg.set("i", itemList);
        String inventoryString = itemCfg.saveToString();
        itemCfg.set("i", null);

        return inventoryString;
    }

    private class MysqlSettings extends AbstractConfig {
        public final String host;
        public final String database;
        public final String user;
        public final String password;

        protected MysqlSettings(Horses plugin) {
            super(plugin, null, null, "mysql");

            loadConfiguration();

            addResourseToHeader("header_mysql.txt");

            this.host = ((String) getAndSet("Host", "localhost", String.class));
            this.database = ((String) getAndSet("Database", "default", String.class));

            this.user = ((String) getAndSet("User", "root", String.class));
            this.password = ((String) getAndSet("Password", "", String.class));

            saveConfiguration();
        }
    }
}