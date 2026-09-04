package com.queso.niche.weapon;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class AimServer {

    private static final Map<UUID, Integer> AIM_TICKS = new HashMap<>();

    private AimServer() {}

    public static void update(Player player, boolean aiming) {
        UUID id = player.getUUID();
        if (aiming) {
            AIM_TICKS.merge(id, 1, (cur, inc) -> Math.min(cur + inc, Aiming.MAX_AIM_TICKS));
            player.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 5, 1, false, false, false));
        } else {
            AIM_TICKS.remove(id);
        }
    }

    public static int aimTicks(Player player) {
        return AIM_TICKS.getOrDefault(player.getUUID(), 0);
    }
}
