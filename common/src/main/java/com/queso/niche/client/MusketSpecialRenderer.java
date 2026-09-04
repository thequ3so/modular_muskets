package com.queso.niche.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.queso.niche.Constants;
import com.queso.niche.registry.ModDataComponents;
import com.queso.niche.weapon.WeaponBuild;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.List;
import java.util.function.Consumer;

public final class MusketSpecialRenderer implements SpecialModelRenderer<MusketSpecialRenderer.Build> {

    public static final Identifier TYPE_ID =
            Identifier.fromNamespaceAndPath(Constants.MOD_ID, "modular_weapon");

    private static final float Z_FRONT = 8.5F / 16.0F, Z_BACK = 7.5F / 16.0F;

    private final WeaponTextureCompositor.LayerSet set;

    private final Build fallback;

    private MusketSpecialRenderer(WeaponTextureCompositor.LayerSet set) {
        this.set = set;
        this.fallback = new Build(set.fallbackLayers());
    }

    public record Build(List<Identifier> layers) {}

    @Override
    public Build extractArgument(ItemStack stack) {
        WeaponBuild build = stack.get(ModDataComponents.WEAPON_BUILD);
        if (build == null || build.isEmpty()) {
            return fallback;
        }
        List<Identifier> layers = WeaponTextureCompositor.resolvedLayers(build, set);
        return layers.isEmpty() ? fallback : new Build(layers);
    }

    @Override
    public void submit(Build build, PoseStack poseStack, SubmitNodeCollector collector,
                       int light, int overlay, boolean hasFoil, int outlineColor) {
        WeaponTextureCompositor.Composited model = WeaponTextureCompositor.composited(build.layers());
        if (model == null) {
            return;
        }
        collector.submitCustomGeometry(poseStack, RenderTypes.entityCutout(model.texture()),
                (pose, buffer) -> {
                    for (WeaponTextureCompositor.MeshQuad quad : model.quads()) {
                        emitQuad(buffer, pose, quad, light, overlay);
                    }
                });
    }

    private static void emitQuad(VertexConsumer buffer, PoseStack.Pose pose,
                                 WeaponTextureCompositor.MeshQuad quad, int light, int overlay) {
        float[] p = quad.pos();
        float[] uv = quad.uv();
        for (int i = 0; i < 4; i++) {
            buffer.addVertex(pose, p[i * 3], p[i * 3 + 1], p[i * 3 + 2])
                    .setColor(0xFFFFFFFF)
                    .setUv(uv[i * 2], uv[i * 2 + 1])
                    .setOverlay(overlay)
                    .setLight(light)
                    .setNormal(pose, quad.nx(), quad.ny(), quad.nz());
        }
    }

    @Override
    public void getExtents(Consumer<Vector3fc> output) {
        output.accept(new Vector3f(0.0F, 0.0F, Z_FRONT));
        output.accept(new Vector3f(1.0F, 0.0F, Z_FRONT));
        output.accept(new Vector3f(1.0F, 1.0F, Z_BACK));
        output.accept(new Vector3f(0.0F, 1.0F, Z_BACK));
    }

    public record Unbaked(WeaponTextureCompositor.LayerSet layers) implements SpecialModelRenderer.Unbaked<Build> {
        public static final MapCodec<Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                WeaponTextureCompositor.LayerSet.CODEC
                        .optionalFieldOf("layers", WeaponTextureCompositor.LayerSet.WEAPON)
                        .forGetter(Unbaked::layers)
        ).apply(instance, Unbaked::new));

        @Override
        public SpecialModelRenderer<Build> bake(SpecialModelRenderer.BakingContext context) {
            return new MusketSpecialRenderer(layers);
        }

        @Override
        public MapCodec<Unbaked> type() {
            return MAP_CODEC;
        }
    }
}
