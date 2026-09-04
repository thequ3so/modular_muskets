package com.queso.niche.client;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;

public final class ReloadPose {

    private static final float MAX_TILT = 34.0F;
    private static final float APPROACH = 0.4F;

    private static float tilt = 0.0F;

    private ReloadPose() {}

    public static float update(boolean reloading) {
        float dt = Minecraft.getInstance().getDeltaTracker().getRealtimeDeltaTicks();
        float target = reloading ? MAX_TILT : 0.0F;
        tilt += (target - tilt) * Mth.clamp(APPROACH * dt, 0.0F, 1.0F);
        return tilt;
    }
}
