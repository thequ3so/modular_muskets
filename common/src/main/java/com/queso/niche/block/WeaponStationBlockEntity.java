package com.queso.niche.block;

import com.queso.niche.registry.ModBlocks;
import com.queso.niche.registry.ModDataComponents;
import com.queso.niche.weapon.Augment;
import com.queso.niche.weapon.ModularWeaponItem;
import com.queso.niche.weapon.WeaponAssembly;
import com.queso.niche.weapon.WeaponBuild;
import com.queso.niche.weapon.WeaponComponentType;
import com.queso.niche.weapon.WeaponPartItem;
import com.queso.niche.weapon.WeaponType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.core.NonNullList;

import java.util.List;
import java.util.Optional;

public class WeaponStationBlockEntity extends BaseContainerBlockEntity {

    public static final int SLOT_BARREL = 0;
    public static final int SLOT_STOCK = 1;
    public static final int SLOT_BRACING = 2;
    public static final int SLOT_AUGMENT = 3;
    public static final int SLOT_RESULT = 4;
    public static final int SIZE = 5;

    private NonNullList<ItemStack> items = NonNullList.withSize(SIZE, ItemStack.EMPTY);
    private boolean refreshing = false;

    private WeaponType frame = WeaponType.MUSKET;

    private Component customName = null;

    public WeaponStationBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.WEAPON_STATION_BE, pos, state);
    }

    public static WeaponComponentType componentTypeForSlot(int slot) {
        return switch (slot) {
            case SLOT_BARREL -> WeaponComponentType.BARREL;
            case SLOT_STOCK -> WeaponComponentType.STOCK;
            case SLOT_BRACING -> WeaponComponentType.BRACING;
            case SLOT_AUGMENT -> WeaponComponentType.AUGMENT;
            default -> null;
        };
    }

    @Override
    public int getContainerSize() {
        return SIZE;
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return items;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> items) {
        this.items = items;
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.niche.weapon_station");
    }

    @Override
    protected AbstractContainerMenu createMenu(int containerId, Inventory playerInventory) {
        return new WeaponStationMenu(containerId, playerInventory, this);
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        if (slot == SLOT_AUGMENT) {
            return Augment.isAugment(stack);
        }
        WeaponComponentType type = componentTypeForSlot(slot);
        return type != null && type != WeaponComponentType.AUGMENT
                && stack.getItem() instanceof WeaponPartItem part && part.slot() == type;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        super.setItem(slot, stack);
        onSlotChanged(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int count) {
        ItemStack removed = super.removeItem(slot, count);
        onSlotChanged(slot);
        return removed;
    }

    public void consumeComponents() {
        if (level == null || level.isClientSide() || refreshing) {
            return;
        }
        refreshing = true;
        try {
            items.get(SLOT_BARREL).shrink(1);
            items.get(SLOT_STOCK).shrink(1);
            items.get(SLOT_BRACING).shrink(1);
            if (Augment.isAugment(items.get(SLOT_AUGMENT))) {
                items.get(SLOT_AUGMENT).shrink(1);
            }
            recomputeResult();
        } finally {
            refreshing = false;
        }
        setChanged();
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        if (level == null) {
            return;
        }
        NonNullList<ItemStack> drops = NonNullList.create();
        drops.add(items.get(SLOT_BARREL));
        drops.add(items.get(SLOT_STOCK));
        drops.add(items.get(SLOT_BRACING));
        drops.add(items.get(SLOT_AUGMENT));
        if (componentsEmpty()) {
            drops.add(items.get(SLOT_RESULT));
        }
        net.minecraft.world.Containers.dropContents(level, pos, drops);
        for (int i = 0; i < SIZE; i++) {
            items.set(i, ItemStack.EMPTY);
        }
    }

    private boolean componentsEmpty() {
        return items.get(SLOT_BARREL).isEmpty()
                && items.get(SLOT_STOCK).isEmpty()
                && items.get(SLOT_BRACING).isEmpty();
    }

    private void onSlotChanged(int slot) {
        if (level == null || level.isClientSide() || refreshing) {
            return;
        }
        refreshing = true;
        try {
            if (slot == SLOT_RESULT) {
                ItemStack result = items.get(SLOT_RESULT);
                if (result.getItem() instanceof ModularWeaponItem weapon && componentsEmpty()) {
                    WeaponAssembly.decompose(result).ifPresent(parts -> {
                        this.frame = weapon.frame();
                        this.customName = result.get(DataComponents.CUSTOM_NAME);
                        items.set(SLOT_BARREL, parts.get(0));
                        items.set(SLOT_STOCK, parts.get(1));
                        items.set(SLOT_BRACING, parts.get(2));
                        Augment aug = result.getOrDefault(ModDataComponents.WEAPON_BUILD, WeaponBuild.EMPTY).augment();
                        items.set(SLOT_AUGMENT, aug != null ? new ItemStack(aug.item()) : ItemStack.EMPTY);
                        recomputeResult();
                    });
                }
            } else {
                recomputeResult();
            }
        } finally {
            refreshing = false;
        }
        setChanged();
    }

    private void recomputeResult() {
        if (componentsEmpty()) {
            this.customName = null;
        }
        boolean wasEmpty = items.get(SLOT_RESULT).isEmpty();
        ItemStack result = WeaponAssembly.assemble(
                items.get(SLOT_BARREL), items.get(SLOT_STOCK), items.get(SLOT_BRACING),
                items.get(SLOT_AUGMENT), this.frame).orElse(ItemStack.EMPTY);
        if (!result.isEmpty() && this.customName != null) {
            result.set(DataComponents.CUSTOM_NAME, this.customName);
        }
        items.set(SLOT_RESULT, result);
        if (wasEmpty && !result.isEmpty() && level != null && !level.isClientSide()) {
            level.playSound(null, worldPosition, SoundEvents.SMITHING_TABLE_USE, SoundSource.BLOCKS, 0.8F, 1.1F);
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, this.items);
        output.putInt("Frame", this.frame.ordinal());
        if (this.customName != null) {
            output.store("PreservedWeaponName", ComponentSerialization.CODEC, this.customName);
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.items = NonNullList.withSize(SIZE, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(input, this.items);
        this.frame = WeaponType.values()[Math.floorMod(input.getIntOr("Frame", 0), WeaponType.values().length)];
        this.customName = input.read("PreservedWeaponName", ComponentSerialization.CODEC).orElse(null);
    }
}
