package dev.thomasglasser.mineraculous.impl.mixin.tommylib.api.client.renderer.entity;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.thomasglasser.mineraculous.impl.client.MineraculousClientUtils;
import dev.thomasglasser.tommylib.api.client.renderer.entity.ThrownSwordRenderer;
import dev.thomasglasser.tommylib.api.world.entity.projectile.ThrownSword;
import net.minecraft.client.renderer.MultiBufferSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ThrownSwordRenderer.class)
public class ThrownSwordRendererMixin {
    @ModifyExpressionValue(method = "render(Ldev/thomasglasser/tommylib/api/world/entity/projectile/ThrownSword;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/ItemRenderer;getFoilBufferDirect(Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/client/renderer/RenderType;ZZ)Lcom/mojang/blaze3d/vertex/VertexConsumer;"))
    private VertexConsumer checkLuckyCharm(VertexConsumer original, @Local(argsOnly = true) MultiBufferSource bufferSource, @Local(argsOnly = true) ThrownSword sword) {
        return MineraculousClientUtils.checkLuckyCharm(original, bufferSource, sword.getPickupItemStackOrigin(), false, true);
    }
}
