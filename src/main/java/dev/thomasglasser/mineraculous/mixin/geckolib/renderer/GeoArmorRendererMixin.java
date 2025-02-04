package dev.thomasglasser.mineraculous.mixin.geckolib.renderer;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

@Mixin(GeoArmorRenderer.class)
public abstract class GeoArmorRendererMixin {
    @Shadow
    public abstract ItemStack getCurrentStack();

    @ModifyExpressionValue(method = "renderToBuffer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/ItemRenderer;getArmorFoilBuffer(Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/client/renderer/RenderType;Z)Lcom/mojang/blaze3d/vertex/VertexConsumer;"))
    private VertexConsumer renderToBuffer(VertexConsumer original, @Local MultiBufferSource bufferSource) {
        return /*MineraculousClientUtils.checkLuckyCharm(original, bufferSource, getCurrentStack())*/original;
    }
}
