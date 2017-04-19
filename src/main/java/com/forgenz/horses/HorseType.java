package com.forgenz.horses;

import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Horse.Variant;

import static org.bukkit.entity.Horse.Variant.*;

public enum HorseType {
    White(Horse.Color.WHITE),
    Creamy(Horse.Color.CREAMY),
    Chestnut(Horse.Color.CHESTNUT),
    Brown(Horse.Color.BROWN),
    Black(Horse.Color.BLACK),
    Gray(Horse.Color.GRAY),
    DarkBrown(Horse.Color.DARK_BROWN),

    BlazeWhite(Horse.Color.WHITE, Horse.Style.WHITE),
    BlazeCreamy(Horse.Color.CREAMY, Horse.Style.WHITE),
    BlazeChestnut(Horse.Color.CHESTNUT, Horse.Style.WHITE),
    BlazeBrown(Horse.Color.BROWN, Horse.Style.WHITE),
    BlazeBlack(Horse.Color.BLACK, Horse.Style.WHITE),
    BlazeGray(Horse.Color.GRAY, Horse.Style.WHITE),
    BlazeDarkBrown(Horse.Color.DARK_BROWN, Horse.Style.WHITE),

    PaintWhite(Horse.Color.WHITE, Horse.Style.WHITEFIELD),
    PaintCreamy(Horse.Color.CREAMY, Horse.Style.WHITEFIELD),
    PaintChestnut(Horse.Color.CHESTNUT, Horse.Style.WHITEFIELD),
    PaintBrown(Horse.Color.BROWN, Horse.Style.WHITEFIELD),
    PaintBlack(Horse.Color.BLACK, Horse.Style.WHITEFIELD),
    PaintGray(Horse.Color.GRAY, Horse.Style.WHITEFIELD),
    PaintDarkBrown(Horse.Color.DARK_BROWN, Horse.Style.WHITEFIELD),

    LeopardWhite(Horse.Color.WHITE, Horse.Style.WHITE_DOTS),
    LeopardCreamy(Horse.Color.CREAMY, Horse.Style.WHITE_DOTS),
    LeopardChestnut(Horse.Color.CHESTNUT, Horse.Style.WHITE_DOTS),
    LeopardBrown(Horse.Color.BROWN, Horse.Style.WHITE_DOTS),
    LeopardBlack(Horse.Color.BLACK, Horse.Style.WHITE_DOTS),
    LeopardGray(Horse.Color.GRAY, Horse.Style.WHITE_DOTS),
    LeopardDarkBrown(Horse.Color.DARK_BROWN, Horse.Style.WHITE_DOTS),

    SootyWhite(Horse.Color.WHITE, Horse.Style.BLACK_DOTS),
    SootyCreamy(Horse.Color.CREAMY, Horse.Style.BLACK_DOTS),
    SootyChestnut(Horse.Color.CHESTNUT, Horse.Style.BLACK_DOTS),
    SootyBrown(Horse.Color.BROWN, Horse.Style.BLACK_DOTS),
    SootyBlack(Horse.Color.BLACK, Horse.Style.BLACK_DOTS),
    SootyGray(Horse.Color.GRAY, Horse.Style.BLACK_DOTS),
    SootyDarkBrown(Horse.Color.DARK_BROWN, Horse.Style.BLACK_DOTS),

    Donkey(DONKEY),
    Mule(MULE),
    Undead(UNDEAD_HORSE),
    Skeleton(SKELETON_HORSE);

    private final String permission;
    private final String tamePermission;
    private final Variant variant;
    private final Horse.Color colour;
    private final Horse.Style style;

    private HorseType(Variant variant) {
        this(variant, null, null);
    }


    private HorseType(Horse.Color colour) {
        this(HORSE, colour, Horse.Style.NONE);
    }

    private HorseType(Horse.Color colour, Horse.Style style) {
        this(HORSE, colour, style);
    }

    private HorseType(Variant variant, Horse.Color colour, Horse.Style style) {
        this.variant = variant;
        this.colour = colour;
        this.style = style;

        this.permission = ("horses.type." + toString().toLowerCase());
        this.tamePermission = ("horses.tame."+toString().toLowerCase());
    }

    public String getTamePermission() {
        return tamePermission;
    }

    public Variant getVariant() {
        return this.variant;
    }

    public Horse.Color getColour() {
        return this.colour;
    }

    public Horse.Style getStyle() {
        return this.style;
    }

    public String getPermission() {
        return this.permission;
    }

    public void setHorseType(AbstractHorse horse2) {
        //horse.setVariant(getVariant());

        if (horse2 instanceof Horse) {
            Horse horse = (Horse) horse2;
            if (getColour() != null)
                horse.setColor(getColour());
            if (getStyle() != null)
                horse.setStyle(getStyle());
        }
    }

    public static HorseType closeValueOf(String like) {
        like = like.toLowerCase();

        for (HorseType type : values()) {
            if (type.toString().toLowerCase().startsWith(like)) {
                return type;
            }
        }
        return null;
    }

    public static HorseType exactValueOf(String typeStr) {
        for (HorseType type : values()) {
            if (type.toString().equalsIgnoreCase(typeStr)) {
                return type;
            }
        }
        return null;
    }

    public static HorseType valueOf(Horse horse) {
        switch (horse.getVariant()) {
            case HORSE:
                HorseType[] a = values();
                Horse.Color colour = horse.getColor();
                for (int i = horse.getStyle().ordinal() * Horse.Color.values().length; i < a.length; i++)
                    if (a[i].getColour() == colour)
                        return a[i];
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
}