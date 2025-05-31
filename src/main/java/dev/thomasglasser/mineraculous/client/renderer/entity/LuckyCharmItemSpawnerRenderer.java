package dev.thomasglasser.mineraculous.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.thomasglasser.mineraculous.world.entity.LuckyCharmItemSpawner;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemEntityRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class LuckyCharmItemSpawnerRenderer extends EntityRenderer<LuckyCharmItemSpawner> {
    private static final float ROTATION_SPEED = 40.0F;
    private static final int TICKS_SCALING = 50;
    private final ItemRenderer itemRenderer;

    public LuckyCharmItemSpawnerRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.itemRenderer = context.getItemRenderer();
    }

    public ResourceLocation getTextureLocation(LuckyCharmItemSpawner p_338515_) {
        return TextureAtlas.LOCATION_BLOCKS;
    }

    public void render(LuckyCharmItemSpawner entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        ItemStack itemstack = entity.getItem();
        if (!itemstack.isEmpty()) {
            poseStack.pushPose();
            if (entity.tickCount <= TICKS_SCALING) {
                float f = Math.min((float) entity.tickCount + partialTick, TICKS_SCALING) / TICKS_SCALING;
                poseStack.scale(f, f, f);
            }

            Level level = entity.level();
            float f1 = Mth.wrapDegrees((float) (level.getGameTime() - 1L)) * ROTATION_SPEED;
            float f2 = Mth.wrapDegrees((float) level.getGameTime()) * ROTATION_SPEED;
            poseStack.mulPose(Axis.YP.rotationDegrees(Mth.rotLerp(partialTick, f1, f2)));
            ItemEntityRenderer.renderMultipleFromCount(this.itemRenderer, poseStack, bufferSource, LightTexture.FULL_BRIGHT, itemstack, level.random, level);
            poseStack.popPose();
        }
    }
}
