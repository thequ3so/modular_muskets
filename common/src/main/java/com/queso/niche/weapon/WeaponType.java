package com.queso.niche.weapon;

import java.util.List;
import java.util.Locale;

public enum WeaponType {
    MUSKET(new WeaponStats(9.0F, 3.2F, 2.0F, 45, 1, 18.0F));

    public static final List<WeaponComponentType> REQUIRED_COMPONENTS =
            List.of(WeaponComponentType.BARREL, WeaponComponentType.STOCK, WeaponComponentType.BRACING);

    private final WeaponStats base;

    WeaponType(WeaponStats base) {
        this.base = base;
    }

    public WeaponStats base() {
        return base;
    }

    public String id() {
        return name().toLowerCase(Locale.ROOT);
    }
}
