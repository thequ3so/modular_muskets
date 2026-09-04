package com.queso.niche.registry;

import com.queso.niche.Constants;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

import java.util.function.BiConsumer;

public final class ModCreativeTabs {

    public static final ResourceKey<CreativeModeTab> NICHE = ResourceKey.create(
            Registries.CREATIVE_MODE_TAB, Identifier.fromNamespaceAndPath(Constants.MOD_ID, "niche"));

    public static final CreativeModeTab NICHE_TAB = CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0)
            .title(Component.translatable("itemGroup.niche"))
            .icon(() -> new ItemStack(ModItems.MUSKET))
            .build();

    public static void register(BiConsumer<ResourceKey<CreativeModeTab>, CreativeModeTab> registrar) {
        registrar.accept(NICHE, NICHE_TAB);
    }

    private ModCreativeTabs() {}
}
