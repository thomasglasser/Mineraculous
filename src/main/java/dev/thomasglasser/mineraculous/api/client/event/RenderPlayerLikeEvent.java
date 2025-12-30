package dev.thomasglasser.mineraculous.api.client.event;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.thomasglasser.mineraculous.impl.client.renderer.entity.PlayerLikeRenderer;
import dev.thomasglasser.mineraculous.impl.world.entity.PlayerLike;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

public abstract class RenderPlayerLikeEvent<T extends LivingEntity & PlayerLike> extends Event {
    private final T playerLike;
    private final PlayerLikeRenderer<T> renderer;
    private final float partialTick;
    private final PoseStack poseStack;
    private final MultiBufferSource multiBufferSource;
    private final int packedLight;

    public RenderPlayerLikeEvent(T playerLike, PlayerLikeRenderer<T> renderer, float partialTick, PoseStack poseStack, MultiBufferSource multiBufferSource, int packedLight) {
        this.playerLike = playerLike;
        this.renderer = renderer;
        this.partialTick = partialTick;
        this.poseStack = poseStack;
        this.multiBufferSource = multiBufferSource;
        this.packedLight = packedLight;
    }

    public T getPlayerLike() {
        return playerLike;
    }

    public PlayerLikeRenderer<T> getRenderer() {
        return renderer;
    }

    public float getPartialTick() {
        return partialTick;
    }

    public PoseStack getPoseStack() {
        return poseStack;
    }

    public MultiBufferSource getMultiBufferSource() {
        return multiBufferSource;
    }

    public int getPackedLight() {
        return packedLight;
    }

    public static class Pre<T extends LivingEntity & PlayerLike> extends RenderPlayerLikeEvent<T> implements ICancellableEvent {
        public Pre(T playerLike, PlayerLikeRenderer<T> renderer, float partialTick, PoseStack poseStack, MultiBufferSource multiBufferSource, int packedLight) {
            super(playerLike, renderer, partialTick, poseStack, multiBufferSource, packedLight);
        }
    }

    public static class RenderCape<T extends LivingEntity & PlayerLike> extends RenderPlayerLikeEvent<T> implements ICancellableEvent {
        public RenderCape(T playerLike, PlayerLikeRenderer<T> renderer, float partialTick, PoseStack poseStack, MultiBufferSource multiBufferSource, int packedLight) {
            super(playerLike, renderer, partialTick, poseStack, multiBufferSource, packedLight);
        }
    }

    public static class Post<T extends LivingEntity & PlayerLike> extends RenderPlayerLikeEvent<T> {
        public Post(T playerLike, PlayerLikeRenderer<T> renderer, float partialTick, PoseStack poseStack, MultiBufferSource multiBufferSource, int packedLight) {
            super(playerLike, renderer, partialTick, poseStack, multiBufferSource, packedLight);
        }
    }
}
