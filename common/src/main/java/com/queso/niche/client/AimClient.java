package com.queso.niche.client;

import com.queso.niche.NicheConfig;
import com.queso.niche.registry.ModDataComponents;
import com.queso.niche.weapon.Aiming;
import com.queso.niche.weapon.Augment;
import com.queso.niche.weapon.ModularWeaponItem;
import com.queso.niche.weapon.WeaponBuild;
import net.minecraft.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

public final class AimClient {

    private static final float AIM_ONSCREEN_SPEED = 0.35F;

    private static final float ZOOM_APPROACH = 0.8F;
    private static final float SCOPE_APPROACH = 5.0F;
    private static final float SCOPE_ZOOM_FACTOR = 0.15F;

    private static final float SWAY_YAW_DEG = 3.2F;
    private static final float SWAY_PITCH_DEG = 2.0F;
    private static final float SWAY_ROLL_DEG = 4.5F;

    private static final float RECOIL_KICK = 2.2F;
    private static final float RECOIL_RECOVER = 0.55F;
    private static final float MODEL_KICK_RECOVER = 0.4F;
    private static final float MAX_RECOIL = 12.0F;

    private static float zoom = 0.0F;
    private static int aimStart = -1;
    private static float recoil = 0.0F;
    private static float modelKick = 0.0F;
    private static long lastDecayMs = -1L;

    private AimClient() {}

    public static void addRecoil(float multiplier) {
        decayKick();
        float scaled = multiplier * NicheConfig.recoilScale();
        recoil = Math.max(-MAX_RECOIL, recoil - RECOIL_KICK * scaled);
        modelKick = Math.min(3.0F, modelKick + scaled);
    }

    public static float recoilPitch() {
        decayKick();
        return recoil;
    }

    public static float modelKick() {
        decayKick();
        return modelKick;
    }

    private static void decayKick() {
        long now = Util.getMillis();
        if (lastDecayMs < 0L) {
            lastDecayMs = now;
            return;
        }
        float dt = (now - lastDecayMs) / 50.0F;
        lastDecayMs = now;
        if (dt <= 0.0F) {
            return;
        }
        recoil *= (float) Math.pow(1.0F - RECOIL_RECOVER, dt);
        modelKick *= (float) Math.pow(1.0F - MODEL_KICK_RECOVER, dt);
        if (Math.abs(recoil) < 0.01F) {
            recoil = 0.0F;
        }
        if (modelKick < 0.01F) {
            modelKick = 0.0F;
        }
    }

    public static float readiness() {
        return Mth.clamp(zoomAmount() - overfocusStrain(), 0.0F, 1.0F);
    }

    private static float zoomInApproach(LocalPlayer player) {
        if (player != null && player.getMainHandItem().getItem() instanceof ModularWeaponItem) {
            WeaponBuild build = player.getMainHandItem().getOrDefault(ModDataComponents.WEAPON_BUILD, WeaponBuild.EMPTY);
            return build.augment() == Augment.SPYGLASS ? SCOPE_APPROACH : build.zoomApproach();
        }
        return ZOOM_APPROACH;
    }

    private static boolean scoped(LocalPlayer player) {
        return player != null && player.getMainHandItem().getItem() instanceof ModularWeaponItem
                && player.getMainHandItem().getOrDefault(ModDataComponents.WEAPON_BUILD, WeaponBuild.EMPTY).augment() == Augment.SPYGLASS;
    }

    public static boolean isScoped() {
        return scoped(Minecraft.getInstance().player);
    }

    private static float currentZoomFactor() {
        return scoped(Minecraft.getInstance().player) ? SCOPE_ZOOM_FACTOR : NicheConfig.zoomFactor();
    }

    private static int swayDelay(LocalPlayer player) {
        if (player != null) {
            ItemStack held = player.getMainHandItem();
            if (held.getItem() instanceof ModularWeaponItem) {
                return held.getOrDefault(ModDataComponents.WEAPON_BUILD, WeaponBuild.EMPTY).swayDelay();
            }
        }
        return Aiming.STEADY_TICKS;
    }

    public static float zoomAmount() {
        return zoom * zoom * (3.0F - 2.0F * zoom);
    }

    public static double sensitivityScale() {
        float sensAtFullZoom = currentZoomFactor() * AIM_ONSCREEN_SPEED;
        return Mth.lerp(zoomAmount(), 1.0F, sensAtFullZoom);
    }

    public static float swayYaw() {
        float strain = overfocusStrain();
        return strain <= 0.0F ? 0.0F : (float) Math.sin(swayPhase() * 0.14) * SWAY_YAW_DEG * strain;
    }

    public static float swayPitch() {
        float strain = overfocusStrain();
        return strain <= 0.0F ? 0.0F : (float) Math.sin(swayPhase() * 0.11 + 1.3) * SWAY_PITCH_DEG * strain;
    }

    public static float swayRoll() {
        float strain = overfocusStrain();
        return strain <= 0.0F ? 0.0F : (float) Math.sin(swayPhase() * 0.09 + 0.5) * SWAY_ROLL_DEG * strain;
    }

    private static float overfocusStrain() {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (!NicheConfig.cameraSway() || scoped(player) || player == null || aimStart < 0
                || !mc.options.getCameraType().isFirstPerson() || !Aiming.isAiming(player)) {
            return 0.0F;
        }
        int held = player.tickCount - aimStart;
        float strain = Mth.clamp((held - swayDelay(player)) / (float) Aiming.SWAY_RAMP_TICKS, 0.0F, 1.0F);
        return strain * zoomAmount();
    }

    private static float swayPhase() {
        Minecraft mc = Minecraft.getInstance();
        float partial = mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
        return (mc.player != null ? mc.player.tickCount : 0) + partial;
    }

    public static float fovMultiplier() {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) {
            zoom = 0.0F;
            recoil = 0.0F;
            modelKick = 0.0F;
            aimStart = -1;
            return 1.0F;
        }
        boolean aiming = Aiming.isAiming(player) && mc.options.getCameraType().isFirstPerson();

        if (aiming) {
            if (aimStart < 0) {
                aimStart = player.tickCount;
            }
        } else {
            aimStart = -1;
        }

        float dt = mc.getDeltaTracker().getRealtimeDeltaTicks();
        float approach = aiming ? zoomInApproach(player) : ZOOM_APPROACH;
        float alpha = Mth.clamp(approach * dt, 0.0F, 1.0F);
        zoom += ((aiming ? 1.0F : 0.0F) - zoom) * alpha;
        if (zoom < 0.002F) {
            zoom = 0.0F;
            return 1.0F;
        }

        return Mth.lerp(zoomAmount(), 1.0F, currentZoomFactor());
    }
}
