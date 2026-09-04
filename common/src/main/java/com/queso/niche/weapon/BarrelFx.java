package com.queso.niche.weapon;

import org.jspecify.annotations.Nullable;

public record BarrelFx(WeaponTrail trail, @Nullable WeaponSound sound, float recoil) {

    public static final BarrelFx DEFAULT = new BarrelFx(WeaponTrail.NONE, null, 1.0F);

    public BarrelFx withTrail(WeaponTrail trail) {
        return new BarrelFx(trail, sound, recoil);
    }

    public BarrelFx withSound(WeaponSound sound) {
        return new BarrelFx(trail, sound, recoil);
    }

    public BarrelFx withRecoil(float recoil) {
        return new BarrelFx(trail, sound, recoil);
    }
}
