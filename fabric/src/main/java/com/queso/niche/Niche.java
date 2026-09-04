package com.queso.niche;

import com.queso.niche.registry.ModBlocks;
import com.queso.niche.registry.ModDataComponents;
import com.queso.niche.registry.ModEntities;
import com.queso.niche.registry.ModItems;
import com.queso.niche.block.WeaponStationMenu;
import com.queso.niche.registry.ModCreativeTabs;
import com.queso.niche.registry.ModMenus;
import com.queso.niche.registry.ModParticles;
import com.queso.niche.network.HeadshotKillPayload;
import com.queso.niche.network.NicheNetworking;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.NestedLootTable;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

import java.util.HashMap;
import java.util.Map;

public class Niche implements ModInitializer {

    private static final Map<ResourceKey<LootTable>, ResourceKey<LootTable>> LOOT_INJECTIONS = new HashMap<>();

    static {
        inject(BuiltInLootTables.ANCIENT_CITY, "ancient_city");
        inject(BuiltInLootTables.NETHER_BRIDGE, "nether_bridge");
        inject(BuiltInLootTables.BASTION_TREASURE, "bastion");
        inject(BuiltInLootTables.END_CITY_TREASURE, "end_city");
        inject(BuiltInLootTables.BURIED_TREASURE, "buried_treasure");
        inject(BuiltInLootTables.SHIPWRECK_TREASURE, "shipwreck");
        inject(BuiltInLootTables.TRIAL_CHAMBERS_REWARD, "trial_chambers");
        inject(BuiltInLootTables.STRONGHOLD_CORRIDOR, "stronghold");
        inject(BuiltInLootTables.SIMPLE_DUNGEON, "dungeon");
        inject(BuiltInLootTables.PILLAGER_OUTPOST, "pillager_outpost");
        inject(BuiltInLootTables.DESERT_PYRAMID, "desert_pyramid");
        inject(BuiltInLootTables.JUNGLE_TEMPLE, "jungle_temple");
        inject(BuiltInLootTables.ABANDONED_MINESHAFT, "mineshaft");
    }

    private static void inject(ResourceKey<LootTable> vanilla, String path) {
        LOOT_INJECTIONS.put(vanilla, ResourceKey.create(Registries.LOOT_TABLE,
                Identifier.fromNamespaceAndPath(Constants.MOD_ID, "injection/" + path)));
    }

    @Override
    public void onInitialize() {

        Constants.LOG.info("Hello Fabric world!");
        CommonClass.init();

        ModDataComponents.register((key, type) -> Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, key, type));
        ModItems.register((key, item) -> Registry.register(BuiltInRegistries.ITEM, key, item));
        ModBlocks.registerBlocks((key, block) -> Registry.register(BuiltInRegistries.BLOCK, key, block));
        ModBlocks.registerBlockItems((key, item) -> Registry.register(BuiltInRegistries.ITEM, key, item));
        ModBlocks.registerBlockEntities((key, type) -> Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, key, type));
        ModEntities.register((key, type) -> Registry.register(BuiltInRegistries.ENTITY_TYPE, key, type));
        ModParticles.register((key, type) -> Registry.register(BuiltInRegistries.PARTICLE_TYPE, key, type));

        PayloadTypeRegistry.clientboundPlay().register(HeadshotKillPayload.TYPE, HeadshotKillPayload.STREAM_CODEC);
        NicheNetworking.headshotKillSender = player -> ServerPlayNetworking.send(player, HeadshotKillPayload.INSTANCE);

        ModMenus.WEAPON_STATION = new MenuType<>(WeaponStationMenu::new, FeatureFlags.VANILLA_SET);
        Registry.register(BuiltInRegistries.MENU, ModMenus.WEAPON_STATION_ID, ModMenus.WEAPON_STATION);

        ModCreativeTabs.register((key, tab) -> Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, key, tab));
        CreativeModeTabEvents.modifyOutputEvent(ModCreativeTabs.NICHE).register(output -> {
            ModItems.creativeStacks().forEach(output::accept);
            output.accept(ModBlocks.WEAPON_STATION_ITEM);
        });

        LootTableEvents.MODIFY.register((key, tableBuilder, source, registries) -> {
            ResourceKey<LootTable> injection = LOOT_INJECTIONS.get(key);
            if (injection != null && source.isBuiltin()) {
                tableBuilder.withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1.0F))
                        .add(NestedLootTable.lootTableReference(injection)));
            }
        });
    }
}
