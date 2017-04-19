package com.forgenz.horses.database;

import com.forgenz.forgecore.v1_0.util.BukkitConfigUtil;
import com.forgenz.horses.HorseType;
import com.forgenz.horses.Horses;
import com.forgenz.horses.PlayerHorse;
import com.forgenz.horses.Stable;
import org.anjocaido.groupmanager.GroupManager;
import org.anjocaido.groupmanager.data.User;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.regex.Pattern;

public class YamlDatabase extends HorseDatabase {
    private static final String PLAYER_DATA_FOLDER = "playerdata";
    private static final String PLAYER_DATA_LOCATION = "playerdata" + File.separatorChar + "%s.yml";
    private static final String GROUPED_PLAYER_DATA_LOCATION = "playerdata" + File.separatorChar + "%s" + File.separatorChar + "%s.yml";

    public YamlDatabase(Horses plugin) {
        super(plugin, HorseDatabaseStorageType.YAML);
    }

    private File getPlayersConfigFile(String player, String stableGroup) {
        if (stableGroup.equals("default")) {
            return new File(getPlugin().getDataFolder(), String.format(PLAYER_DATA_LOCATION, new Object[]{player}));
        }
        return new File(getPlugin().getDataFolder(), String.format(GROUPED_PLAYER_DATA_LOCATION, new Object[]{stableGroup, player}));
    }

    private File getPlayersConfigFile(OfflinePlayer player, String stableGroup) {
        if (!Bukkit.getOnlineMode()) {
            return getPlayersConfigFile(player.getName(), stableGroup);
        }

        File uuidFile = getPlayersConfigFile(player.getUniqueId().toString(), stableGroup);

        if (uuidFile.exists()) {
            return uuidFile;
        }

        File nameFile = getPlayersConfigFile(player.getName(), stableGroup);

        if (nameFile.exists()) {
            return nameFile.renameTo(uuidFile) ? uuidFile : nameFile;
        }

        return uuidFile;
    }

    private YamlConfiguration getPlayerConfig(OfflinePlayer player, String stableGroup) {
        YamlConfiguration cfg = new YamlConfiguration();

        File file = getPlayersConfigFile(player, stableGroup);

        if (file.exists()) {
            try {
                cfg.load(file);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InvalidConfigurationException e) {
                e.printStackTrace();
            }
        }

        return cfg;
    }

    public List<Stable> loadEverything() {
        File playerDataFolder = new File(getPlugin().getDataFolder(), "playerdata");

        if (!playerDataFolder.isDirectory()) {
            return Collections.emptyList();
        }

        ArrayList stables = new ArrayList();

        loadStableGroup(playerDataFolder, stables, true);

        return stables;
    }

    protected void importStables(List<Stable> stables) {
        for (Stable stable : stables)
            saveStable(stable);
    }

    private void loadStableGroup(File folder, ArrayList<Stable> stables, boolean recursive) {
        String groupName = folder.getName().equals("playerdata") ? "default" : folder.getName();
        Pattern extentionReplace = Pattern.compile("\\.yml$", 2);

        File[] fileList = folder.listFiles();

        stables.ensureCapacity(fileList.length + stables.size());

        for (File file : fileList) {
            if (file.isDirectory()) {
                if (recursive)
                    loadStableGroup(file, stables, false);
            } else {
                String playerName = extentionReplace.matcher(file.getName()).replaceAll("");
                stables.add(loadStable(playerName, groupName));
            }
        }
    }

    protected Stable loadStable(String player, String stableGroup) {
        Stable stable = new Stable(getPlugin(), stableGroup, player);

        loadHorses(stable, stableGroup);

        return stable;
    }

