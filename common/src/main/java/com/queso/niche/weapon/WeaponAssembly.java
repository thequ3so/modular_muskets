package com.queso.niche.weapon;

import com.queso.niche.registry.ModDataComponents;
import com.queso.niche.registry.ModItems;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class WeaponAssembly {

    private WeaponAssembly() {}

    public static Optional<ItemStack> assemble(ItemStack barrelStack, ItemStack stockStack, ItemStack bracingStack,
                                               ItemStack augmentStack, WeaponType frame) {
        if (!isSlot(barrelStack, WeaponComponentType.BARREL)
                || !isSlot(stockStack, WeaponComponentType.STOCK)
                || !isSlot(bracingStack, WeaponComponentType.BRACING)) {
            return Optional.empty();
        }
        List<Item> parts = new ArrayList<>(4);
        parts.add(barrelStack.getItem());
        parts.add(stockStack.getItem());
        parts.add(bracingStack.getItem());
        if (Augment.isAugment(augmentStack)) {
            parts.add(augmentStack.getItem());
        }
        ItemStack weapon = new ItemStack(ModItems.weaponFor(frame));
        weapon.set(ModDataComponents.WEAPON_BUILD, WeaponBuild.ofParts(parts));
        return Optional.of(weapon);
    }

    private static boolean isSlot(ItemStack stack, WeaponComponentType type) {
        return stack.getItem() instanceof WeaponPartItem part && part.slot() == type;
    }

    public static Optional<List<ItemStack>> decompose(ItemStack weaponStack) {
        if (!(weaponStack.getItem() instanceof ModularWeaponItem)) {
            return Optional.empty();
        }
        WeaponBuild build = weaponStack.get(ModDataComponents.WEAPON_BUILD);
        if (build == null || build.isEmpty()) {
            return Optional.empty();
        }

        Map<WeaponComponentType, ItemStack> bySlot = new EnumMap<>(WeaponComponentType.class);
        for (WeaponPartItem part : build.resolveParts()) {
            bySlot.putIfAbsent(part.slot(), new ItemStack(part));
        }
        if (!bySlot.containsKey(WeaponComponentType.BARREL)
                || !bySlot.containsKey(WeaponComponentType.STOCK)
                || !bySlot.containsKey(WeaponComponentType.BRACING)) {
            return Optional.empty();
        }
        return Optional.of(List.of(
                bySlot.get(WeaponComponentType.BARREL),
                bySlot.get(WeaponComponentType.STOCK),
                bySlot.get(WeaponComponentType.BRACING)));
    }
}
