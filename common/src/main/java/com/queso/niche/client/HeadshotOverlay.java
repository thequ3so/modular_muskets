package com.queso.niche.client;

import net.minecraft.client.Minecraft;

public final class HeadshotOverlay {

    private static int triggerTick = Integer.MIN_VALUE;

    private HeadshotOverlay() {}

    public static void trigger() {
        Minecraft mc = Minecraft.getInstance();
        triggerTick = mc.player != null ? mc.player.tickCount : 0;
    }

    public static float age() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || triggerTick == Integer.MIN_VALUE) {
            return Float.MAX_VALUE;
        }
        float partial = mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
        return (mc.player.tickCount + partial) - triggerTick;
    }
}
