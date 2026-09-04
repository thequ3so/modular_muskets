package com.queso.niche.client;

import com.queso.niche.Constants;
import com.queso.niche.block.WeaponStationMenu;
import com.queso.niche.weapon.Augment;
import com.queso.niche.weapon.ModularWeaponItem;
import com.queso.niche.weapon.WeaponBuild;
import com.queso.niche.weapon.WeaponComponentType;
import com.queso.niche.weapon.WeaponPartItem;
import com.queso.niche.weapon.WeaponStats;
import com.queso.niche.weapon.WeaponType;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class WeaponStationScreen extends AbstractContainerScreen<WeaponStationMenu> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(Constants.MOD_ID, "textures/gui/weapon_station.png");
    private static final int BADGE_SIZE = 12;

    private static final int SLOT_BARREL = 0, SLOT_STOCK = 1, SLOT_BRACING = 2, SLOT_AUGMENT = 3, SLOT_RESULT = 4;

    private static final int[] HINT_SLOT = {SLOT_BARREL, SLOT_STOCK, SLOT_BRACING, SLOT_AUGMENT};
    private static final Identifier[] HINT_ICON = {icon("wooden_barrel"), icon("wooden_stock"), icon("copper_bracing"),
            Identifier.withDefaultNamespace("textures/item/spyglass.png")};
    private static final int[] HINT_X = {8, 80, 44, 143};
    private static final int HINT_Y = 20;
    private static final int HINT_TINT = 0x55FFFFFF;

    private static final int PREVIEW_X = 136, PREVIEW_Y = 67, PREVIEW_SIZE = 32;

    private static final int STAT_X_L = 10, STAT_X_R = 56, STAT_TOP = 50, STAT_LINE = 12, BADGE_Y = 85;
    private static final int TITLE_COLOR = 0xFFB0B0B0;
    private static final int COL_LABEL = 0xFFB0B0B0, COL_BETTER = 0xFF6FE86F, COL_WORSE = 0xFFE86F6F, COL_SAME = 0xFFFFFFFF;

    public WeaponStationScreen(WeaponStationMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 176, 206);
        this.inventoryLabelY = 112;
    }

    private static Identifier icon(String id) {
        return Identifier.fromNamespaceAndPath(Constants.MOD_ID, "textures/item/component/" + id + ".png");
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE,
                this.leftPos, this.topPos, 0.0F, 0.0F, this.imageWidth, this.imageHeight, 256, 256);

        renderSlotHints(graphics);

        List<WeaponPartItem> parts = installedParts();
        renderStatPreview(graphics, parts);
        renderBadges(graphics, parts);

        renderBuildPreview(graphics);
        if (this.menu.getSlot(SLOT_RESULT).getItem().isEmpty()) {
            renderRetrievalTooltip(graphics, mouseX, mouseY);
        }
        renderAugmentHelp(graphics, mouseX, mouseY);
    }

    private void renderAugmentHelp(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        int x0 = this.leftPos + HINT_X[3], y0 = this.topPos + HINT_Y;
        if (mouseX < x0 || mouseY < y0 || mouseX >= x0 + 16 || mouseY >= y0 + 16) {
            return;
        }
        if (!this.menu.getSlot(SLOT_AUGMENT).getItem().isEmpty()) {
            return;
        }
        List<Component> lines = new ArrayList<>();
        lines.add(Component.translatable("tooltip.niche.augment.slot").withStyle(ChatFormatting.GOLD));
        lines.add(Component.translatable("tooltip.niche.augment.hint").withStyle(ChatFormatting.GRAY));
        for (Augment a : Augment.all()) {
            lines.add(Component.literal(" • ").append(new ItemStack(a.item()).getHoverName())
                    .withStyle(ChatFormatting.DARK_GRAY));
        }
        graphics.setTooltipForNextFrame(this.font, lines, Optional.empty(), mouseX, mouseY);
    }

    @Override
    protected void extractSlot(GuiGraphicsExtractor graphics, Slot slot, int mouseX, int mouseY) {
        if (slot == this.menu.getSlot(SLOT_RESULT)) {
            return;
        }
        super.extractSlot(graphics, slot, mouseX, mouseY);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        graphics.text(this.font, this.title, this.titleLabelX, this.titleLabelY, TITLE_COLOR);
        graphics.text(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 0xFF404040, false);
    }

    private List<WeaponPartItem> installedParts() {
        List<WeaponPartItem> parts = new ArrayList<>(3);
        for (int slot : new int[]{SLOT_BARREL, SLOT_STOCK, SLOT_BRACING}) {
            if (this.menu.getSlot(slot).getItem().getItem() instanceof WeaponPartItem part) {
                parts.add(part);
            }
        }
        return parts;
    }

    private void renderSlotHints(GuiGraphicsExtractor graphics) {
        for (int i = 0; i < HINT_SLOT.length; i++) {
            if (this.menu.getSlot(HINT_SLOT[i]).getItem().isEmpty()) {
                graphics.blit(RenderPipelines.GUI_TEXTURED, HINT_ICON[i], this.leftPos + HINT_X[i], this.topPos + HINT_Y,
                        0.0F, 0.0F, 16, 16, 16, 16, HINT_TINT);
            }
        }
    }

    private void renderBuildPreview(GuiGraphicsExtractor graphics) {
        List<Identifier> layers = new ArrayList<>();
        layers.add(WeaponTextureCompositor.BASE_LAYER);
        for (int slot : new int[]{SLOT_BARREL, SLOT_STOCK, SLOT_BRACING}) {
            ItemStack stack = this.menu.getSlot(slot).getItem();
            if (stack.getItem() instanceof WeaponPartItem part) {
                layers.add(WeaponTextureCompositor.layerTexture(part));
            }
        }
        Augment augment = Augment.forStack(this.menu.getSlot(SLOT_AUGMENT).getItem());
        if (augment != null) {
            layers.add(augment.layer());
        }
        Identifier composite = WeaponTextureCompositor.composite(layers);
        if (composite == null) {
            return;
        }
        graphics.blit(RenderPipelines.GUI_TEXTURED, composite, this.leftPos + PREVIEW_X, this.topPos + PREVIEW_Y,
                0.0F, 0.0F, PREVIEW_SIZE, PREVIEW_SIZE, 32, 32, 32, 32, 0xFFFFFFFF);
    }

    private void renderRetrievalTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        int x0 = this.leftPos + PREVIEW_X, y0 = this.topPos + PREVIEW_Y;
        if (mouseX < x0 || mouseY < y0 || mouseX >= x0 + PREVIEW_SIZE || mouseY >= y0 + PREVIEW_SIZE) {
            return;
        }
        boolean b = this.menu.getSlot(SLOT_BARREL).getItem().isEmpty();
        boolean s = this.menu.getSlot(SLOT_STOCK).getItem().isEmpty();
        boolean br = this.menu.getSlot(SLOT_BRACING).getItem().isEmpty();

        List<Component> lines = new ArrayList<>();
        if (b && s && br) {
            lines.add(Component.translatable("tooltip.niche.station.empty").withStyle(ChatFormatting.GRAY));
        } else {
            lines.add(Component.translatable("tooltip.niche.station.missing").withStyle(ChatFormatting.GRAY));
            if (b) {
                lines.add(missing(WeaponComponentType.BARREL));
            }
            if (s) {
                lines.add(missing(WeaponComponentType.STOCK));
            }
            if (br) {
                lines.add(missing(WeaponComponentType.BRACING));
            }
        }
        graphics.setTooltipForNextFrame(this.font, lines, Optional.empty(), mouseX, mouseY);
    }

    private static Component missing(WeaponComponentType type) {
        return Component.literal(" • ").append(Component.translatable(type.translationKey()))
                .withStyle(ChatFormatting.RED);
    }

    private void renderStatPreview(GuiGraphicsExtractor graphics, List<WeaponPartItem> parts) {
        WeaponStats base = WeaponType.MUSKET.base();
        List<Item> items = new ArrayList<>(parts);
        ItemStack augmentStack = this.menu.getSlot(SLOT_AUGMENT).getItem();
        if (Augment.isAugment(augmentStack)) {
            items.add(augmentStack.getItem());
        }
        WeaponBuild build = WeaponBuild.ofParts(items);
        WeaponStats stats = build.computeStats(WeaponType.MUSKET);
        int magazine = ModularWeaponItem.effectiveMagazine(build, WeaponType.MUSKET);
        int xl = this.leftPos + STAT_X_L;
        int xr = this.leftPos + STAT_X_R;
        int y = this.topPos + STAT_TOP;
        statLine(graphics, xl, y, "Dmg", String.format("%.1f", stats.damage()), stats.damage(), base.damage(), true);
        statLine(graphics, xl, y + STAT_LINE, "Vel", String.format("%.1f", stats.velocity()), stats.velocity(), base.velocity(), true);
        statLine(graphics, xl, y + STAT_LINE * 2, "Spr", String.format("%.1f", stats.inaccuracy()), stats.inaccuracy(), base.inaccuracy(), false);
        statLine(graphics, xr, y, "Rld", String.format("%.1fs", stats.reloadTicks() / 20.0F), stats.reloadTicks(), base.reloadTicks(), false);
        statLine(graphics, xr, y + STAT_LINE, "Mag", Integer.toString(magazine), magazine, base.magazine(), true);
    }

    private void statLine(GuiGraphicsExtractor graphics, int x, int y, String label, String value,
                          float current, float base, boolean higherIsBetter) {
        graphics.text(this.font, label, x, y, COL_LABEL);
        int color = current == base ? COL_SAME : (current > base) == higherIsBetter ? COL_BETTER : COL_WORSE;
        graphics.text(this.font, value, x + this.font.width(label) + 3, y, color);
    }

    private void renderBadges(GuiGraphicsExtractor graphics, List<WeaponPartItem> parts) {
        int x = this.leftPos + STAT_X_L;
        int y = this.topPos + BADGE_Y;
        int drawn = 0;
        for (WeaponPartItem part : parts) {
            Identifier badge = badgeFor(part);
            if (badge == null) {
                continue;
            }
            graphics.blit(RenderPipelines.GUI_TEXTURED, badge, x + drawn * (BADGE_SIZE + 2), y,
                    0.0F, 0.0F, BADGE_SIZE, BADGE_SIZE, BADGE_SIZE, BADGE_SIZE, 0xFFFFFFFF);
            drawn++;
        }
    }

    private static Identifier badgeFor(WeaponPartItem part) {
        String theme = BuiltInRegistries.ITEM.getKey(part).getPath().replace("_" + part.slot().id(), "");
        Identifier badge = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "textures/gui/" + theme + "_badge.png");
        return Minecraft.getInstance().getResourceManager().getResource(badge).isPresent() ? badge : null;
    }
}
