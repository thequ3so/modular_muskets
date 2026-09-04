package com.queso.niche.mixin;

import com.queso.niche.weapon.ModularWeaponItem;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.world.entity.HumanoidArm;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidModel.class)
public abstract class MixinPlayerModel {

    private static final float ARM_FORWARD = (float) (-Math.PI / 2.0);
    private static final float TRIGGER_YAW = 0.15F;
    private static final float SUPPORT_YAW = 0.55F;

    @Shadow public ModelPart rightArm;
    @Shadow public ModelPart leftArm;
    @Shadow public ModelPart head;

    @Inject(method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/HumanoidRenderState;)V", at = @At("TAIL"))
    private void niche$aimPose(HumanoidRenderState state, CallbackInfo ci) {
        if (!(state.getMainHandItemStack().getItem() instanceof ModularWeaponItem)) {
            return;
        }
        float pitch = ARM_FORWARD + this.head.xRot;
        boolean rightHanded = state.mainArm == HumanoidArm.RIGHT;
        ModelPart trigger = rightHanded ? this.rightArm : this.leftArm;
        ModelPart support = rightHanded ? this.leftArm : this.rightArm;
        float side = rightHanded ? 1.0F : -1.0F;

        trigger.xRot = pitch;
        trigger.yRot = this.head.yRot - side * TRIGGER_YAW;
        trigger.zRot = 0.0F;

        support.xRot = pitch;
        support.yRot = this.head.yRot + side * SUPPORT_YAW;
        support.zRot = 0.0F;
    }
}
