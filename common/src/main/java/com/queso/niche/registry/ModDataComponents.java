package com.queso.niche.registry;

import com.queso.niche.Constants;
import com.queso.niche.weapon.WeaponBuild;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.component.ChargedProjectiles;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.UnaryOperator;

public final class ModDataComponents {

    private static final Map<ResourceKey<DataComponentType<?>>, DataComponentType<?>> TYPES = new LinkedHashMap<>();

    public static final DataComponentType<WeaponBuild> WEAPON_BUILD = register("weapon_build",
            builder -> builder.persistent(WeaponBuild.CODEC).networkSynchronized(WeaponBuild.STREAM_CODEC).cacheEncoding());

    public static final DataComponentType<ChargedProjectiles> LOADED_AMMO = register("loaded_ammo",
            builder -> builder.persistent(ChargedProjectiles.CODEC).networkSynchronized(ChargedProjectiles.STREAM_CODEC).cacheEncoding());

    private static <T> DataComponentType<T> register(String name, UnaryOperator<DataComponentType.Builder<T>> op) {
        DataComponentType<T> type = op.apply(DataComponentType.builder()).build();
        ResourceKey<DataComponentType<?>> key = ResourceKey.create(
                Registries.DATA_COMPONENT_TYPE, Identifier.fromNamespaceAndPath(Constants.MOD_ID, name));
        TYPES.put(key, type);
        return type;
    }

    public static void register(BiConsumer<ResourceKey<DataComponentType<?>>, DataComponentType<?>> registrar) {
        TYPES.forEach(registrar);
    }

    private ModDataComponents() {}
}
