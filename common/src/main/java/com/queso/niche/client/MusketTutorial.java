package com.queso.niche.client;

import com.queso.niche.NicheConfig;
import net.minecraft.client.Minecraft;

public final class MusketTutorial {

    private static final int DURATION = 140;
    private static final int FADE = 25;

    private static int startTick = Integer.MIN_VALUE;

    private MusketTutorial() {}

    public static void maybeTrigger() {
        if (NicheConfig.tutorialShown()) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        startTick = mc.player != null ? mc.player.tickCount : 0;
        NicheConfig.markTutorialShown();
    }

    public static float alpha() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || startTick == Integer.MIN_VALUE) {
            return 0.0F;
        }
        float partial = mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
        float age = (mc.player.tickCount + partial) - startTick;
        if (age < 0.0F || age >= DURATION) {
            return 0.0F;
        }
        float remain = DURATION - age;
        return remain < FADE ? remain / FADE : 1.0F;
    }
}
