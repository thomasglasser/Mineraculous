package dev.thomasglasser.mineraculous.impl.mixin.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.CapeLayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CapeLayer.class)
public class CapeLayerMixin {
    @Inject(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/player/AbstractClientPlayer;FFFFFF)V", at = @At("HEAD"), cancellable = true)
    private void disableCapeRenderWhenTransformed(PoseStack poseStack, MultiBufferSource buffer, int packedLight, AbstractClientPlayer livingEntity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci) {
        if (livingEntity.getData(MineraculousAttachmentTypes.MIRACULOUSES).isTransformed() || livingEntity.getData(MineraculousAttachmentTypes.KAMIKOTIZATION).isPresent()) {
            ci.cancel();
        }
    }
}
