package com.queso.niche;

import com.queso.niche.registry.ModBlocks;
import com.queso.niche.registry.ModDataComponents;
import com.queso.niche.registry.ModEntities;
import com.queso.niche.registry.ModItems;
import com.queso.niche.block.WeaponStationMenu;
import com.queso.niche.registry.ModCreativeTabs;
import com.queso.niche.registry.ModMenus;
import com.queso.niche.registry.ModParticles;
import com.queso.niche.client.HeadshotOverlay;
import com.queso.niche.network.HeadshotKillPayload;
import com.queso.niche.network.NicheNetworking;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.registries.RegisterEvent;

@Mod(Constants.MOD_ID)
public class Niche {

    public Niche(IEventBus eventBus) {

        Constants.LOG.info("Hello NeoForge world!");
        CommonClass.init();

        eventBus.addListener(this::registerContent);
        eventBus.addListener(this::addCreative);
        eventBus.addListener(this::registerPayloads);
    }

    private void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToClient(HeadshotKillPayload.TYPE, HeadshotKillPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(HeadshotOverlay::trigger));
        NicheNetworking.headshotKillSender = player -> PacketDistributor.sendToPlayer(player, HeadshotKillPayload.INSTANCE);
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == ModCreativeTabs.NICHE) {
            ModItems.creativeStacks().forEach(event::accept);
            event.accept(ModBlocks.WEAPON_STATION_ITEM);
        }
    }

    private void registerContent(RegisterEvent event) {
        event.register(Registries.DATA_COMPONENT_TYPE, registry -> ModDataComponents.register(registry::register));
        event.register(Registries.ITEM, registry -> {
            ModItems.register(registry::register);
            ModBlocks.registerBlockItems(registry::register);
        });
        event.register(Registries.BLOCK, registry -> ModBlocks.registerBlocks(registry::register));
        event.register(Registries.BLOCK_ENTITY_TYPE, registry -> ModBlocks.registerBlockEntities(registry::register));
        event.register(Registries.ENTITY_TYPE, registry -> ModEntities.register(registry::register));
        event.register(Registries.PARTICLE_TYPE, registry -> ModParticles.register(registry::register));
        event.register(Registries.MENU, registry -> {
            ModMenus.WEAPON_STATION = IMenuTypeExtension.create((id, inv, buf) -> new WeaponStationMenu(id, inv));
            registry.register(ModMenus.WEAPON_STATION_ID, ModMenus.WEAPON_STATION);
        });
        event.register(Registries.CREATIVE_MODE_TAB, registry -> ModCreativeTabs.register(registry::register));
    }
}
