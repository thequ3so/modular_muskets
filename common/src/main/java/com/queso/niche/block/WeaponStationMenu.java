package com.queso.niche.block;

import com.queso.niche.registry.ModMenus;
import com.queso.niche.weapon.Augment;
import com.queso.niche.weapon.ModularWeaponItem;
import com.queso.niche.weapon.WeaponComponentType;
import com.queso.niche.weapon.WeaponPartItem;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class WeaponStationMenu extends AbstractContainerMenu {

    private static final int STATION_SLOTS = WeaponStationBlockEntity.SIZE;
    private static final int INV_START = STATION_SLOTS;
    private static final int INV_END = INV_START + 27;
    private static final int HOTBAR_END = INV_END + 9;

    private final Container container;

    public WeaponStationMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, new SimpleContainer(WeaponStationBlockEntity.SIZE));
    }

    public WeaponStationMenu(int containerId, Inventory playerInventory, Container container) {
        super(ModMenus.WEAPON_STATION, containerId);
        checkContainerSize(container, WeaponStationBlockEntity.SIZE);
        this.container = container;
        container.startOpen(playerInventory.player);

        this.addSlot(new ComponentSlot(container, WeaponStationBlockEntity.SLOT_BARREL, 8, 20));
        this.addSlot(new ComponentSlot(container, WeaponStationBlockEntity.SLOT_STOCK, 80, 20));
        this.addSlot(new ComponentSlot(container, WeaponStationBlockEntity.SLOT_BRACING, 44, 20));
        this.addSlot(new AugmentSlot(container, WeaponStationBlockEntity.SLOT_AUGMENT, 143, 20));
        this.addSlot(new WeaponSlot(container, WeaponStationBlockEntity.SLOT_RESULT, 144, 75));

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 128 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 186));
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return this.container.stillValid(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stack = slot.getItem();
            result = stack.copy();
            if (index < STATION_SLOTS) {
                if (!this.moveItemStackTo(stack, INV_START, HOTBAR_END, true)) {
                    return ItemStack.EMPTY;
                }
                slot.onQuickCraft(stack, result);
            } else {
                if (!this.moveItemStackTo(stack, 0, STATION_SLOTS - 1, false)) {
                    if (index < INV_END) {
                        if (!this.moveItemStackTo(stack, INV_END, HOTBAR_END, false)) {
                            return ItemStack.EMPTY;
                        }
                    } else if (!this.moveItemStackTo(stack, INV_START, INV_END, false)) {
                        return ItemStack.EMPTY;
                    }
                }
            }

            if (stack.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
            if (stack.getCount() == result.getCount()) {
                return ItemStack.EMPTY;
            }
            slot.onTake(player, stack);
        }
        return result;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.container.stopOpen(player);
    }

    private static final class ComponentSlot extends Slot {
        private final WeaponComponentType type;

        ComponentSlot(Container container, int index, int x, int y) {
            super(container, index, x, y);
            this.type = WeaponStationBlockEntity.componentTypeForSlot(index);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return stack.getItem() instanceof WeaponPartItem part && part.slot() == this.type;
        }
    }

    private static final class AugmentSlot extends Slot {
        AugmentSlot(Container container, int index, int x, int y) {
            super(container, index, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return Augment.isAugment(stack);
        }
    }

    private static final class WeaponSlot extends Slot {
        WeaponSlot(Container container, int index, int x, int y) {
            super(container, index, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            if (!(stack.getItem() instanceof ModularWeaponItem)) {
                return false;
            }
            return this.container.getItem(WeaponStationBlockEntity.SLOT_BARREL).isEmpty()
                    && this.container.getItem(WeaponStationBlockEntity.SLOT_STOCK).isEmpty()
                    && this.container.getItem(WeaponStationBlockEntity.SLOT_BRACING).isEmpty()
                    && this.container.getItem(WeaponStationBlockEntity.SLOT_AUGMENT).isEmpty();
        }

        @Override
        public void onTake(Player player, ItemStack stack) {
            if (this.container instanceof WeaponStationBlockEntity station) {
                station.consumeComponents();
            }
            if (!player.level().isClientSide()) {
                player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.SMITHING_TABLE_USE, SoundSource.BLOCKS, 0.8F, 1.4F);
            }
            super.onTake(player, stack);
        }
    }
}
