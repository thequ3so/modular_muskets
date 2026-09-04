package com.queso.niche.registry;

import com.queso.niche.Constants;
import com.queso.niche.block.WeaponStationBlock;
import com.queso.niche.block.WeaponStationBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;

public final class ModBlocks {

    private static final Map<ResourceKey<Block>, Block> BLOCKS = new LinkedHashMap<>();
    private static final Map<ResourceKey<Item>, Item> BLOCK_ITEMS = new LinkedHashMap<>();
    private static final Map<ResourceKey<BlockEntityType<?>>, BlockEntityType<?>> BLOCK_ENTITIES = new LinkedHashMap<>();

    public static final Block WEAPON_STATION = registerBlock("weapon_station", key ->
            new WeaponStationBlock(BlockBehaviour.Properties.of().setId(key).strength(2.5F).sound(SoundType.WOOD)));

    public static final Item WEAPON_STATION_ITEM = registerBlockItem("weapon_station", WEAPON_STATION);

    public static final BlockEntityType<WeaponStationBlockEntity> WEAPON_STATION_BE = registerBlockEntity(
            "weapon_station", new BlockEntityType<>(WeaponStationBlockEntity::new, Set.of(WEAPON_STATION)));

    private static Block registerBlock(String name, Function<ResourceKey<Block>, Block> factory) {
        ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(Constants.MOD_ID, name));
        Block block = factory.apply(key);
        BLOCKS.put(key, block);
        return block;
    }

    private static Item registerBlockItem(String name, Block block) {
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(Constants.MOD_ID, name));
        BlockItem item = new BlockItem(block, new Item.Properties().setId(key).useBlockDescriptionPrefix());
        BLOCK_ITEMS.put(key, item);
        return item;
    }

    private static <T extends BlockEntity> BlockEntityType<T> registerBlockEntity(String name, BlockEntityType<T> type) {
        ResourceKey<BlockEntityType<?>> key = ResourceKey.create(
                Registries.BLOCK_ENTITY_TYPE, Identifier.fromNamespaceAndPath(Constants.MOD_ID, name));
        BLOCK_ENTITIES.put(key, type);
        return type;
    }

    public static void registerBlocks(BiConsumer<ResourceKey<Block>, Block> registrar) {
        BLOCKS.forEach(registrar);
    }

    public static void registerBlockItems(BiConsumer<ResourceKey<Item>, Item> registrar) {
        BLOCK_ITEMS.forEach(registrar);
    }

    public static void registerBlockEntities(BiConsumer<ResourceKey<BlockEntityType<?>>, BlockEntityType<?>> registrar) {
        BLOCK_ENTITIES.forEach(registrar);
    }

    private ModBlocks() {}
}
