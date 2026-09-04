package com.queso.niche.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.queso.niche.client.AimClient;
import net.minecraft.client.player.AbstractClientPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AbstractClientPlayer.class)
public class MixinPlayerFov {

    @ModifyReturnValue(method = "getFieldOfViewModifier(ZF)F", at = @At("RETURN"))
    private float niche$aimZoom(float original) {
        return original * AimClient.fovMultiplier();
    }
}