    protected void loadHorses(Stable stable, String stableGroup) {
        YamlConfiguration cfg = getPlayerConfig(Bukkit.getOfflinePlayer(stable.getOwner()), stableGroup);

        ConfigurationSection sect = BukkitConfigUtil.getAndSetConfigurationSection(cfg, "Horses");

        for (String horse : sect.getKeys(false)) {
            ConfigurationSection horseSect = sect.getConfigurationSection(horse);
            configToHorse(stable,horseSect,getPlugin());
        }

        if (cfg.isString("lastactive")) {
            PlayerHorse horse = stable.findHorse(cfg.getString("lastactive"), true);
            stable.setLastActiveHorse(horse);
        }
    }
    public static PlayerHorse configToHorse(Stable stable, ConfigurationSection horseSect, Horses plugin) {
        HorseType type = HorseType.exactValueOf(horseSect.getString("type", HorseType.White.toString()));
        long lastDeath = horseSect.getLong("lastdeath") * 1000L;
        double maxHealth = horseSect.getDouble("maxhealth");
        double health = horseSect.getDouble("health");
        double speed = horseSect.getDouble("speed", 0.225D);
        double jumpStrength = horseSect.getDouble("jumpstrength", 0.7D);
        boolean renameable = true;
        if (horseSect.contains("renameable")) {
            renameable = horseSect.getBoolean("renameable");
        }
        boolean hasChest = (type == HorseType.Mule) || (type == HorseType.Donkey) ? horseSect.getBoolean("chest", false) : false;

        ItemStack saddle = null;
        if (horseSect.contains("saddle")) {
            if (horseSect.isBoolean("saddle")) {
                if (horseSect.getBoolean("saddle")) {
                    saddle = new ItemStack(Material.SADDLE);
                }
            }else {
                saddle = horseSect.getItemStack("saddle");
            }
        }

        ItemStack armour = null;
        if (horseSect.contains("armour")) {
            if (horseSect.isString("armour")) {
                armour = new ItemStack(Material.getMaterial(horseSect.getString("armour", "null")));
            }else {
                armour = horseSect.getItemStack("armour");
            }
        }
        String trail = null;
        if (horseSect.contains("trail")) {
            trail = horseSect.getString("trail");
        }
        ArrayList items = new ArrayList();

        for (Map itemMap : horseSect.getMapList("inventory")) {
            int slot = -1;
            try {
                slot = ((Integer) itemMap.get("slot")).intValue();
            } catch (NullPointerException e) {
                plugin.log(Level.SEVERE, "Player '%s' data file is corrupt: Inventory slot number was missing", e, new Object[]{stable.getOwner()});
                continue;
            } catch (ClassCastException e) {
                plugin.log(Level.SEVERE, "Player '%s' data file is corrupt: Inventory slot number was not a number", e, new Object[]{stable.getOwner()});
                continue;
            }

            ItemStack item = ItemStack.deserialize(itemMap);

            while (items.size() <= slot) {
                items.add(null);
            }
            items.set(slot, item);
        }

        PlayerHorse horseData = new PlayerHorse(plugin, stable, horseSect.getName(), type, maxHealth, health, speed, jumpStrength, null);
        horseData.setLastDeath(lastDeath);
        horseData.setRenamable(renameable);

        horseData.setItems((ItemStack[]) items.toArray(new ItemStack[items.size()]));
        if (saddle != null)
            horseData.setSaddle(saddle);
        if (armour != null)
            horseData.setArmour(armour);
        horseData.setHasChest(hasChest);

        stable.addHorse(horseData);
        return horseData;
    }
    public static void saveHorseToSection(ConfigurationSection sect, PlayerHorse horse) {
        String colourCodedDisplayName = COLOUR_CHAR_REPLACE.matcher(horse.getDisplayName()).replaceAll("&");
        ConfigurationSection horseSect = BukkitConfigUtil.getAndSetConfigurationSection(sect, colourCodedDisplayName);

        horseSect.set("type", horse.getType().toString());
        horseSect.set("lastdeath", Long.valueOf(horse.getLastDeath() / 1000L));
        horseSect.set("maxhealth", Double.valueOf(horse.getMaxHealth()));
        horseSect.set("health", Double.valueOf(horse.getHealth()));
        horseSect.set("speed", Double.valueOf(horse.getSpeed()));
        horseSect.set("jumpstrength", Double.valueOf(horse.getJumpStrength()));
        if ((horse.getType() == HorseType.Mule) || (horse.getType() == HorseType.Donkey)) {
            horseSect.set("chest", Boolean.valueOf(horse.hasChest()));
        } else {
            horseSect.set("chest", null);
        }

        horseSect.set("saddle", null);
        horseSect.set("armour", null);
        horseSect.set("renameable",horse.isRenamable());

        ArrayList itemList = new ArrayList();

        ItemStack[] items = horse.getItems();
        for (int i = 0; i < items.length; i++) {
            if (items[i] != null) {
                Map item = items[i].serialize();

                item.put("slot", Integer.valueOf(i));

                itemList.add(item);
            }
        }
        horseSect.set("inventory", itemList);
    }
    protected void saveStable(Stable stable) {
        File playerDataFile = getPlayersConfigFile(Bukkit.getOfflinePlayer(stable.getOwner()), stable.getGroup());

        if (stable.getHorseCount() == 0) {
            if (playerDataFile.exists())
                playerDataFile.delete();
            return;
        }

        YamlConfiguration cfg = new YamlConfiguration();

        if (stable.getLastActiveHorse() != null)
            cfg.set("lastactive", stable.getLastActiveHorse().getName());
        else {
            cfg.set("lastactive", null);
        }
        ConfigurationSection sect = BukkitConfigUtil.getAndSetConfigurationSection(cfg, "Horses");

        for (PlayerHorse horse : stable) {
            saveHorseToSection(sect,horse);
        }

        try {
            cfg.save(playerDataFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveHorse(PlayerHorse horse) {
        saveStable(horse.getStable());
    }

    public boolean deleteHorse(PlayerHorse horse) {
        saveStable(horse.getStable());
        return true;
    }

    public boolean migrateToUuidDb() {
        File dataFolder = new File(super.getPlugin().getDataFolder(), "playerdata");

        return migrateFile(dataFolder, true);
    }
    public boolean fixUndeadSkel() {
        File dataFolder = new File(super.getPlugin().getDataFolder(), "playerdata");
        return fixUndeadSkel(dataFolder,true);
    }
    private boolean fixUndeadSkel(File file, boolean top) {
        if (file.isDirectory()) {
            if (!top) {
                return true;
            }
            boolean success = true;
            for (File data : file.listFiles()) {
                success = (success) && (fixUndeadSkel(data, false));
            }
            return success;
        }

        if (!file.getName().endsWith(".yml")) {
            return true;
        }

        String fileName = file.getName().substring(0, file.getName().length() - ".yml".length());
        OfflinePlayer offlinePlayer;
        try {
            offlinePlayer = Bukkit.getOfflinePlayer(UUID.fromString(fileName));
        } catch (IllegalArgumentException e) {
            offlinePlayer = Bukkit.getOfflinePlayer(fileName);
        }
        if (offlinePlayer != null) {
            YamlConfiguration cfg = new YamlConfiguration();
            try {
                cfg.load(file);
                if (cfg.contains("Horses")) {
                    ConfigurationSection horsesSection = cfg.getConfigurationSection("Horses");
                    for (String key : horsesSection.getKeys(false)) {
                        ConfigurationSection horseSection = horsesSection.getConfigurationSection(key);
                        if (horseSection.contains("type")) {
                            String typeString = horseSection.getString("type");
                            HorseType horseType = HorseType.exactValueOf(typeString);
                            if (horseType != null) {
                                if (horseType == HorseType.Skeleton || horseType == HorseType.Undead) {
                                    final PluginManager pluginManager = Bukkit.getServer().getPluginManager();
                                    final Plugin GMplugin = pluginManager.getPlugin("GroupManager");

                                    if (GMplugin != null && GMplugin.isEnabled()) {
                                        GroupManager groupManager = (GroupManager) GMplugin;
                                        try {
                                            String playerName = offlinePlayer.getName();
                                            if (playerName == null) {
                                                playerName = fileName;
                                            }
                                            User user = groupManager.getWorldsHolder().getDefaultWorld().getUser(playerName);
                                            if (user != null && !user.hasSamePermissionNode(horseType.getTamePermission())) {
                                                horseSection.set("type", HorseType.Black.toString());
                                                cfg.save(file);
                                            }
                                        }catch (Exception ignored) {}
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (InvalidConfigurationException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    private boolean migrateFile(File file, boolean top) {
        if (file.isDirectory()) {
            if (!top) {
                return true;
            }
            boolean success = true;
            for (File data : file.listFiles()) {
                success = (success) && (migrateFile(data, false));
            }
            return success;
        }

        if (!file.getName().endsWith(".yml")) {
            return true;
        }

        String fileName = file.getName().substring(0, file.getName().length() - ".yml".length());
        try {
            UUID.fromString(fileName);

            return true;
        } catch (IllegalArgumentException e) {
            OfflinePlayer player = Bukkit.getOfflinePlayer(fileName);

            UUID id = player.getUniqueId();

            if (id == null) {
                return false;
            }

            File migratedFile = new File(file.getParentFile(), id.toString() + ".yml");

            if (migratedFile.exists()) {
                getPlugin().getLogger().info(String.format("Player has two datafiles '%s' and '%s'", new Object[]{migratedFile.getPath(), file.getPath()}));
                return false;
            }

            return file.renameTo(migratedFile);
        }
    }
}