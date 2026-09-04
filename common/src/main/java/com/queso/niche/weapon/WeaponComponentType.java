package com.queso.niche.weapon;

import java.util.Locale;

public enum WeaponComponentType {
    BARREL,
    STOCK,
    BRACING,
    AUGMENT;

    public String id() {
        return name().toLowerCase(Locale.ROOT);
    }

    public String translationKey() {
        return "niche.component_type." + id();
    }
}
