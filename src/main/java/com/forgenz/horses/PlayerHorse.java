package com.forgenz.horses;

import com.forgenz.forgecore.v1_0.ForgeCore;
import com.forgenz.horses.attribute.*;
import com.forgenz.horses.util.HorseSpeedUtil;
import com.voxmc.voxlib.util.VoxEffects;
import org.bukkit.*;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.ChestedHorse;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse.Variant;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.regex.Pattern;

@SuppressWarnings({"deprecation", "WeakerAccess", "unused"})
public class PlayerHorse
        implements ForgeCore {
    public static final Pattern FORMATTING_CODES_PATTERN = Pattern.compile("&[klmnor]", Pattern.CASE_INSENSITIVE);
    private static final String OWNERSHIP_METADATA_KEY = "Horses.Ownership";
    private static final Location cacheLoc = new Location(null, 0.0D, 0.0D, 0.0D);
    private final Horses plugin;
    private final Stable stable;
    private final HorseType type;
    private final ArrayList<ItemStack> inventory = new ArrayList<>();
    @SuppressWarnings("PublicField")
    public Set<Attribute> currentAttributes = new HashSet<>();
    private int id;
    private AbstractHorse horse;
    private long lastDeath;
    private String name;
    private String displayName;
    private double maxHealth;
    private double health;
    private double speed;
    private double jumpStrength;
    private boolean hasChest;
    private boolean renamable;
    
    public PlayerHorse(final Horses plugin, final Stable stable, final String name, final HorseType type, final double maxHealth, final double health, final double speed, final double jumpStrength, final AbstractHorse horse) {
        this(plugin, stable, name, type, maxHealth, health, speed, jumpStrength, horse, -1);
    }
    
    public PlayerHorse(final Horses plugin, final Stable stable, final String name, final HorseType type, double maxHealth, double health, final double speed, double jumpStrength, final AbstractHorse horse, final int id) {
        this.plugin = plugin;
        this.stable = stable;
        this.id = id;
        
        displayName = ChatColor.translateAlternateColorCodes('&', name).replaceAll("&", "").replaceAll(" ", "_").replaceAll("\\.", "");
        this.name = ChatColor.stripColor(displayName);
        
        this.type = type;
        
        if(health <= 0.0D) {
            health = getPlugin().getHorsesConfig().getHorseTypeConfig(null, type).horseHp;
        }
        if(health > maxHealth || maxHealth <= 0.0D) {
            maxHealth = health;
        }
        this.maxHealth = maxHealth;
        this.health = health;
        
        if(getPlugin().getHorsesConfig().fixZeroJumpStrength && jumpStrength <= 0.0D) {
            jumpStrength = 0.7D;
        }
        this.speed = speed;
        this.jumpStrength = jumpStrength;
        
        this.horse = horse;
        
        if(this.horse != null) {
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
    
    private static int getLevelFromLore(final String loreI) {
        return Integer.parseInt(loreI.split(" ")[0].replace("+", ""));
    }
    
    @SuppressWarnings("TypeMayBeWeakened")
    public static PlayerHorse getFromEntity(final AbstractHorse horse) {
        for(final MetadataValue meta : horse.getMetadata("Horses.Ownership")) {
            if(meta.getOwningPlugin() == Horses.getInstance() && meta.value().getClass() == PlayerHorse.class) {
                return (PlayerHorse) meta.value();
            }
        }
        
        return null;
    }
    
    public Stable getStable() {
        return stable;
    }
    
    public Horses getPlugin() {
        return plugin;
    }
    
    public boolean isRenamable() {
        return renamable && !plugin.getHorsesConfig().getBlackListNames().contains(name);
    }
    
    public void setRenamable(final boolean renamable) {
        this.renamable = renamable;
    }
    
    public AbstractHorse getHorse() {
        return horse;
    }
    
    public String getName() {
        return name;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public HorseType getType() {
        return type;
    }
    
    public long getLastDeath() {
        return lastDeath;
    }
    
    public void setLastDeath(final long time) {
        lastDeath = time;
    }
    
    public int getId() {
        return id;
    }
    
    public void setId(final int id) {
        this.id = id;
    }
    
    public double getMaxHealth() {
        if(horse != null && horse.isValid()) {
            maxHealth = horse.getMaxHealth();
        }
        return maxHealth;
    }
    
    public void setMaxHealth(final double amount) {
        maxHealth = amount;
        
        if(horse != null) {
            horse.setMaxHealth(amount);
        }
    }
    
    public double getHealth() {
        if(horse != null && horse.isValid()) {
            health = horse.getHealth();
        }
        return health;
    }
    
    public void setHealth(final double amount) {
        health = amount;
        
        if(horse != null) {
            horse.setHealth(amount);
        }
    }
    
    public double getHealEstimate(final float amount) {
        if(getMaxHealth() < getHealth() + amount) {
            return maxHealth - health;
        }
        
        return amount;
    }
    
    @SuppressWarnings("UnusedReturnValue")
    public double addHealth(double amount) {
        if(getMaxHealth() < getHealth() + amount) {
            amount = maxHealth - health;
        }
        
        health += amount;
        
        if(horse != null && horse.isValid()) {
            horse.setHealth(health);
        }
        
        return amount;
    }
    
    public void addMaxHealth(final double amount) {
        maxHealth = getMaxHealth() + amount;
        health = getHealth() + amount;
        
        if(horse != null && horse.isValid()) {
            horse.setMaxHealth(maxHealth);
            horse.setHealth(health);
        }
    }
    
    public double getJumpStrength() {
        if(horse != null) {
            jumpStrength = horse.getJumpStrength();
        }
        
        return jumpStrength;
    }
    
    public void setJumpStrength(final double jumpStrength) {
        this.jumpStrength = jumpStrength;
    }
    
    public double getSpeed() {
        if(horse != null) {
            speed = HorseSpeedUtil.getHorseSpeed(horse);
        }
        
        return speed;
    }
    
    public void setSpeed(final double speed) {
        this.speed = speed;
    }
    
    public void setSaddle(final ItemStack saddle) {
        setItem(0, saddle);
    }
    
    public void setHasChest(final boolean hasChest) {
        if(type != HorseType.Mule && type != HorseType.Donkey) {
            return;
        }
        this.hasChest = hasChest;
    }
    
    public boolean hasChest() {
        if(type != HorseType.Mule && type != HorseType.Donkey) {
            return false;
        }
        if(horse != null && horse instanceof ChestedHorse) {
            hasChest = ((ChestedHorse) horse).isCarryingChest();
        }
        
        return hasChest;
    }
    
    public void setArmour(final Material material) {
        setItem(1, new ItemStack(material));
    }
    
    public void setArmour(final ItemStack itemStack) {
        setItem(1, itemStack);
    }
    
    public ItemStack getItem(final int i) {
        try {
            if(inventory.size() <= i && i < 0) {
                return null;
            }
            if(horse != null) {
                ItemStack item = horse.getInventory().getItem(i);
                if(item != null && item.getType() == Material.AIR) {
                    item = null;
                }
                inventory.set(i, item);
            }
            
            return inventory.get(i);
        } catch(final Exception e) {
            return null;
        }
    }
    
    public void setItem(final int i, final ItemStack item) {
        if(i < 0) {
            return;
        }
        if(inventory.size() <= i) {
            for(int index = inventory.size(); index < i; index++) {
                inventory.add(null);
            }
            inventory.add(item);
        } else {
            inventory.set(i, item);
        }
    }
    
    public ItemStack[] getItems() {
        if(horse != null) {
            final ItemStack[] items = horse.getInventory().getContents();
            
            inventory.clear();
            Collections.addAll(inventory, items);
            return items;
        }
        
        return inventory.toArray(new ItemStack[inventory.size()]);
    }
    
    public void setItems(final ItemStack[] items) {
        inventory.clear();
        inventory.ensureCapacity(items.length);
        
        Collections.addAll(inventory, items);
    }
    
    public void removeHorse() {
        if(horse != null) {
            if(!horse.isDead()) {
                health = horse.getHealth();
            }
            
            getItems();
            hasChest();
            clearAttributes();
            if(horse.isDead()) {
                if(getPlugin().getHorsesConfig().getPermConfig(getStable().getPlayerOwner()).keepEquipmentOnDeath) {
                    horse.getInventory().clear();
                    if(horse instanceof ChestedHorse) {
                        ((ChestedHorse) horse).setCarryingChest(false);
                    }
                } else {
                    inventory.clear();
                    hasChest = false;
                }
            }
            if(horse.getLocation() != null) {
                playSound(horse.getLocation());
                final VoxEffects voxEffects = plugin.getHorsesConfig().getDismisEffects();
                if(voxEffects != null) {
                    voxEffects.play(horse.getLocation());
                }
            }
            horse.remove();
            
            stable.removeActiveHorse(this);
            
            horse.removeMetadata("Horses.Ownership", getPlugin());
            horse = null;
        }
        
        saveChanges();
    }
    
    @SuppressWarnings("UnusedReturnValue")
    public boolean spawnHorse(final Player player) {
        if(!player.getName().equals(getStable().getOwner())) {
            return false;
        }
        
        if(horse != null && horse.isValid()) {
            horse.teleport(player);
            return true;
        }
        
        if(getStable().getActiveHorse() != null) {
            getStable().getActiveHorse().removeHorse();
        }
        
        final Location loc = player.getLocation(cacheLoc);
        
        if(getPlugin().getHorsesConfig().getPermConfig(getStable().getPlayerOwner()).bypassSpawnProtection) {
            getPlugin().getHorseSpawnListener().setSpawning();
        }
        final AbstractHorse horse;
        if(getType() == HorseType.Skeleton) {
            horse = (AbstractHorse) loc.getWorld().spawnEntity(loc, EntityType.SKELETON_HORSE);
        } else if(getType() == HorseType.Undead) {
            horse = (AbstractHorse) loc.getWorld().spawnEntity(loc, EntityType.ZOMBIE_HORSE);
        } else if(getType() == HorseType.Mule) {
            horse = (ChestedHorse) loc.getWorld().spawnEntity(loc, EntityType.MULE);
        } else if(getType() == HorseType.Donkey) {
            horse = (ChestedHorse) loc.getWorld().spawnEntity(loc, EntityType.MULE);
        } else {
            horse = (AbstractHorse) loc.getWorld().spawnEntity(loc, EntityType.HORSE);
        }
        horse.setRemoveWhenFarAway(false);
        
        getType().setHorseType(horse);
        
        horse.setAdult();
        horse.setTamed(true);
        horse.setOwner(getStable().getPlayerOwner());
        
        if(hasChest() && horse instanceof ChestedHorse) {
            final ChestedHorse chestedHorse = (ChestedHorse) horse;
            chestedHorse.setCarryingChest(true);
        }
        final Inventory inv = horse.getInventory();
        
        final ItemStack[] items = inv.getContents();
        for(int i = 2; i < items.length; i++) {
            if(i >= inventory.size()) {
                items[i] = null;
            } else {
                items[i] = inventory.get(i);
            }
        }
        inv.setContents(items);
        
        if(horse instanceof AbstractHorse) {
            @SuppressWarnings("TypeMayBeWeakened") final AbstractHorse horse2 = (AbstractHorse) horse;
            final Inventory horseInventory = horse2.getInventory();
            if(horseInventory != null) {
                if(!inventory.isEmpty()) {
                    horseInventory.setItem(0, inventory.get(0));
                }
                if(inventory.size() > 1) {
                    horseInventory.setItem(1, inventory.get(1));
                }
            }
        }
        
        horse.setCustomName(displayName);
        horse.setCustomNameVisible(true);
        
        horse.setMaxHealth(maxHealth);
        if(health > maxHealth) {
            health = maxHealth;
        }
        horse.setHealth(health);
        HorseSpeedUtil.setHorseSpeed(horse, getSpeed());
        horse.setJumpStrength(getJumpStrength());
        
        horse.setTarget(player);
        horse.setMetadata("Horses.Ownership", new FixedMetadataValue(getPlugin(), this));
        playSound(loc);
        final VoxEffects voxEffects = plugin.getHorsesConfig().getSpawnEffects();
        if(voxEffects != null) {
            voxEffects.play(horse.getLocation());
        }
        getStable().setActiveHorse(this);
        
        this.horse = horse;
        new BukkitRunnable() {
            @Override
            public void run() {
                updateAttributes();
            }
        }.runTaskLater(plugin, 1);
        
        return true;
    }
    
    public void playSound(final Location loc) {
        final World world = loc.getWorld();
        final Variant variant = type.getVariant();
        if(variant == Variant.DONKEY) {
            world.playSound(loc, Sound.ENTITY_DONKEY_AMBIENT, 1, 1);
        } else if(variant == Variant.SKELETON_HORSE) {
            world.playSound(loc, Sound.ENTITY_SKELETON_HORSE_AMBIENT, 1, 1);
        } else if(variant == Variant.UNDEAD_HORSE) {
            world.playSound(loc, Sound.ENTITY_ZOMBIE_HORSE_AMBIENT, 1, 1);
        } else {
            world.playSound(loc, Sound.ENTITY_HORSE_AMBIENT, 1, 1);
        }
    }
    
    public void saveChanges() {
        getPlugin().getHorseDatabase().saveHorse(this);
    }
    
    public boolean deleteHorse() {
        if(horse != null) {
            horse.remove();
            getStable().removeActiveHorse(this);
        }
        
        return stable.deleteHorse(this);
    }
    
    public void rename(final String name) {
        displayName = ChatColor.translateAlternateColorCodes('&', name).replaceAll("&", "").replaceAll(" ", "_");
        this.name = ChatColor.stripColor(displayName);
        
        if(horse != null && horse.isValid()) {
            horse.setCustomName(displayName);
        }
        
        saveChanges();
    }
    
    public void removeAttribute(final Attribute attribute) {
        if(currentAttributes.contains(attribute)) {
            currentAttributes.remove(attribute);
        }
        if(attribute instanceof BuffAttribute) {
            ((BuffAttribute) attribute).onRemove(this);
        }
    }
    
    public void addAttribute(final Attribute attribute) {
        if(currentAttributes.contains(attribute)) {
            return;
        }
        currentAttributes.add(attribute);
        if(attribute instanceof BuffAttribute) {
            ((BuffAttribute) attribute).onAdd(this);
        }
    }
    
    public void clearAttributes() {
        for(final Attribute attribute : new HashSet<>(currentAttributes)) {
            removeAttribute(attribute);
        }
    }
    
    public boolean hasAttribute(final Attribute attribute) {
        return currentAttributes.contains(attribute);
    }
    
    public void adjustValuesToMax() {
        final double maxJump = Horses.getInstance().getHorsesConfig().getCheatMaxJump();
        final double maxSpeed = Horses.getInstance().getHorsesConfig().getCheatMaxSpeed();
        final double maxHealth = Horses.getInstance().getHorsesConfig().getCheatMaxHealth();
        if(maxJump < jumpStrength || maxSpeed < speed || maxHealth < this.maxHealth) {
            if(getHorse() != null) {
                removeHorse();
            }
            if(jumpStrength > maxJump) {
                jumpStrength = maxJump;
            }
            if(speed > maxSpeed) {
                speed = maxSpeed;
            }
            if(this.maxHealth > maxHealth) {
                this.maxHealth = maxHealth;
                if(health > this.maxHealth) {
                    health = this.maxHealth;
                }
            }
            saveChanges();
        }
    }
    
    public void updateAttributes() {
        if(getHorse() == null) {
            return;
        }
        final Collection<Attribute> attributes = new ArrayList<>();
        final Inventory inv = getHorse().getInventory();
        final ItemStack[] armor = {inv.getItem(0), inv.getItem(1)};
        for(final ItemStack itemStack : armor) {
            if(itemStack != null && itemStack.getItemMeta() != null && itemStack.getItemMeta().hasLore()) {
                for(String loreI : itemStack.getItemMeta().getLore()) {
                    loreI = ChatColor.stripColor(loreI);
                    Attribute attribute = null;
                    if(loreI.contains("Trail: ")) {
                        final String trailName = loreI.replace("Trail: ", "").trim().toLowerCase();
                        attribute = new TrailAttribute(Horses.getInstance().getHorsesConfig().getTrails().get(trailName));
                    } else if(loreI.contains("Horse Speed")) {
                        final int level = getLevelFromLore(loreI);
                        final double speedAmount = level * plugin.getHorsesConfig().getSpeedPerLevel();
                        attribute = new HorseSpeedAttribute(speedAmount);
                    } else if(loreI.contains("Horse Jump")) {
                        final int level = getLevelFromLore(loreI);
                        final double jumpAmount = level * plugin.getHorsesConfig().getJumpPerLevel();
                        attribute = new HorseJumpAttribute(jumpAmount);
                    } else if(loreI.contains("Horse Health")) {
                        final int level = getLevelFromLore(loreI);
                        attribute = new HorseHealthAttribute(level);
                    }
                    if(attribute != null) {
                        attributes.add(attribute);
                    }
                }
            }
        }
        for(final Attribute attribute : attributes) {
            if(!hasAttribute(attribute)) {
                addAttribute(attribute);
            }
        }
        for(final Attribute attribute : new HashSet<>(currentAttributes)) {
            if(!attributes.contains(attribute)) {
                removeAttribute(attribute);
            }
        }
        if(!attributes.isEmpty()) {
            getHorse().setBreed(false);
        } else if(getHorse().isAdult()) {
            getHorse().setBreed(true);
        }
    }
}