package com.queso.niche.weapon;

import net.minecraft.ChatFormatting;

import java.util.Locale;

public enum WeaponAbility {
    MULTISHOT(ChatFormatting.YELLOW),
    RAPID_FIRE(ChatFormatting.GOLD),
    EXPLOSIVE(ChatFormatting.RED),
    INCENDIARY(ChatFormatting.GOLD),
    LIGHTNING(ChatFormatting.AQUA),
    PIERCING(ChatFormatting.WHITE),
    KNOCKBACK(ChatFormatting.GRAY),
    LIFESTEAL(ChatFormatting.DARK_RED),
    FROST(ChatFormatting.AQUA),
    POISON(ChatFormatting.DARK_GREEN),
    WITHER(ChatFormatting.DARK_GRAY),
    HOMING(ChatFormatting.LIGHT_PURPLE),
    TELEPORT(ChatFormatting.DARK_PURPLE),
    SONIC(ChatFormatting.DARK_AQUA),
    LEVITATION(ChatFormatting.WHITE),
    GLOWING(ChatFormatting.YELLOW),
    DARKNESS(ChatFormatting.DARK_GRAY),
    WEAKNESS(ChatFormatting.DARK_GRAY),
    RICOCHET(ChatFormatting.GRAY),
    STICKY(ChatFormatting.GOLD),
    REPEATER(ChatFormatting.LIGHT_PURPLE),
    FRUGAL(ChatFormatting.BLUE);

    private final ChatFormatting color;

    WeaponAbility(ChatFormatting color) {
        this.color = color;
    }

    public ChatFormatting color() {
        return color;
    }

    public int bit() {
        return 1 << this.ordinal();
    }

    public String id() {
        return name().toLowerCase(Locale.ROOT);
    }

    public String translationKey() {
        return "ability.niche." + id();
    }

    public static int toMask(Iterable<WeaponAbility> abilities) {
        int mask = 0;
        for (WeaponAbility a : abilities) {
            mask |= a.bit();
        }
        return mask;
    }

    public static boolean inMask(int mask, WeaponAbility a) {
        return (mask & a.bit()) != 0;
    }
}
