package dev.thomasglasser.mineraculous.mixin.geckolib.renderer;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import software.bernie.geckolib.renderer.GeoItemRenderer;

@Mixin(GeoItemRenderer.class)
public abstract class GeoItemRendererMixin {
    @Shadow
    public abstract ItemStack getCurrentItemStack();

    @ModifyExpressionValue(method = "renderByItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/ItemRenderer;getFoilBufferDirect(Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/client/renderer/RenderType;ZZ)Lcom/mojang/blaze3d/vertex/VertexConsumer;"))
    private VertexConsumer renderByItem(VertexConsumer original, @Local(argsOnly = true) MultiBufferSource bufferSource) {
        return /*MineraculousClientUtils.checkLuckyCharm(original, bufferSource, getCurrentItemStack())*/original;
    }

    @ModifyExpressionValue(method = "renderInGui", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/ItemRenderer;getFoilBufferDirect(Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/client/renderer/RenderType;ZZ)Lcom/mojang/blaze3d/vertex/VertexConsumer;"))
    private VertexConsumer renderInGui(VertexConsumer original, @Local(argsOnly = true) MultiBufferSource bufferSource) {
        return /*MineraculousClientUtils.checkLuckyCharm(original, bufferSource, getCurrentItemStack())*/original;
    }
}
