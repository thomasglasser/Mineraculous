package dev.thomasglasser.mineraculous.impl.mixin.minecraft.client.renderer;

import dev.thomasglasser.mineraculous.impl.client.MineraculousClientUtils;
import net.minecraft.client.renderer.LevelRenderer;
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
        if (MineraculousClientUtils.getKwamiEffect() != null) {
            MineraculousClientUtils.getKwamiEffect().resize(width, height);
        }
    }

    @Inject(method = "close", at = @At("TAIL"))
    private void closeKwamiEffect(CallbackInfo ci) {
        if (MineraculousClientUtils.getKwamiEffect() != null) {
            MineraculousClientUtils.getKwamiEffect().close();
        }
    }
}
