package com.queso.niche.registry;

import com.queso.niche.Constants;
import com.queso.niche.entity.MusketBall;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

public final class ModEntities {

    private static final Map<ResourceKey<EntityType<?>>, EntityType<?>> ENTITIES = new LinkedHashMap<>();

    public static final EntityType<MusketBall> MUSKET_BALL = register("musket_ball", key ->
            EntityType.Builder.<MusketBall>of(MusketBall::new, MobCategory.MISC)
                    .noLootTable()
                    .sized(0.2F, 0.2F)
                    .clientTrackingRange(6)
                    .updateInterval(2)
                    .build(key));

    private static <T extends Entity> EntityType<T> register(String name, Function<ResourceKey<EntityType<?>>, EntityType<T>> factory) {
        ResourceKey<EntityType<?>> key = ResourceKey.create(
                Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(Constants.MOD_ID, name));
        EntityType<T> type = factory.apply(key);
        ENTITIES.put(key, type);
        return type;
    }

    public static void register(BiConsumer<ResourceKey<EntityType<?>>, EntityType<?>> registrar) {
        ENTITIES.forEach(registrar);
    }

    private ModEntities() {}
}
