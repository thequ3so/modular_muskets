package com.queso.niche.client;

import com.queso.niche.Constants;
import com.queso.niche.client.NicheParticle;
import com.queso.niche.registry.ModEntities;
import com.queso.niche.registry.ModMenus;
import com.queso.niche.registry.ModParticles;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.event.RegisterSpecialModelRendererEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

@EventBusSubscriber(modid = Constants.MOD_ID, value = Dist.CLIENT)
public final class NeoForgeClientEvents {

    @SubscribeEvent
    static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.MUSKET_BALL, MusketBallRenderer::new);
    }

    @SubscribeEvent
    static void onRegisterScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenus.WEAPON_STATION, WeaponStationScreen::new);
    }

    @SubscribeEvent
    static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.CROSSHAIR,
                Identifier.fromNamespaceAndPath(Constants.MOD_ID, "reload_indicator"), WeaponHud::render);
    }

    @SubscribeEvent
    static void onAddReloadListeners(AddClientReloadListenersEvent event) {
        event.addListener(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "weapon_composite"),
                (state, bg, barrier, game) -> barrier.wait((Void) null)
                        .thenRunAsync(WeaponTextureCompositor::clear, game));
    }

    @SubscribeEvent
    static void onRegisterSpecialModelRenderers(RegisterSpecialModelRendererEvent event) {
        event.register(MusketSpecialRenderer.TYPE_ID, MusketSpecialRenderer.Unbaked.MAP_CODEC);
    }

    @SubscribeEvent
    static void onRegisterParticles(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ModParticles.MUSKETBALL_TRAIL, NicheParticle.TrailProvider::new);
        event.registerSpriteSet(ModParticles.MUSKET_POOF, NicheParticle.PoofProvider::new);
    }

    private NeoForgeClientEvents() {}
}
