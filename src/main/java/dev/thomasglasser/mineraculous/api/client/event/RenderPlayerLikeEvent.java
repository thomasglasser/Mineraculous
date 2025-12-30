package dev.thomasglasser.mineraculous.api.client.event;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.thomasglasser.mineraculous.impl.client.renderer.entity.PlayerLikeRenderer;
import dev.thomasglasser.mineraculous.impl.world.entity.PlayerLike;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.ApiStatus;

/**
 * Fired when a player-like entity is being rendered.
 * See the two subclasses for listening for before and after rendering,
 * or the {@link RenderCape} subclass for rendering the entity's cape.
 *
 * @see Pre
 * @see Post
 * @see RenderCape
 * @see PlayerLikeRenderer
 *
 * @param <T> The type of player-like entity being rendered
 */
public abstract class RenderPlayerLikeEvent<T extends LivingEntity & PlayerLike> extends Event {
    private final T playerLike;
    private final PlayerLikeRenderer<T> renderer;
    private final float partialTick;
    private final PoseStack poseStack;
    private final MultiBufferSource multiBufferSource;
    private final int packedLight;

    @ApiStatus.Internal
    public RenderPlayerLikeEvent(T playerLike, PlayerLikeRenderer<T> renderer, float partialTick, PoseStack poseStack, MultiBufferSource multiBufferSource, int packedLight) {
        this.playerLike = playerLike;
        this.renderer = renderer;
        this.partialTick = partialTick;
        this.poseStack = poseStack;
        this.multiBufferSource = multiBufferSource;
        this.packedLight = packedLight;
    }

    /**
     * Returns the player-like entity being rendered.
     * 
     * @return The player-like entity being rendered
     */
    public T getPlayerLike() {
        return playerLike;
    }

    /**
     * Returns the player-like renderer used to render the entity.
     * 
     * @return The player-like renderer used to render the entity
     */
    public PlayerLikeRenderer<T> getRenderer() {
        return renderer;
    }

    /**
     * Returns the partial tick of the current render cycle.
     * 
     * @return The partial tick of the current render cycle
     */
    public float getPartialTick() {
        return partialTick;
    }

    /**
     * Returns the pose stack used for rendering.
     * 
     * @return The pose stack used for rendering
     */
    public PoseStack getPoseStack() {
        return poseStack;
    }

    /**
     * Returns the source of rendering buffers.
     * 
     * @return The source of rendering buffers
     */
    public MultiBufferSource getMultiBufferSource() {
        return multiBufferSource;
    }

    /**
     * Returns the amount of packed (sky and block) light for rendering.
     * 
     * @return The amount of packed (sky and block) light for rendering
     */
    public int getPackedLight() {
        return packedLight;
    }

    /**
     * Fired <b>before</b> the entity is rendered.
     * This can be used for rendering additional effects or suppressing rendering.
     *
     * <p>This event is {@linkplain ICancellableEvent cancellable}.
     * If this event is cancelled, then the entity will not be rendered and the corresponding
     * {@link RenderPlayerLikeEvent.Post} will not be fired.</p>
     *
     * <p>This event is fired on the {@linkplain NeoForge#EVENT_BUS main NeoForge event bus},
     * only on the {@linkplain LogicalSide#CLIENT logical client}.</p>
     *
     * @param <T> The type of player-like entity being rendered
     */
    public static class Pre<T extends LivingEntity & PlayerLike> extends RenderPlayerLikeEvent<T> implements ICancellableEvent {
        @ApiStatus.Internal
        public Pre(T playerLike, PlayerLikeRenderer<T> renderer, float partialTick, PoseStack poseStack, MultiBufferSource multiBufferSource, int packedLight) {
            super(playerLike, renderer, partialTick, poseStack, multiBufferSource, packedLight);
        }
    }

    /**
     * Fires <b>after</b> the entity's cape is rendered.
     * This can be used for rendering additional effects or suppressing rendering.
     *
     * <p>This event is {@linkplain ICancellableEvent cancellable}.</p>
     * If this event is cancelled, then the cape will not be rendered.
     *
     * <p>This event is fired on the {@linkplain NeoForge#EVENT_BUS main NeoForge event bus},
     * only on the {@linkplain LogicalSide#CLIENT logical client}.</p>
     *
     * @param <T> The type of player-like entity being rendered
     */
    public static class RenderCape<T extends LivingEntity & PlayerLike> extends RenderPlayerLikeEvent<T> implements ICancellableEvent {
        @ApiStatus.Internal
        public RenderCape(T playerLike, PlayerLikeRenderer<T> renderer, float partialTick, PoseStack poseStack, MultiBufferSource multiBufferSource, int packedLight) {
            super(playerLike, renderer, partialTick, poseStack, multiBufferSource, packedLight);
        }
    }

    /**
     * Fired <b>after</b> the entity is rendered, if the corresponding {@link RenderPlayerLikeEvent.Pre} is not cancelled.
     *
     * <p>This event is not {@linkplain ICancellableEvent cancellable}.</p>
     *
     * <p>This event is fired on the {@linkplain NeoForge#EVENT_BUS main NeoForge event bus},
     * only on the {@linkplain LogicalSide#CLIENT logical client}.</p>
     *
     * @param <T> The type of player-like entity being rendered
     */
    public static class Post<T extends LivingEntity & PlayerLike> extends RenderPlayerLikeEvent<T> {
        @ApiStatus.Internal
        public Post(T playerLike, PlayerLikeRenderer<T> renderer, float partialTick, PoseStack poseStack, MultiBufferSource multiBufferSource, int packedLight) {
            super(playerLike, renderer, partialTick, poseStack, multiBufferSource, packedLight);
        }
    }
}
