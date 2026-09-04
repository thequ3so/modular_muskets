package com.queso.niche.weapon;

import net.minecraft.world.entity.player.Player;

public final class Aiming {

    public static final int STEADY_TICKS = 40;
    public static final int MAX_AIM_TICKS = 140;
    public static final int SWAY_RAMP_TICKS = 60;

    private Aiming() {}

    public static boolean isAiming(Player player) {
        return player.isShiftKeyDown() && player.getMainHandItem().getItem() instanceof ModularWeaponItem;
    }
}
