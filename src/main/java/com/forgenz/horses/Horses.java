package com.forgenz.horses;

import com.forgenz.forgecore.v1_0.bukkit.ForgePlugin;
import com.forgenz.forgecore.v1_0.command.ForgeCommandHandler;
import com.forgenz.forgecore.v1_0.locale.ForgeLocale;
import com.forgenz.horses.command.*;
import com.forgenz.horses.config.HorsesConfig;
import com.forgenz.horses.database.HorseDatabase;
import com.forgenz.horses.listeners.*;
import com.forgenz.horses.pack.PackDatabase;
import com.forgenz.horses.tasks.HorseDismissTask;
import org.bukkit.ChatColor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class Horses extends ForgePlugin {
    private static Horses plugin;
    private ForgeLocale locale;
    private HorsesConfig config;
    private HorseDatabase database;
    private ForgeCommandHandler commandHandler;
    private HorseDismissTask horseDismissTask;
    private Plugin noCheatPlus;
    private HorseSpawnListener spawnListener;
    private SummonCommand summonCmd;
    private PackDatabase packDatabase;

    public static Horses getInstance() {
        return plugin;
    }

    public void onLoad() {
    }

    public void onEnable() {
        super.onEnable();
        try {
            plugin = this;

            setupEconomy();

            File configurationFile = new File(getDataFolder(), "config.yml");
            try {
                YamlConfiguration cfg = new YamlConfiguration();

                cfg.load(configurationFile);
            } catch (InvalidConfigurationException e) {
                configurationFile.renameTo(new File(getDataFolder(), "config.yml.broken"));
            } catch (FileNotFoundException e) {
            } catch (IOException e) {
            }
            reloadConfig();

            this.locale = new ForgeLocale(this);
            this.locale.registerEnumMessages(Messages.class);
            this.locale.updateMessages();

            this.config = new HorsesConfig(this);
            packDatabase = new PackDatabase(this);
            setupWorldGuard(this.config.worldGuardCfg != null);
            setupNoCheatPlus();

            this.spawnListener = new HorseSpawnListener(this);

            this.horseDismissTask = new HorseDismissTask(this);
            this.horseDismissTask.runTaskTimer(this, 100L, 100L);

            this.database = this.config.databaseType.create(this, true);
            this.database.importHorses(this.config.importDatabaseType);

            this.commandHandler = new ForgeCommandHandler(this);
            getCommand("horses").setExecutor(this.commandHandler);
            this.commandHandler.setNumCommandsPerHelpPage(5);

            if (this.config.showAuthor) {
                this.commandHandler.setHeaderFormat(String.format("%1$s%3$s %2$sv%1$s%4$s %2$sby %1$s%5$s", new Object[]{ChatColor.DARK_GREEN, ChatColor.YELLOW, "Horses", "%VERSION%", "ShadowDog007"}));
            } else {
                this.commandHandler.setHeaderFormat(String.format("%1$s%3$s %2$sv%1$s%4$s", new Object[]{ChatColor.DARK_GREEN, ChatColor.YELLOW, "Horses", "%VERSION%"}));
            }

            this.commandHandler.registerCommand(new GiveCommand(this));
            this.commandHandler.registerCommand(new BuyCommand(this));
            this.commandHandler.registerCommand(new DeleteCommand(this));
            this.commandHandler.registerCommand(new DismissCommand(this));
            this.commandHandler.registerCommand(new HealCommand(this));
            this.commandHandler.registerCommand(new ListCommand(this));
            this.commandHandler.registerCommand(new RenameCommand(this));
            this.commandHandler.registerCommand(this.summonCmd = new SummonCommand(this));
            this.commandHandler.registerCommand(new TypeCommand(this));
            this.commandHandler.registerCommand(new RebuildCommand(this));
            this.commandHandler.registerCommand(new ConvertHorsesCommand(this));

            this.commandHandler.registerCommand(new ReloadCommand(this));
            this.commandHandler.registerCommand(new PackCommand(this));
            this.commandHandler.registerCommand(new FixMaxCommand(this));

            if (this.config.isProtecting())
                new DamageListener(this);
            new PlayerMoveListener(this);
            new HorseDeathListener(this);
            new InteractListener(this);
            new PlayerListener(this);
            new TeleportListener(this);
            new HorseUnpackListener(this);

            saveConfig();
        } catch (Exception e) {
            severe("Error when attempting to enable %s v%s", e, new Object[]{getName(), getDescription().getVersion()});
            severe("Try updating to the latest build of CraftBukkit or Horses");
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    public void onDisable() {
        unregisterListeners();

        if (this.database != null)
            this.database.saveAll();
        if (this.horseDismissTask != null)
            this.horseDismissTask.cancel();
        this.summonCmd = null;
        this.commandHandler = null;
        this.database = null;
        this.config = null;
        this.locale = null;
        plugin = null;
    }

    public PackDatabase getPackDatabase() {
        return packDatabase;
    }

    private void setupNoCheatPlus() {
        this.noCheatPlus = getServer().getPluginManager().getPlugin("NoCheatPlus");
    }

    public HorsesConfig getHorsesConfig() {
        return this.config;
    }

    public HorseDatabase getHorseDatabase() {
        return this.database;
    }

    public HorseSpawnListener getHorseSpawnListener() {
        return this.spawnListener;
    }

    public ForgeCommandHandler getCommandHandler() {
        return this.commandHandler;
    }

    public boolean isNoCheatPlusEnabled() {
        return (this.noCheatPlus != null) && (this.noCheatPlus.isEnabled());
    }

    public SummonCommand getSummonCmd() {
        return this.summonCmd;
    }
}