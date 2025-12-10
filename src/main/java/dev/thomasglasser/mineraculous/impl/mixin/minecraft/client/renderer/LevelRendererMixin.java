package dev.thomasglasser.mineraculous.impl.mixin.minecraft.client.renderer;

import dev.thomasglasser.mineraculous.impl.client.MineraculousClientUtils;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {
    @Inject(method = "onResourceManagerReload", at = @At("TAIL"))
    private void callKwamiEffectInit(CallbackInfo ci) {
        MineraculousClientUtils.initKwami();
    }

    @Inject(method = "resize", at = @At("TAIL"))
    private void resizeKwamiEffect(int width, int height, CallbackInfo ci) {
        if (MineraculousClientUtils.kwamiEffect != null) {
            MineraculousClientUtils.kwamiEffect.resize(width, height);
        }
    }

    @Inject(method = "close", at = @At("TAIL"))
    private void closeKwamiEffect(CallbackInfo ci) {
        if (MineraculousClientUtils.kwamiEffect != null) {
            MineraculousClientUtils.kwamiEffect.close();
        }
    }

    @Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;getModelViewStack()Lorg/joml/Matrix4fStack;", shift = At.Shift.BEFORE))
    private void injectAfterOutlineClear(
            DeltaTracker deltaTracker,
            boolean renderBlockOutline,
            Camera camera,
            GameRenderer gameRenderer,
            LightTexture lightTexture,
            Matrix4f frustumMatrix,
            Matrix4f projectionMatrix,
            CallbackInfo ci) {
        if (MineraculousClientUtils.shouldShowKwamiGlow()) {
            MineraculousClientUtils.kwamiTarget.clear(Minecraft.ON_OSX);
            MineraculousClientUtils.kwamiTarget.copyDepthFrom(Minecraft.getInstance().getMainRenderTarget()); // supposed to enable depth test
        }
    }
}
