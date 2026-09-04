package com.queso.niche.mixin;

import com.queso.niche.client.AimClient;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public class MixinMouseHandler {

    @Shadow private double accumulatedDX;
    @Shadow private double accumulatedDY;

    @Inject(method = "turnPlayer", at = @At("HEAD"))
    private void niche$slowAimTurn(double mousea, CallbackInfo ci) {
        double scale = AimClient.sensitivityScale();
        if (scale < 1.0) {
            this.accumulatedDX *= scale;
            this.accumulatedDY *= scale;
        }
    }
}
