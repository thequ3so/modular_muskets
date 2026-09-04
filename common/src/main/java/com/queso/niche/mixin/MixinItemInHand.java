package com.queso.niche.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.queso.niche.client.AimClient;
import com.queso.niche.client.MusketTutorial;
import com.queso.niche.client.ReloadPose;
import com.queso.niche.weapon.ModularWeaponItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemInHandRenderer.class)
public abstract class MixinItemInHand {

    private static final float FORWARD_SHIFT = 0.2F;

    @Inject(method = "shouldInstantlyReplaceVisibleItem", at = @At("HEAD"), cancellable = true)
    private void niche$noReequipOnComponentChange(ItemStack from, ItemStack to, CallbackInfoReturnable<Boolean> cir) {
        if (from.getItem() instanceof ModularWeaponItem && to.getItem() == from.getItem()) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "itemUsed", at = @At("HEAD"), cancellable = true)
    private void niche$noUseBounce(InteractionHand hand, CallbackInfo ci) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null && player.getItemInHand(hand).getItem() instanceof ModularWeaponItem) {
            ci.cancel();
        }
    }

    @Inject(method = "renderItem", at = @At("HEAD"), cancellable = true)
    private void niche$reloadTilt(LivingEntity mob, ItemStack itemStack, ItemDisplayContext type,
                                  PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords,
                                  CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        if (mob != mc.player || !type.firstPerson() || !(itemStack.getItem() instanceof ModularWeaponItem)) {
            return;
        }
        MusketTutorial.maybeTrigger();
        if (AimClient.isScoped() && AimClient.zoomAmount() > 0.6F) {
            ci.cancel();
            return;
        }
        poseStack.translate(0.0F, 0.0F, -FORWARD_SHIFT);

        LocalPlayer player = mc.player;
        boolean reloading = player.isUsingItem()
                && player.getUseItem() == itemStack
                && !ModularWeaponItem.isLoaded(itemStack);
        float tilt = ReloadPose.update(reloading);
        if (Math.abs(tilt) > 0.05F) {
            poseStack.mulPose(Axis.XP.rotationDegrees(tilt));
        }

        float kick = AimClient.modelKick();
        if (kick > 0.05F) {
            poseStack.mulPose(Axis.XP.rotationDegrees(kick * 4.5F));
        }
    }
}
