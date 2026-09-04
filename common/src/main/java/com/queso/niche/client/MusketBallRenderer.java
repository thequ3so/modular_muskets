package com.queso.niche.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.queso.niche.Constants;
import com.queso.niche.entity.MusketBall;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;

public class MusketBallRenderer extends EntityRenderer<MusketBall, EntityRenderState> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(Constants.MOD_ID, "textures/entity/projectile/musketball.png");
    private static final RenderType RENDER_TYPE = RenderTypes.entityCutout(TEXTURE);
    private static final float SIZE = 0.3F;

    public MusketBallRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected int getBlockLightLevel(MusketBall entity, BlockPos blockPos) {
        return 15;
    }

    @Override
    public EntityRenderState createRenderState() {
        return new EntityRenderState();
    }

    @Override
    public void submit(EntityRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.scale(SIZE, SIZE, SIZE);
        poseStack.mulPose(camera.orientation);
        collector.submitCustomGeometry(poseStack, RENDER_TYPE, (pose, buffer) -> buildQuad(state, pose, buffer, -1));
        if (state.outlineColor != 0 && RENDER_TYPE.outline().isPresent()) {
            collector.submitCustomGeometry(poseStack, RENDER_TYPE.outline().get(),
                    (pose, buffer) -> buildQuad(state, pose, buffer, state.outlineColor));
        }
        poseStack.popPose();
        super.submit(state, poseStack, collector, camera);
    }

    private static void buildQuad(EntityRenderState state, PoseStack.Pose pose, VertexConsumer buffer, int color) {
        vertex(buffer, pose, state.lightCoords, 0.0F, 0, 0, 1, color);
        vertex(buffer, pose, state.lightCoords, 1.0F, 0, 1, 1, color);
        vertex(buffer, pose, state.lightCoords, 1.0F, 1, 1, 0, color);
        vertex(buffer, pose, state.lightCoords, 0.0F, 1, 0, 0, color);
    }

    private static void vertex(VertexConsumer buffer, PoseStack.Pose pose, int light, float x, int y, int u, int v, int color) {
        buffer.addVertex(pose, x - 0.5F, y - 0.5F, 0.0F)
                .setColor(color)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(pose, 0.0F, 1.0F, 0.0F);
    }
}
