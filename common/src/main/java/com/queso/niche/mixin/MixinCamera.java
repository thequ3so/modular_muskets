package com.queso.niche.mixin;

import com.queso.niche.client.AimClient;
import net.minecraft.client.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Camera.class)
public class MixinCamera {

    @ModifyVariable(method = "setRotation(FF)V", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private float niche$swayYaw2(float yRot) {
        return yRot + AimClient.swayYaw();
    }

    @ModifyVariable(method = "setRotation(FF)V", at = @At("HEAD"), argsOnly = true, ordinal = 1)
    private float niche$swayPitch2(float xRot) {
        return xRot + AimClient.swayPitch() + AimClient.recoilPitch();
    }

    @ModifyVariable(method = "setRotation(FFF)V", at = @At("HEAD"), argsOnly = true, ordinal = 0, require = 0)
    private float niche$swayYaw3(float yRot) {
        return yRot + AimClient.swayYaw();
    }

    @ModifyVariable(method = "setRotation(FFF)V", at = @At("HEAD"), argsOnly = true, ordinal = 1, require = 0)
    private float niche$swayPitch3(float xRot) {
        return xRot + AimClient.swayPitch() + AimClient.recoilPitch();
    }

    @ModifyVariable(method = "setRotation(FFF)V", at = @At("HEAD"), argsOnly = true, ordinal = 2, require = 0)
    private float niche$swayRoll3(float roll) {
        return roll + AimClient.swayRoll();
    }
}
