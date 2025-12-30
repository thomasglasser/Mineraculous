package dev.thomasglasser.mineraculous.impl.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import dev.thomasglasser.mineraculous.api.client.event.RenderPlayerLikeEvent;
import dev.thomasglasser.mineraculous.impl.client.renderer.entity.PlayerLikeRenderer;
import dev.thomasglasser.mineraculous.impl.world.entity.PlayerLike;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.NeoForge;

public class PlayerLikeCapeLayer<T extends LivingEntity & PlayerLike> extends RenderLayer<T, PlayerModel<T>> {
    private final PlayerLikeRenderer<T> renderer;

    public PlayerLikeCapeLayer(PlayerLikeRenderer<T> renderer) {
        super(renderer);
        this.renderer = renderer;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, T livingEntity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        if (!livingEntity.isInvisible() && PlayerLikeRenderer.isModelPartShown(livingEntity, PlayerModelPart.CAPE) && livingEntity.getVisualSource() instanceof AbstractClientPlayer player) {
            PlayerSkin playerskin = player.getSkin();
            if (playerskin.capeTexture() != null) {
                ItemStack itemstack = livingEntity.getItemBySlot(EquipmentSlot.CHEST);
                if (!itemstack.is(Items.ELYTRA)) {
                    poseStack.pushPose();
                    if (!NeoForge.EVENT_BUS.post(new RenderPlayerLikeEvent.RenderCape<>(livingEntity, renderer, partialTicks, poseStack, buffer, packedLight)).isCanceled()) {
                        poseStack.translate(0.0F, 0.0F, 0.125F);
                        double d0 = Mth.lerp(partialTicks, livingEntity.xCloakO(), livingEntity.xCloak()) - Mth.lerp(partialTicks, livingEntity.xo, livingEntity.getX());
                        double d1 = Mth.lerp(partialTicks, livingEntity.yCloakO(), livingEntity.yCloak()) - Mth.lerp(partialTicks, livingEntity.yo, livingEntity.getY());
                        double d2 = Mth.lerp(partialTicks, livingEntity.zCloakO(), livingEntity.zCloak()) - Mth.lerp(partialTicks, livingEntity.zo, livingEntity.getZ());
                        float f = Mth.rotLerp(partialTicks, livingEntity.yBodyRotO, livingEntity.yBodyRot);
                        double d3 = Mth.sin(f * ((float) Math.PI / 180F));
                        double d4 = -Mth.cos(f * ((float) Math.PI / 180F));
                        float f1 = (float) d1 * 10.0F;
                        f1 = Mth.clamp(f1, -6.0F, 32.0F);
                        float f2 = (float) (d0 * d3 + d2 * d4) * 100.0F;
                        f2 = Mth.clamp(f2, 0.0F, 150.0F);
                        float f3 = (float) (d0 * d4 - d2 * d3) * 100.0F;
                        f3 = Mth.clamp(f3, -20.0F, 20.0F);
                        if (f2 < 0.0F) {
                            f2 = 0.0F;
                        }

                        float f4 = Mth.lerp(partialTicks, livingEntity.oBob(), livingEntity.bob());
                        f1 += Mth.sin(Mth.lerp(partialTicks, livingEntity.walkDistO, livingEntity.walkDist) * 6.0F) * 32.0F * f4;
                        if (livingEntity.isCrouching()) {
                            f1 += 25.0F;
                        }

                        poseStack.mulPose(Axis.XP.rotationDegrees(6.0F + f2 / 2.0F + f1));
                        poseStack.mulPose(Axis.ZP.rotationDegrees(f3 / 2.0F));
                        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - f3 / 2.0F));
                        VertexConsumer vertexconsumer = buffer.getBuffer(RenderType.entitySolid(playerskin.capeTexture()));
                        this.getParentModel().renderCloak(poseStack, vertexconsumer, packedLight, OverlayTexture.NO_OVERLAY);
                    }
                    poseStack.popPose();
                }
            }
        }
    }
}
