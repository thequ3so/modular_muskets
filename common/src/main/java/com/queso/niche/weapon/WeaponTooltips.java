package com.queso.niche.weapon;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.function.Consumer;

public final class WeaponTooltips {

    private WeaponTooltips() {}

    public static void appendStatDeltas(PartStats stats, Consumer<Component> lines) {
        addDelta(lines, "damage", stats.damage(), true);
        addDelta(lines, "velocity", stats.velocity(), true);
        addDelta(lines, "accuracy", -stats.inaccuracy(), true);
        addDelta(lines, "reload", stats.reloadTicks(), false);
        addDelta(lines, "magazine", stats.magazine(), true);
    }

    public static void appendAimDeltas(float aimSpeed, int stability, Consumer<Component> lines) {
        if (aimSpeed != WeaponPartItem.DEFAULT_AIM_SPEED) {
            addDelta(lines, "aim_speed", Math.round((aimSpeed - 1.0F) * 100.0F), true, "%");
        }
        if (stability != 0) {
            addDelta(lines, "stability", stability / 20.0F, true, "s");
        }
    }

    public static void appendWeaponStats(WeaponStats stats, Consumer<Component> lines) {
        lines.accept(statLine("damage", String.format("%.1f", stats.damage())));
        lines.accept(statLine("velocity", String.format("%.1f", stats.velocity())));
        lines.accept(statLine("accuracy", String.format("%.1f", stats.inaccuracy())));
        lines.accept(statLine("reload", String.format("%.1fs", stats.reloadTicks() / 20.0F)));
        lines.accept(statLine("magazine", Integer.toString(stats.magazine())));
    }

    private static MutableComponent statLine(String stat, String value) {
        return Component.translatable("tooltip.niche.stat." + stat)
                .append(Component.literal(": " + value))
                .withStyle(ChatFormatting.GRAY);
    }

    private static void addDelta(Consumer<Component> lines, String stat, float value, boolean higherIsBetter) {
        addDelta(lines, stat, value, higherIsBetter, "");
    }

    private static void addDelta(Consumer<Component> lines, String stat, float value, boolean higherIsBetter, String unit) {
        if (value == 0.0F) {
            return;
        }
        boolean good = higherIsBetter == (value > 0.0F);
        String num = (value > 0.0F ? "+" : "") + trim(value) + unit;
        lines.accept(Component.literal(num + " ")
                .append(Component.translatable("tooltip.niche.stat." + stat))
                .withStyle(good ? ChatFormatting.GREEN : ChatFormatting.RED));
    }

    private static String trim(float v) {
        return v == Math.rint(v) ? Integer.toString((int) v) : String.format("%.1f", v);
    }
}
