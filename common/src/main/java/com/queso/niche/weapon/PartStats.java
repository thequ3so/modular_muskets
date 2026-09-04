package com.queso.niche.weapon;

public record PartStats(
        float damage,
        float velocity,
        float inaccuracy,
        int reloadTicks,
        int magazine,
        float range) {

    public static final PartStats NONE = new PartStats(0.0F, 0.0F, 0.0F, 0, 0, 0.0F);

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private float damage;
        private float velocity;
        private float inaccuracy;
        private int reloadTicks;
        private int magazine;
        private float range;

        public Builder damage(float v) { this.damage = v; return this; }
        public Builder velocity(float v) { this.velocity = v; return this; }
        public Builder inaccuracy(float v) { this.inaccuracy = v; return this; }
        public Builder reloadTicks(int v) { this.reloadTicks = v; return this; }
        public Builder magazine(int v) { this.magazine = v; return this; }
        public Builder range(float v) { this.range = v; return this; }

        public PartStats build() {
            return new PartStats(damage, velocity, inaccuracy, reloadTicks, magazine, range);
        }
    }
}
