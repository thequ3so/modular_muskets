package com.queso.niche.weapon;

import net.minecraft.util.Mth;

public record WeaponStats(
        float damage,
        float velocity,
        float inaccuracy,
        int reloadTicks,
        int magazine,
        float range) {

    public WeaponStats apply(PartStats p) {
        return new WeaponStats(
                Math.max(0.0F, damage + p.damage()),
                Mth.clamp(velocity + p.velocity(), 0.1F, 10.0F),
                Math.max(0.0F, inaccuracy + p.inaccuracy()),
                Math.max(1, reloadTicks + p.reloadTicks()),
                Math.max(1, magazine + p.magazine()),
                Math.max(0.0F, range + p.range()));
    }
}
