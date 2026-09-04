package com.queso.niche.registry;

import com.queso.niche.Constants;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public final class ModParticles {

    private static final Map<ResourceKey<ParticleType<?>>, ParticleType<?>> TYPES = new LinkedHashMap<>();

    public static final SimpleParticleType MUSKETBALL_TRAIL = create("musketball_trail");
    public static final SimpleParticleType MUSKET_POOF = create("musket_poof");

    private static SimpleParticleType create(String name) {
        SimpleParticleType type = new SimpleParticleType(false) {};
        TYPES.put(ResourceKey.create(Registries.PARTICLE_TYPE,
                Identifier.fromNamespaceAndPath(Constants.MOD_ID, name)), type);
        return type;
    }

    public static void register(BiConsumer<ResourceKey<ParticleType<?>>, ParticleType<?>> registrar) {
        TYPES.forEach(registrar);
    }

    private ModParticles() {}
}
