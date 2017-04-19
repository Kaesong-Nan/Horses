package com.forgenz.horses;

import com.forgenz.forgecore.v1_0.ForgeCore;
import com.forgenz.horses.attribute.*;
import com.forgenz.horses.util.HorseSpeedUtil;
import com.voxmc.voxlib.util.VoxEffects;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public class PlayerHorse
        implements ForgeCore {
    private static final String OWNERSHIP_METADATA_KEY = "Horses.Ownership";
    private static final Location cacheLoc = new Location(null, 0.0D, 0.0D, 0.0D);

    public static final Pattern FORMATTING_CODES_PATTERN = Pattern.compile("&[klmnor]", 2);
    private final Horses plugin;
    private final Stable stable;
    private int id;
    private AbstractHorse horse;
    private long lastDeath = 0L;
    private String name;
    private String displayName;
    private HorseType type;
    private double maxHealth;
    private double health;
    private double speed;
    private double jumpStrength;
    private boolean hasChest = false;
    private boolean renamable;

    private final ArrayList<ItemStack> inventory = new ArrayList();

    public PlayerHorse(Horses plugin, Stable stable, String name, HorseType type, double maxHealth, double health, double speed, double jumpStrength, Horse horse) {
        this(plugin, stable, name, type, maxHealth, health, speed, jumpStrength, horse,-1);
    }

    public PlayerHorse(Horses plugin, Stable stable, String name, HorseType type, double maxHealth, double health, double speed, double jumpStrength, Horse horse, int id) {
        this.plugin = plugin;
        this.stable = stable;
        this.id = id;

        this.displayName = ChatColor.translateAlternateColorCodes('&', name).replaceAll("&", "").replaceAll(" ", "_").replaceAll("\\.", "");
        this.name = ChatColor.stripColor(this.displayName);

        this.type = type;

        if (health <= 0.0D)
            health = getPlugin().getHorsesConfig().getHorseTypeConfig(null, type).horseHp;
        if ((health > maxHealth) || (maxHealth <= 0.0D)) {
            maxHealth = health;
        }
        this.maxHealth = maxHealth;
        this.health = health;

        if ((getPlugin().getHorsesConfig().fixZeroJumpStrength) && (jumpStrength <= 0.0D)) {
            jumpStrength = 0.7D;
        }
        this.speed = speed;
        this.jumpStrength = jumpStrength;

        this.horse = horse;

        if (this.horse != null) {
            getMaxHealth();
            getHealth();
            hasChest();
            getItems();
            getSpeed();
            getJumpStrength();
            this.horse = null;
            horse.remove();
        }
    }

    public Stable getStable() {
        return this.stable;
    }

    public Horses getPlugin() {
        return this.plugin;
    }

    public boolean isRenamable() {
        return renamable && !plugin.getHorsesConfig().getBlackListNames().contains(name);
    }

    public void setRenamable(boolean renamable) {
        this.renamable = renamable;
    }

    public AbstractHorse getHorse() {
        return this.horse;
    }

    public String getName() {
        return this.name;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public HorseType getType() {
        return this.type;
    }

    public long getLastDeath() {
        return this.lastDeath;
    }

    public void setLastDeath(long time) {
        this.lastDeath = time;
    }

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getMaxHealth() {
        if ((this.horse != null) && (this.horse.isValid())) {
            this.maxHealth = this.horse.getMaxHealth();
        }
        return this.maxHealth;
    }

    public double getHealth() {
        if ((this.horse != null) && (this.horse.isValid())) {
            this.health = this.horse.getHealth();
        }
        return this.health;
    }

    public double getHealEstimate(float amount) {
        if (getMaxHealth() < getHealth() + amount) {
            return this.maxHealth - this.health;
        }

        return amount;
    }

    public void setMaxHealth(double amount) {
        this.maxHealth = amount;

        if (this.horse != null) {
            this.horse.setMaxHealth(amount);
        }
    }

    public void setHealth(double amount) {
        this.health = amount;

        if (this.horse != null) {
            this.horse.setHealth(amount);
        }
    }

    public double addHealth(double amount) {
        if (getMaxHealth() < getHealth() + amount) {
            amount = this.maxHealth - this.health;
        }

        this.health += amount;

        if ((this.horse != null) && (this.horse.isValid())) {
            this.horse.setHealth(this.health);
        }

        return amount;
    }


    public void addMaxHealth(double amount) {
        this.maxHealth = (getMaxHealth() + amount);
        this.health = (getHealth() + amount);

        if ((this.horse != null) && (this.horse.isValid())) {
            this.horse.setMaxHealth(this.maxHealth);
            this.horse.setHealth(this.health);
        }
    }

    public double getJumpStrength() {
        if (this.horse != null) {
            this.jumpStrength = this.horse.getJumpStrength();
        }

        return this.jumpStrength;
    }

    public double getSpeed() {
        if (this.horse != null) {
            this.speed = HorseSpeedUtil.getHorseSpeed(this.horse);
        }

        return this.speed;
    }


    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public void setJumpStrength(double jumpStrength) {
        this.jumpStrength = jumpStrength;
    }

    public void setSaddle(ItemStack saddle) {
        setItem(0, saddle);
    }

    public void setHasChest(boolean hasChest) {
        if ((this.type != HorseType.Mule) && (this.type != HorseType.Donkey)) {
            return;
        }
        this.hasChest = hasChest;
    }

    public boolean hasChest() {
        if ((this.type != HorseType.Mule) && (this.type != HorseType.Donkey)) {
            return false;
        }
        if (this.horse != null && this.horse instanceof ChestedHorse) {
            this.hasChest = ((ChestedHorse)this.horse).isCarryingChest();
        }

        return this.hasChest;
    }

    public void setArmour(Material material) {
        setItem(1, new ItemStack(material));
    }
    public void setArmour(ItemStack itemStack) {
        setItem(1, itemStack);
    }

    public ItemStack getItem(int i) {
        try {
            if ((this.inventory.size() <= i) && (i < 0)) {
                return null;
            }
            if (this.horse != null) {
                ItemStack item = this.horse.getInventory().getItem(i);
                if ((item != null) && (item.getType() == Material.AIR)) {
                    item = null;
                }
                this.inventory.set(i, item);
            }

            return (ItemStack) this.inventory.get(i);
        }catch (Exception e) {
            return null;
        }
    }

    public void setItem(int i, ItemStack item) {
        if (i < 0) {
            return;
        }
        if (this.inventory.size() <= i) {
            for (int index = this.inventory.size(); index < i; index++) {
                this.inventory.add(null);
            }
            this.inventory.add(item);
        }else {
            this.inventory.set(i,item);
        }
    }

    public void setItems(ItemStack[] items) {
        this.inventory.clear();
        this.inventory.ensureCapacity(items.length);

        for (ItemStack item : items)
            this.inventory.add(item);
    }

    public ItemStack[] getItems() {
        if (this.horse != null) {
            ItemStack[] items = this.horse.getInventory().getContents();

            this.inventory.clear();
            for (ItemStack item : items) {
                this.inventory.add(item);
            }
            return items;
        }

        return (ItemStack[]) this.inventory.toArray(new ItemStack[this.inventory.size()]);
    }

    public void removeHorse() {
        if (this.horse != null) {
            if (!this.horse.isDead()) {
                this.health = this.horse.getHealth();
            }

            getItems();
            hasChest();
            clearAttributes();
            if (this.horse.isDead()) {
                if (getPlugin().getHorsesConfig().getPermConfig(getStable().getPlayerOwner()).keepEquipmentOnDeath) {
                    this.horse.getInventory().clear();
                    if (this.horse instanceof ChestedHorse) {
                        ((ChestedHorse)this.horse).setCarryingChest(false);
                    }
                } else {
                    this.inventory.clear();
                    this.hasChest = false;
                }
            }
            if (this.horse.getLocation() != null) {
                playSound(this.horse.getLocation());
                VoxEffects voxEffects = plugin.getHorsesConfig().getDismisEffects();
                if (voxEffects != null) {
                    voxEffects.play(horse.getLocation());
                }
            }
            this.horse.remove();

            this.stable.removeActiveHorse(this);

            this.horse.removeMetadata("Horses.Ownership", getPlugin());
            this.horse = null;
        }

        saveChanges();
    }

    public boolean spawnHorse(Player player) {
        if (!player.getName().equals(getStable().getOwner())) {
            return false;
        }

        if ((this.horse != null) && (this.horse.isValid())) {
            this.horse.teleport(player);
            return true;
        }

        if (getStable().getActiveHorse() != null) {
            getStable().getActiveHorse().removeHorse();
        }

        Location loc = player.getLocation(cacheLoc);

        if (getPlugin().getHorsesConfig().getPermConfig(getStable().getPlayerOwner()).bypassSpawnProtection) {
            getPlugin().getHorseSpawnListener().setSpawning();
        }
        AbstractHorse horse;
        if (getType() == HorseType.Skeleton) {
            horse = (AbstractHorse) loc.getWorld().spawnEntity(loc, EntityType.SKELETON_HORSE);
        }else if (getType() == HorseType.Undead) {
            horse = (AbstractHorse) loc.getWorld().spawnEntity(loc, EntityType.ZOMBIE_HORSE);
        }else if (getType() == HorseType.Mule) {
            horse = (ChestedHorse) loc.getWorld().spawnEntity(loc, EntityType.MULE);
        }else if (getType() == HorseType.Donkey) {
            horse = (ChestedHorse) loc.getWorld().spawnEntity(loc, EntityType.MULE);
        }else {
            horse = (AbstractHorse) loc.getWorld().spawnEntity(loc, EntityType.HORSE);
        }
        horse.setRemoveWhenFarAway(false);

        getType().setHorseType(horse);

        horse.setAdult();
        horse.setTamed(true);
        horse.setOwner(getStable().getPlayerOwner());

        if (hasChest() && horse instanceof ChestedHorse) {
            ChestedHorse chestedHorse = (ChestedHorse) horse;
            chestedHorse.setCarryingChest(true);
        }
        Inventory inv = horse.getInventory();

        ItemStack[] items = inv.getContents();
        for (int i = 2; i < items.length; i++) {
            if (i >= this.inventory.size())
                items[i] = null;
            else
                items[i] = ((ItemStack) this.inventory.get(i));
        }
        inv.setContents(items);


        if (horse instanceof Horse) {
            Horse horse2 = (Horse) horse;
            Inventory horseInventory = horse2.getInventory();
            if (horseInventory != null) {
                if (this.inventory.size() > 0)
                    horseInventory.setItem(0,(ItemStack) this.inventory.get(0));
                if (this.inventory.size() > 1) {
                    horseInventory.setItem(1,(ItemStack) this.inventory.get(1));
                }
            }
        }


        horse.setCustomName(this.displayName);
        horse.setCustomNameVisible(true);

        horse.setMaxHealth(this.maxHealth);
        if (this.health > maxHealth) {
            health = maxHealth;
        }
        horse.setHealth(this.health);
        HorseSpeedUtil.setHorseSpeed(horse, getSpeed());
        horse.setJumpStrength(getJumpStrength());

        horse.setTarget(player);
        horse.setMetadata("Horses.Ownership", new FixedMetadataValue(getPlugin(), this));
        playSound(loc);
        VoxEffects voxEffects = plugin.getHorsesConfig().getSpawnEffects();
        if (voxEffects != null) {
            voxEffects.play(horse.getLocation());
        }
        getStable().setActiveHorse(this);

        this.horse = horse;
        new BukkitRunnable(){
            @Override
            public void run() {
                updateAttributes();
            }
        }.runTaskLater(plugin,1);


        return true;

    }
    public void playSound(Location loc) {
        World world = loc.getWorld();
        Horse.Variant variant = type.getVariant();
        if (variant == Horse.Variant.DONKEY) {
            world.playSound(loc,Sound.ENTITY_DONKEY_AMBIENT,1,1);
        }else if (variant == Horse.Variant.SKELETON_HORSE) {
            world.playSound(loc,Sound.ENTITY_SKELETON_HORSE_AMBIENT,1,1);
        }else if (variant == Horse.Variant.UNDEAD_HORSE) {
            world.playSound(loc,Sound.ENTITY_ZOMBIE_HORSE_AMBIENT,1,1);
        }else {
            world.playSound(loc,Sound.ENTITY_HORSE_AMBIENT,1,1);
        }
    }

    public void saveChanges() {
        getPlugin().getHorseDatabase().saveHorse(this);
    }

    public boolean deleteHorse() {
        if (this.horse != null) {
            this.horse.remove();
            getStable().removeActiveHorse(this);
        }

        return this.stable.deleteHorse(this);
    }

    public void rename(String name) {
        this.displayName = ChatColor.translateAlternateColorCodes('&', name).replaceAll("&", "").replaceAll(" ", "_");
        this.name = ChatColor.stripColor(this.displayName);

        if ((this.horse != null) && (this.horse.isValid())) {
            this.horse.setCustomName(this.displayName);
        }

        saveChanges();
    }
    public Set<Attribute> currentAttributes = new HashSet<>();
    public void removeAttribute(Attribute attribute) {
        if (currentAttributes.contains(attribute)) {
            currentAttributes.remove(attribute);
        }
        if (attribute instanceof BuffAttribute) {
            ((BuffAttribute) attribute).onRemove(this);
        }
    }
    public void addAttribute(Attribute attribute) {
        if (currentAttributes.contains(attribute)) {
            return;
        }
        currentAttributes.add(attribute);
        if (attribute instanceof BuffAttribute) {
            ((BuffAttribute) attribute).onAdd(this);
        }
    }
    public void clearAttributes() {
        for (Attribute attribute : new HashSet<>(currentAttributes)) {
            removeAttribute(attribute);
        }
    }
    public boolean hasAttribute(Attribute attribute) {
        return currentAttributes.contains(attribute);
    }
    public void adjustValuesToMax() {
        double maxJump = Horses.getInstance().getHorsesConfig().getCheatMaxJump();
        double maxSpeed = Horses.getInstance().getHorsesConfig().getCheatMaxSpeed();
        double maxHealth = Horses.getInstance().getHorsesConfig().getCheatMaxHealth();
        if (maxJump < this.jumpStrength || maxSpeed < this.speed || maxHealth < this.maxHealth) {
            if (this.getHorse() != null) {
                removeHorse();
            }
            if (this.jumpStrength > maxJump) {
                this.jumpStrength = maxJump;
            }
            if (this.speed > maxSpeed) {
                this.speed = maxSpeed;
            }
            if (this.maxHealth > maxHealth) {
                this.maxHealth = maxHealth;
                if (this.health > this.maxHealth) {
                    this.health = this.maxHealth;
                }
            }
            saveChanges();
        }
    }
    public void updateAttributes() {
        if (getHorse() == null) {
            return;
        }
        List<Attribute> attributes = new ArrayList<>();
        Inventory inv = getHorse().getInventory();
        ItemStack[] armor = {inv.getItem(0), inv.getItem(1)};
        for (ItemStack itemStack : armor) {
            if (itemStack != null && itemStack.getItemMeta() != null && itemStack.getItemMeta().hasLore()) {
                for (String loreI : itemStack.getItemMeta().getLore()) {
                    loreI = ChatColor.stripColor(loreI);
                    Attribute attribute = null;
                    if (loreI.contains("Trail: ")) {
                        String trailName = loreI.replace("Trail: ","").trim().toLowerCase();
                        attribute = new TrailAttribute(Horses.getInstance().getHorsesConfig().getTrails().get(trailName));
                    }else if (loreI.contains("Horse Speed")) {
                        int level = getLevelFromLore(loreI);
                        double speedAmount = level * plugin.getHorsesConfig().getSpeedPerLevel();
                        attribute = new HorseSpeedAttribute(speedAmount);
                    }else if (loreI.contains("Horse Jump")) {
                        int level = getLevelFromLore(loreI);
                        double jumpAmount = level * plugin.getHorsesConfig().getJumpPerLevel();
                        attribute = new HorseJumpAttribute(jumpAmount);
                    }else if (loreI.contains("Horse Health")) {
                        int level = getLevelFromLore(loreI);
                        attribute = new HorseHealthAttribute(level);
                    }
                    if (attribute != null) {
                        attributes.add(attribute);
                    }
                }
            }
        }
        for (Attribute attribute : attributes) {
            if (!hasAttribute(attribute)) {
                addAttribute(attribute);
            }
        }
        for (Attribute attribute : new HashSet<>(currentAttributes)) {
            if (!attributes.contains(attribute)) {
                removeAttribute(attribute);
            }
        }
        if (!attributes.isEmpty()) {
            getHorse().setBreed(false);
        }else if (getHorse().isAdult()) {
            getHorse().setBreed(true);
        }
    }
    private static int getLevelFromLore(String loreI) {
        return Integer.parseInt(loreI.split(" ")[0].replace("+",""));
    }

    public static PlayerHorse getFromEntity(Horse horse) {
        for (MetadataValue meta : horse.getMetadata("Horses.Ownership")) {
            if ((meta.getOwningPlugin() == Horses.getInstance()) && (meta.value().getClass() == PlayerHorse.class)) {
                return (PlayerHorse) meta.value();
            }
        }

        return null;
    }
}