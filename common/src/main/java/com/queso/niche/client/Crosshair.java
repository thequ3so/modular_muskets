package com.queso.niche.client;

public final class Crosshair {

    public static final float MIN_GAP = 2.0F;
    public static final float SPREAD_SCALE = 4.0F;
    public static final float MOVE_SCALE = 22.0F;
    public static final float MOVE_MAX = 5.0F;
    public static final int TICK_LEN = 4;
    public static final int BORDER = 1;

    private Crosshair() {}

    public static int gap(float bloom, float moveSpread) {
        return Math.round(MIN_GAP + bloom + moveSpread);
    }

    public static int maxHalfExtent(float inaccuracy) {
        int maxGap = Math.round(MIN_GAP + inaccuracy * SPREAD_SCALE + MOVE_MAX);
        return maxGap + 1 + TICK_LEN + BORDER;
    }
}
