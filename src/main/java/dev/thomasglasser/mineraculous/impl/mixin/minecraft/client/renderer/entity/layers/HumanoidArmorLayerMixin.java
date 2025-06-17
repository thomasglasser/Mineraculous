package dev.thomasglasser.mineraculous.impl.mixin.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.thomasglasser.mineraculous.api.client.renderer.MineraculousRenderTypes;
import dev.thomasglasser.mineraculous.api.core.component.MineraculousDataComponents;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidArmorLayer.class)
public class HumanoidArmorLayerMixin {
    @Inject(method = "renderArmorPiece(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/EquipmentSlot;ILnet/minecraft/client/model/HumanoidModel;FFFFFF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;hasFoil()Z"))
    private void renderLuckyCharm(PoseStack poseStack, MultiBufferSource bufferSource, LivingEntity livingEntity, EquipmentSlot slot, int packedLight, HumanoidModel<?> model, float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci) {
        if (livingEntity.getItemBySlot(slot).has(MineraculousDataComponents.LUCKY_CHARM)) {
            model.renderToBuffer(poseStack, bufferSource.getBuffer(MineraculousRenderTypes.armorLuckyCharm()), packedLight, OverlayTexture.NO_OVERLAY);
        }
    }
}
