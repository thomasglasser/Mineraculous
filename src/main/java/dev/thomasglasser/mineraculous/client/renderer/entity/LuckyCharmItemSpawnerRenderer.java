package dev.thomasglasser.mineraculous.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.thomasglasser.mineraculous.world.entity.LuckyCharmItemSpawner;
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

    public LuckyCharmItemSpawnerRenderer(EntityRendererProvider.Context p_338603_) {
        super(p_338603_);
        this.itemRenderer = p_338603_.getItemRenderer();
    }

    public ResourceLocation getTextureLocation(LuckyCharmItemSpawner p_338515_) {
        return TextureAtlas.LOCATION_BLOCKS;
    }

    public void render(LuckyCharmItemSpawner p_338815_, float p_338631_, float p_338539_, PoseStack p_338440_, MultiBufferSource p_338413_, int p_338541_) {
        ItemStack itemstack = p_338815_.getItem();
        if (!itemstack.isEmpty()) {
            p_338440_.pushPose();
            if (p_338815_.tickCount <= TICKS_SCALING) {
                float f = Math.min((float) p_338815_.tickCount + p_338539_, TICKS_SCALING) / TICKS_SCALING;
                p_338440_.scale(f, f, f);
            }

            Level level = p_338815_.level();
            float f1 = Mth.wrapDegrees((float) (level.getGameTime() - 1L)) * ROTATION_SPEED;
            float f2 = Mth.wrapDegrees((float) level.getGameTime()) * ROTATION_SPEED;
            p_338440_.mulPose(Axis.YP.rotationDegrees(Mth.rotLerp(p_338539_, f1, f2)));
            ItemEntityRenderer.renderMultipleFromCount(this.itemRenderer, p_338440_, p_338413_, 15728880, itemstack, level.random, level);
            p_338440_.popPose();
        }
    }
}
