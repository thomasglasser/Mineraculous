package dev.thomasglasser.mineraculous.impl.mixin.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(EntityRenderer.class)
public class EntityRendererMixin {
    @Unique
    private static Entity mineraculous$entity;

    @Inject(method = "renderLeash", at = @At("HEAD"))
    private <T extends Entity, E extends Entity> void captureEntity(T entity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, E leashHolder, CallbackInfo ci) {
        EntityRendererMixin.mineraculous$entity = entity;
    }

    @Inject(method = "renderLeash", at = @At("TAIL"))
    private <T extends Entity, E extends Entity> void releaseEntity(T entity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, E leashHolder, CallbackInfo ci) {
        EntityRendererMixin.mineraculous$entity = null;
    }

    @ModifyArgs(method = "addVertexPair", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/VertexConsumer;setColor(FFFF)Lcom/mojang/blaze3d/vertex/VertexConsumer;"))
    private static void renderYoyoLeashRope(Args args) {
        if (mineraculous$entity != null && mineraculous$entity.getData(MineraculousAttachmentTypes.YOYO_LEASH_HOLDER).isPresent()) {
            args.setAll(0.0F, 0.0F, 0.0F, 1.0F);
        }
    }
}
