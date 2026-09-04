package com.queso.niche.weapon;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.List;
import java.util.function.Consumer;

public class WeaponPartItem extends Item {

    public static final float DEFAULT_ZOOM_APPROACH = 0.8F;
    public static final int DEFAULT_SWAY_DELAY = Aiming.STEADY_TICKS;
    public static final float DEFAULT_AIM_SPEED = 1.0F;
    public static final int DEFAULT_STABILITY = 0;

    private final WeaponComponentType slot;
    private final PartStats stats;
    private final List<WeaponAbility> abilities;
    private final BarrelFx fx;
    private final float zoomApproach;
    private final int swayDelay;
    private final float aimSpeed;
    private final int stability;

    public WeaponPartItem(Properties properties, WeaponComponentType slot, PartStats stats) {
        this(properties, slot, stats, List.of());
    }

    public WeaponPartItem(Properties properties, WeaponComponentType slot, PartStats stats, List<WeaponAbility> abilities) {
        this(properties, slot, stats, abilities, BarrelFx.DEFAULT);
    }

    public WeaponPartItem(Properties properties, WeaponComponentType slot, PartStats stats,
                          List<WeaponAbility> abilities, BarrelFx fx) {
        this(properties, slot, stats, abilities, fx, DEFAULT_ZOOM_APPROACH, DEFAULT_SWAY_DELAY);
    }

    public WeaponPartItem(Properties properties, WeaponComponentType slot, PartStats stats,
                          List<WeaponAbility> abilities, float zoomApproach, int swayDelay) {
        this(properties, slot, stats, abilities, BarrelFx.DEFAULT, zoomApproach, swayDelay);
    }

    public WeaponPartItem(Properties properties, WeaponComponentType slot, PartStats stats,
                          List<WeaponAbility> abilities, BarrelFx fx, float zoomApproach, int swayDelay) {
        this(properties, slot, stats, abilities, fx, zoomApproach, swayDelay, DEFAULT_AIM_SPEED, DEFAULT_STABILITY);
    }

    public WeaponPartItem(Properties properties, WeaponComponentType slot, PartStats stats,
                          List<WeaponAbility> abilities, BarrelFx fx, float zoomApproach, int swayDelay,
                          float aimSpeed, int stability) {
        super(properties);
        this.slot = slot;
        this.stats = stats;
        this.abilities = List.copyOf(abilities);
        this.fx = fx;
        this.zoomApproach = zoomApproach;
        this.swayDelay = swayDelay;
        this.aimSpeed = aimSpeed;
        this.stability = stability;
    }

    public WeaponComponentType slot() {
        return slot;
    }

    public PartStats stats() {
        return stats;
    }

    public List<WeaponAbility> abilities() {
        return abilities;
    }

    public BarrelFx fx() {
        return fx;
    }

    public float zoomApproach() {
        return zoomApproach;
    }

    public int swayDelay() {
        return swayDelay;
    }

    public float aimSpeed() {
        return aimSpeed;
    }

    public int stability() {
        return stability;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display,
                                Consumer<Component> lines, TooltipFlag flag) {
        lines.accept(Component.translatable("tooltip.niche.part.slot",
                        Component.translatable(slot.translationKey()))
                .withStyle(ChatFormatting.GRAY));

        WeaponTooltips.appendStatDeltas(stats, lines);
        WeaponTooltips.appendAimDeltas(aimSpeed, stability, lines);

        for (WeaponAbility ability : abilities) {
            lines.accept(Component.literal("✦ ")
                    .append(Component.translatable(ability.translationKey()))
                    .withStyle(ability.color()));
        }
    }
}
