package com.queso.niche.weapon;

import java.util.Locale;

public enum MaterialTier {
    WOODEN   (b(0.0F, 0.0F, 0.0F,  0, 0, 0.0F),  s(0.0F, 0.0F,  0.0F,  0, 0, 0),  br(0.0F, 0.0F,  0.1F,  3, 0, 0)),
    FLINT    (b(1.5F, 0.15F,0.0F,  0, 0, 1.0F),  s(0.0F, 0.0F, -0.1F,  0, 0, 0),  br(0.0F, 0.0F,  0.05F, 2, 0, 0)),
    BONE     (b(0.5F, 0.0F, 0.0F,  0, 0, 0.0F),  s(0.0F, 0.0F, -0.3F, -2, 0, 0),  br(0.0F, 0.0F, -0.1F,  2, 0, 0)),
    STONE    (b(1.0F, 0.1F, 0.0F,  0, 0, 1.0F),  s(0.0F, 0.0F, -0.2F, -1, 0, 0),  br(0.0F, 0.0F,  0.0F,  3, 0, 0)),
    COPPER   (b(1.5F, 0.2F, 0.0F,  0, 0, 2.0F),  s(0.0F, 0.0F, -0.4F, -2, 0, 0),  br(0.0F, 0.0F, -0.2F,  4, 0, 0)),
    IRON     (b(3.0F, 0.3F, 0.0F,  0, 0, 4.0F),  s(0.0F, 0.0F, -0.7F, -3, 0, 0),  br(0.0F, 0.0F, -0.5F,  3, 0, 0)),
    GOLDEN   (b(1.0F, 0.5F, 0.0F, -6, 0, 2.0F),  s(0.0F, 0.0F, -0.3F, -8, 0, 0),  br(0.0F, 0.0F, -0.3F, -2, 0, 0)),
    DIAMOND  (b(5.0F, 0.5F, 0.0F,  0, 0, 6.0F),  s(0.0F, 0.0F, -1.2F, -4, 1, 0),  br(0.0F, 0.0F, -0.8F,  2, 0, 0)),
    NETHERITE(b(7.0F, 0.7F, 0.0F,  0, 0, 8.0F),  s(0.0F, 0.0F, -1.6F, -6, 1, 0),  br(0.0F, 0.0F, -1.0F,  2, 1, 0));

    private final PartStats barrel;
    private final PartStats stock;
    private final PartStats bracing;

    MaterialTier(PartStats barrel, PartStats stock, PartStats bracing) {
        this.barrel = barrel;
        this.stock = stock;
        this.bracing = bracing;
    }

    public PartStats stats(WeaponComponentType slot) {
        return switch (slot) {
            case BARREL -> barrel;
            case STOCK -> stock;
            case BRACING -> bracing;
            case AUGMENT -> PartStats.NONE;
        };
    }

    public String id() {
        return name().toLowerCase(Locale.ROOT);
    }

    private static PartStats b(float dmg, float vel, float inacc, int rl, int mag, float rng) {
        return new PartStats(dmg, vel, inacc, rl, mag, rng);
    }

    private static PartStats s(float dmg, float vel, float inacc, int rl, int mag, float rng) {
        return new PartStats(dmg, vel, inacc, rl, mag, rng);
    }

    private static PartStats br(float dmg, float vel, float inacc, int rl, int mag, float rng) {
        return new PartStats(dmg, vel, inacc, rl, mag, rng);
    }
}
