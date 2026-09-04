package com.queso.niche.client;

import com.queso.niche.Constants;
import com.queso.niche.NicheConfig;
import com.queso.niche.weapon.ModularWeaponItem;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

public final class WeaponHud {

    private static final Identifier BACKGROUND = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "textures/gui/reload_background.png");
    private static final Identifier FILL = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "textures/gui/reload_fill.png");
    private static final Identifier AMMO_FULL = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "textures/gui/ammo_full.png");
    private static final Identifier AMMO_EMPTY = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "textures/gui/ammo_empty.png");
    private static final Identifier VIGNETTE = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "textures/gui/scope_vignette.png");
    private static final Identifier SPYGLASS_SCOPE = Identifier.withDefaultNamespace("textures/misc/spyglass_scope.png");
    private static final Identifier HEADSHOT = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "textures/particles/headshot.png");

    private static final int BG_W = 22, BG_H = 5;
    private static final int FILL_W = 20, FILL_H = 3;
    private static final int AMMO_W = 5, AMMO_H = 5;
    private static final int AMMO_GAP = 1;
    private static final int AMMO_MAX_PER_ROW = 8;
    private static final int CLEARANCE = 2;

    private static final int HS_FRAME_TICKS = 3;
    private static final int HS_HOLD = 10;
    private static final int HS_FADE = 3;

    private WeaponHud() {}

    public static void render(GuiGraphicsExtractor graphics, DeltaTracker delta) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) {
            return;
        }

        renderHeadshot(graphics, player);
        renderTutorial(graphics, mc);

        ItemStack main = player.getMainHandItem();
        if (!(main.getItem() instanceof ModularWeaponItem weapon)) {
            return;
        }

        renderScope(graphics);

        float inaccuracy = weapon.stats(main).inaccuracy();
        int x = graphics.guiWidth() / 2 - BG_W / 2;
        int y = graphics.guiHeight() / 2 + Crosshair.maxHalfExtent(inaccuracy) + CLEARANCE;

        ItemStack using = player.getUseItem();
        if (player.isUsingItem() && using.getItem() instanceof ModularWeaponItem usingWeapon
                && !ModularWeaponItem.isLoaded(using)) {
            int reloadTicks = Math.max(1, usingWeapon.stats(using).reloadTicks());
            int elapsed = usingWeapon.getUseDuration(using, player) - player.getUseItemRemainingTicks();
            float progress = Mth.clamp(elapsed / (float) reloadTicks, 0.0F, 1.0F);

            graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, x, y, 0.0F, 0.0F, BG_W, BG_H, BG_W, BG_H);
            int filled = Math.round(FILL_W * progress);
            if (filled > 0) {
                graphics.blit(RenderPipelines.GUI_TEXTURED, FILL, x + 1, y + 1, 0.0F, 0.0F, filled, FILL_H, FILL_W, FILL_H);
            }
        } else if (ModularWeaponItem.isLoaded(main)) {
            int loaded = ModularWeaponItem.loadedCount(main);
            int capacity = Math.max(loaded, Math.max(1, weapon.stats(main).magazine()));
            renderAmmo(graphics, loaded, capacity, y);
        }
    }

    private static void renderAmmo(GuiGraphicsExtractor graphics, int loaded, int capacity, int y) {
        int perRow = capacity <= AMMO_MAX_PER_ROW ? capacity : (capacity + 1) / 2;
        int cx = graphics.guiWidth() / 2;
        for (int i = 0; i < capacity; i++) {
            int row = i / perRow;
            int col = i % perRow;
            int rowCount = Math.min(perRow, capacity - row * perRow);
            int rowWidth = rowCount * AMMO_W + (rowCount - 1) * AMMO_GAP;
            int px = cx - rowWidth / 2 + col * (AMMO_W + AMMO_GAP);
            int py = y + row * (AMMO_H + AMMO_GAP);
            Identifier seg = i < loaded ? AMMO_FULL : AMMO_EMPTY;
            graphics.blit(RenderPipelines.GUI_TEXTURED, seg, px, py, 0.0F, 0.0F, AMMO_W, AMMO_H, AMMO_W, AMMO_H);
        }
    }

    private static void renderTutorial(GuiGraphicsExtractor graphics, Minecraft mc) {
        float a = MusketTutorial.alpha();
        if (a <= 0.02F) {
            return;
        }
        int alpha = (int) (a * 255.0F) << 24;
        int cx = graphics.guiWidth() / 2;
        int y = graphics.guiHeight() - 84;
        graphics.centeredText(mc.font, Component.translatable("toast.niche.first_musket.title"),
                cx, y, 0x00FFE08A | alpha);
        graphics.centeredText(mc.font, Component.translatable("toast.niche.first_musket.desc"),
                cx, y + 11, 0x00FFFFFF | alpha);
    }

    private static void renderSpyglass(GuiGraphicsExtractor graphics) {
        int gw = graphics.guiWidth();
        int gh = graphics.guiHeight();
        int size = Math.min(gw, gh);
        int x = (gw - size) / 2;
        int y = (gh - size) / 2;
        graphics.blit(RenderPipelines.GUI_TEXTURED, SPYGLASS_SCOPE, x, y, 0.0F, 0.0F, size, size, size, size, 0xFFFFFFFF);
        if (x > 0) {
            graphics.fill(0, 0, x, gh, 0xFF000000);
            graphics.fill(x + size, 0, gw, gh, 0xFF000000);
        }
        if (y > 0) {
            graphics.fill(0, 0, gw, y, 0xFF000000);
            graphics.fill(0, y + size, gw, gh, 0xFF000000);
        }
    }

    private static void renderScope(GuiGraphicsExtractor graphics) {
        float amount = AimClient.zoomAmount();
        if (amount <= 0.01F) {
            return;
        }
        if (AimClient.isScoped()) {
            if (amount > 0.6F) {
                renderSpyglass(graphics);
            }
            return;
        }
        int gw = graphics.guiWidth();
        int gh = graphics.guiHeight();
        int alpha = (int) (Mth.clamp(amount, 0.0F, 1.0F) * NicheConfig.vignetteIntensity() * 255.0F) << 24;
        int side = Math.max(gw, gh);
        int x = (gw - side) / 2;
        int y = (gh - side) / 2;
        graphics.blit(RenderPipelines.GUI_TEXTURED, VIGNETTE, x, y, 0.0F, 0.0F, side, side, side, side, 0x00FFFFFF | alpha);
    }

    private static void renderHeadshot(GuiGraphicsExtractor graphics, LocalPlayer player) {
        float age = HeadshotOverlay.age();
        int animEnd = 3 * HS_FRAME_TICKS;
        int total = HS_HOLD + HS_FADE;
        if (age < 0.0F || age >= total) {
            return;
        }
        int frame = age < animEnd ? (int) (age / HS_FRAME_TICKS) : 2;
        frame = Math.min(frame, 2);
        float fade = age < HS_HOLD ? 1.0F : Mth.clamp((total - age) / (float) HS_FADE, 0.0F, 1.0F);
        int color = ((int) (fade * 255.0F) << 24) | 0x00FFFFFF;

        ItemStack main = player.getMainHandItem();
        float inaccuracy = main.getItem() instanceof ModularWeaponItem weapon ? weapon.stats(main).inaccuracy() : 0.0F;

        int size = 7;
        int x = graphics.guiWidth() / 2 - size / 2;
        int y = graphics.guiHeight() / 2 - Crosshair.maxHalfExtent(inaccuracy) - CLEARANCE - size;
        graphics.blit(RenderPipelines.GUI_TEXTURED, HEADSHOT, x, y, 0.0F, frame * 7.0F, size, size, 7, 7, 7, 21, color);
    }
}
