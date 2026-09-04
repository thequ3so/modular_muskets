package com.queso.niche.mixin;

import com.queso.niche.client.AimClient;
import com.queso.niche.client.Crosshair;
import com.queso.niche.weapon.ModularWeaponItem;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Hud.class)
public class MixinHud {

    @Inject(method = "extractCrosshair", at = @At("HEAD"), cancellable = true)
    private void niche$dynamicCrosshair(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || !mc.options.getCameraType().isFirstPerson()) {
            return;
        }
        ItemStack main = mc.player.getMainHandItem();
        if (!(main.getItem() instanceof ModularWeaponItem weapon)) {
            return;
        }

        float inaccuracy = weapon.stats(main).inaccuracy();
        float aim = AimClient.zoomAmount();
        float bloom = (1.0F - aim) * inaccuracy * Crosshair.SPREAD_SCALE;
        float speed = (float) mc.player.getDeltaMovement().horizontalDistance();
        float moveSpread = Math.min(speed * Crosshair.MOVE_SCALE, Crosshair.MOVE_MAX);
        int gap = Crosshair.gap(bloom, moveSpread);
        int tick = Crosshair.TICK_LEN;

        int cx = graphics.guiWidth() / 2;
        int cy = graphics.guiHeight() / 2;
        graphics.nextStratum();

        int color = niche$readyColor(AimClient.readiness());

        niche$tick(graphics, cx, cy - gap - tick, cx + 1, cy - gap, color);
        niche$tick(graphics, cx, cy + gap + 1, cx + 1, cy + gap + 1 + tick, color);
        niche$tick(graphics, cx - gap - tick, cy, cx - gap, cy + 1, color);
        niche$tick(graphics, cx + gap + 1, cy, cx + gap + 1 + tick, cy + 1, color);
        niche$tick(graphics, cx, cy, cx + 1, cy + 1, color);

        ci.cancel();
    }

    private static int niche$readyColor(float readiness) {
        int rb = (int) Mth.lerp(Mth.clamp(readiness, 0.0F, 1.0F), 0xFF, 0x55);
        return 0xFF000000 | (rb << 16) | (0xFF << 8) | rb;
    }

    private static void niche$tick(GuiGraphicsExtractor graphics, int x0, int y0, int x1, int y1, int color) {
        graphics.fill(x0 - 1, y0 - 1, x1 + 1, y1 + 1, 0xB0000000);
        graphics.fill(x0, y0, x1, y1, color);
    }
}
