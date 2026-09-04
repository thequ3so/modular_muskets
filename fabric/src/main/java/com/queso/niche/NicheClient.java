package com.queso.niche;

import com.queso.niche.client.HeadshotOverlay;
import com.queso.niche.client.MusketBallRenderer;
import com.queso.niche.client.MusketSpecialRenderer;
import com.queso.niche.client.NicheParticle;
import com.queso.niche.client.WeaponHud;
import com.queso.niche.client.WeaponStationScreen;
import com.queso.niche.client.WeaponTextureCompositor;
import com.queso.niche.network.HeadshotKillPayload;
import com.queso.niche.registry.ModEntities;
import com.queso.niche.registry.ModMenus;
import com.queso.niche.registry.ModParticles;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.special.SpecialModelRenderers;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;

public class NicheClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(ModEntities.MUSKET_BALL, MusketBallRenderer::new);
        MenuScreens.register(ModMenus.WEAPON_STATION, WeaponStationScreen::new);

        SpecialModelRenderers.ID_MAPPER.put(MusketSpecialRenderer.TYPE_ID, MusketSpecialRenderer.Unbaked.MAP_CODEC);
        HudElementRegistry.attachElementAfter(VanillaHudElements.CROSSHAIR,
                Identifier.fromNamespaceAndPath(Constants.MOD_ID, "reload_indicator"), WeaponHud::render);

        ParticleProviderRegistry.getInstance().register(ModParticles.MUSKETBALL_TRAIL, NicheParticle.TrailProvider::new);
        ParticleProviderRegistry.getInstance().register(ModParticles.MUSKET_POOF, NicheParticle.PoofProvider::new);

        ClientPlayNetworking.registerGlobalReceiver(HeadshotKillPayload.TYPE,
                (payload, context) -> context.client().execute(HeadshotOverlay::trigger));

        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(
                new SimpleSynchronousResourceReloadListener() {
                    @Override
                    public Identifier getFabricId() {
                        return Identifier.fromNamespaceAndPath(Constants.MOD_ID, "weapon_composite");
                    }

                    @Override
                    public void onResourceManagerReload(ResourceManager manager) {
                        WeaponTextureCompositor.clear();
                    }
                });
    }
}
