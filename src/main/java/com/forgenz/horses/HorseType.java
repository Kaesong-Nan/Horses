package com.forgenz.horses;

import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Horse.Color;
import org.bukkit.entity.Horse.Style;
import org.bukkit.entity.Horse.Variant;

import static org.bukkit.entity.Horse.Variant.*;

@SuppressWarnings({"unused", "deprecation"})
public enum HorseType {
    White(Color.WHITE),
    Creamy(Color.CREAMY),
    Chestnut(Color.CHESTNUT),
    Brown(Color.BROWN),
    Black(Color.BLACK),
    Gray(Color.GRAY),
    DarkBrown(Color.DARK_BROWN),
    
    BlazeWhite(Color.WHITE, Style.WHITE),
    BlazeCreamy(Color.CREAMY, Style.WHITE),
    BlazeChestnut(Color.CHESTNUT, Style.WHITE),
    BlazeBrown(Color.BROWN, Style.WHITE),
    BlazeBlack(Color.BLACK, Style.WHITE),
    BlazeGray(Color.GRAY, Style.WHITE),
    BlazeDarkBrown(Color.DARK_BROWN, Style.WHITE),
    
    PaintWhite(Color.WHITE, Style.WHITEFIELD),
    PaintCreamy(Color.CREAMY, Style.WHITEFIELD),
    PaintChestnut(Color.CHESTNUT, Style.WHITEFIELD),
    PaintBrown(Color.BROWN, Style.WHITEFIELD),
    PaintBlack(Color.BLACK, Style.WHITEFIELD),
    PaintGray(Color.GRAY, Style.WHITEFIELD),
    PaintDarkBrown(Color.DARK_BROWN, Style.WHITEFIELD),
    
    LeopardWhite(Color.WHITE, Style.WHITE_DOTS),
    LeopardCreamy(Color.CREAMY, Style.WHITE_DOTS),
    LeopardChestnut(Color.CHESTNUT, Style.WHITE_DOTS),
    LeopardBrown(Color.BROWN, Style.WHITE_DOTS),
    LeopardBlack(Color.BLACK, Style.WHITE_DOTS),
    LeopardGray(Color.GRAY, Style.WHITE_DOTS),
    LeopardDarkBrown(Color.DARK_BROWN, Style.WHITE_DOTS),
    
    SootyWhite(Color.WHITE, Style.BLACK_DOTS),
    SootyCreamy(Color.CREAMY, Style.BLACK_DOTS),
    SootyChestnut(Color.CHESTNUT, Style.BLACK_DOTS),
    SootyBrown(Color.BROWN, Style.BLACK_DOTS),
    SootyBlack(Color.BLACK, Style.BLACK_DOTS),
    SootyGray(Color.GRAY, Style.BLACK_DOTS),
    SootyDarkBrown(Color.DARK_BROWN, Style.BLACK_DOTS),
    
    Donkey(DONKEY),
    Mule(MULE),
    Undead(UNDEAD_HORSE),
    Skeleton(SKELETON_HORSE);
    
    private final String permission;
    private final String tamePermission;
    private final Variant variant;
    private final Color colour;
    private final Style style;
    
    HorseType(final Variant variant) {
        this(variant, null, null);
    }
    
    HorseType(final Color colour) {
        this(HORSE, colour, Style.NONE);
    }
    
    HorseType(final Color colour, final Style style) {
        this(HORSE, colour, style);
    }
    
    HorseType(final Variant variant, final Color colour, final Style style) {
        this.variant = variant;
        this.colour = colour;
        this.style = style;
        
        permission = "horses.type." + toString().toLowerCase();
        tamePermission = "horses.tame." + toString().toLowerCase();
    }
    
    public static HorseType closeValueOf(String like) {
        like = like.toLowerCase();
        
        for(final HorseType type : values()) {
            if(type.toString().toLowerCase().startsWith(like)) {
                return type;
            }
        }
        return null;
    }
    
    public static HorseType exactValueOf(final String typeStr) {
        for(final HorseType type : values()) {
            if(type.toString().equalsIgnoreCase(typeStr)) {
                return type;
            }
        }
        return null;
    }
    
    public static HorseType valueOf(final AbstractHorse horseIn) {
        switch(horseIn.getVariant()) {
            case HORSE:
                final Horse horse = (Horse) horseIn;
                final HorseType[] a = values();
                final Color colour = horse.getColor();
                for(int i = horse.getStyle().ordinal() * Color.values().length; i < a.length; i++) {
                    if(a[i].getColour() == colour) {
                        return a[i];
                    }
                }
            case DONKEY:
                return Donkey;
            case MULE:
                return Mule;
            case UNDEAD_HORSE:
                return Undead;
            case SKELETON_HORSE:
                return Skeleton;
        }
        return null;
    }
    
    public String getTamePermission() {
        return tamePermission;
    }
    
    public Variant getVariant() {
        return variant;
    }
    
    public Color getColour() {
        return colour;
    }
    
    public Style getStyle() {
        return style;
    }
    
    public String getPermission() {
        return permission;
    }
    
    public void setHorseType(final AbstractHorse horse2) {
        //horse.setVariant(getVariant());
        
        if(horse2 instanceof Horse) {
            final Horse horse = (Horse) horse2;
            if(getColour() != null) {
                horse.setColor(getColour());
            }
            if(getStyle() != null) {
                horse.setStyle(getStyle());
            }
        }
    }
}