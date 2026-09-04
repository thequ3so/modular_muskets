package com.queso.niche.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;

public class NicheParticle extends SingleQuadParticle {

    private final SpriteSet sprites;

    protected NicheParticle(ClientLevel level, double x, double y, double z,
                            double xa, double ya, double za, SpriteSet sprites,
                            float size, int lifetime, float gravity, float friction, int startAge, float roll) {
        super(level, x, y, z, sprites.first());
        this.sprites = sprites;
        this.quadSize = size;
        this.lifetime = lifetime;
        this.gravity = gravity;
        this.friction = friction;
        this.xd = xa;
        this.yd = ya;
        this.zd = za;
        this.age = startAge;
        this.roll = roll;
        this.oRoll = roll;
        this.setSpriteFromAge(sprites);
    }

    @Override
    public void tick() {
        super.tick();
        this.setSpriteFromAge(this.sprites);
    }

    @Override
    protected Layer getLayer() {
        return Layer.TRANSLUCENT;
    }

    public static final class TrailProvider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public TrailProvider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType options, ClientLevel level,
                                       double x, double y, double z, double xa, double ya, double za, RandomSource random) {
            float roll = (float) (random.nextDouble() * Math.PI * 2.0);
            return new NicheParticle(level, x, y, z, xa, ya, za, sprites, 0.25F, 10, 0.0F, 0.86F, 0, roll);
        }
    }

    public static final class PoofProvider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public PoofProvider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType options, ClientLevel level,
                                       double x, double y, double z, double xa, double ya, double za, RandomSource random) {
            double vx = xa + (random.nextDouble() - 0.5) * 0.08;
            double vz = za + (random.nextDouble() - 0.5) * 0.08;
            double vy = ya + 0.07 + random.nextDouble() * 0.06;
            int lifetime = 34 + random.nextInt(20);
            int startAge = random.nextInt(12);
            float roll = (float) ((random.nextDouble() - 0.5) * (Math.PI / 4.0));
            return new NicheParticle(level, x, y, z, vx, vy, vz, sprites, 0.4F, lifetime, -0.02F, 0.94F, startAge, roll);
        }
    }
}
