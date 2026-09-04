package com.queso.niche.weapon;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public record WeaponBuild(List<Identifier> parts) {

    public static final WeaponBuild EMPTY = new WeaponBuild(List.of());

    public static final Codec<WeaponBuild> CODEC =
            Identifier.CODEC.listOf().xmap(WeaponBuild::new, WeaponBuild::parts);

    public static final StreamCodec<ByteBuf, WeaponBuild> STREAM_CODEC =
            Identifier.STREAM_CODEC.apply(ByteBufCodecs.list()).map(WeaponBuild::new, WeaponBuild::parts);

    public boolean isEmpty() {
        return parts.isEmpty();
    }

    public List<WeaponPartItem> resolveParts() {
        List<WeaponPartItem> out = new ArrayList<>(parts.size());
        for (Identifier id : parts) {
            Item item = BuiltInRegistries.ITEM.getValue(id);
            if (item instanceof WeaponPartItem part) {
                out.add(part);
            }
        }
        return out;
    }

    public @Nullable Augment augment() {
        for (Identifier id : parts) {
            Augment a = Augment.forItem(BuiltInRegistries.ITEM.getValue(id));
            if (a != null) {
                return a;
            }
        }
        return null;
    }

    public WeaponStats computeStats(WeaponType frame) {
        WeaponStats stats = frame.base();
        for (WeaponPartItem part : resolveParts()) {
            stats = stats.apply(part.stats());
        }
        Augment augment = augment();
        if (augment != null) {
            stats = stats.apply(augment.stats());
        }
        return stats;
    }

    public java.util.Set<WeaponAbility> abilities() {
        java.util.EnumSet<WeaponAbility> set = java.util.EnumSet.noneOf(WeaponAbility.class);
        for (WeaponPartItem part : resolveParts()) {
            set.addAll(part.abilities());
        }
        return set;
    }

    private @Nullable WeaponPartItem barrelPart() {
        for (WeaponPartItem part : resolveParts()) {
            if (part.slot() == WeaponComponentType.BARREL) {
                return part;
            }
        }
        return null;
    }

    public BarrelFx barrelFx() {
        WeaponPartItem barrel = barrelPart();
        return barrel != null ? barrel.fx() : BarrelFx.DEFAULT;
    }

    public WeaponTrail trail() {
        return barrelFx().trail();
    }

    public @Nullable WeaponSound fireSound() {
        return barrelFx().sound();
    }

    public float recoil() {
        return barrelFx().recoil();
    }

    public float zoomApproach() {
        float approach = WeaponPartItem.DEFAULT_ZOOM_APPROACH;
        float scale = 1.0F;
        for (WeaponPartItem part : resolveParts()) {
            if (part.slot() == WeaponComponentType.BRACING) {
                approach = part.zoomApproach();
            }
            scale *= part.aimSpeed();
        }
        return approach * scale;
    }

    public int swayDelay() {
        int delay = WeaponPartItem.DEFAULT_SWAY_DELAY;
        int bonus = 0;
        for (WeaponPartItem part : resolveParts()) {
            if (part.slot() == WeaponComponentType.BRACING) {
                delay = part.swayDelay();
            }
            bonus += part.stability();
        }
        return Math.max(1, delay + bonus);
    }

    public static WeaponBuild ofParts(List<? extends Item> orderedParts) {
        List<Identifier> ids = new ArrayList<>(orderedParts.size());
        for (Item item : orderedParts) {
            ids.add(BuiltInRegistries.ITEM.getKey(item));
        }
        return new WeaponBuild(List.copyOf(ids));
    }
}
