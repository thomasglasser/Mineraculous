package dev.thomasglasser.mineraculous.api.event;

import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.api.world.entity.MineraculousEntityUtils;
import dev.thomasglasser.mineraculous.api.world.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.api.world.miraculous.MiraculousData;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

/**
 * Fired when Miraculous-related events occur.
 * See the subclasses for listening for de/transformation or renouncing.
 *
 * @see Transform
 * @see Detransform
 * @see Renounce
 */
public abstract class MiraculousEvent extends LivingEvent {
    private final Holder<Miraculous> miraculous;
    private final MiraculousData miraculousData;
    private final ItemStack stack;

    @ApiStatus.Internal
    public MiraculousEvent(LivingEntity entity, Holder<Miraculous> miraculous, MiraculousData miraculousData, ItemStack stack) {
        super(entity);
        this.miraculous = miraculous;
        this.miraculousData = miraculousData;
        this.stack = stack;
    }

    /**
     * Returns the miraculous being used.
     * 
     * @return The miraculous being used
     */
    public Holder<Miraculous> getMiraculous() {
        return miraculous;
    }

    /**
     * Returns the miraculous data.
     * 
     * @return The miraculous data
     */
    public MiraculousData getMiraculousData() {
        return miraculousData;
    }

    /**
     * Returns the miraculous stack.
     * 
     * @return The miraculous stack
     */
    public ItemStack getStack() {
        return stack;
    }

    /**
     * Fired when {@link MiraculousData} begins a transformation.
     * See the subclasses for the phases of transformation.
     *
     * @see Pre
     * @see Start
     * @see Finish
     */
    public static abstract class Transform extends MiraculousEvent {
        @ApiStatus.Internal
        public Transform(LivingEntity entity, Holder<Miraculous> miraculous, MiraculousData miraculousData, ItemStack stack) {
            super(entity, miraculous, miraculousData, stack);
        }

        /**
         * Fired <b>before</b> the entity triggers transformation.
         * This can be used for performing additional effects or suppressing transformation triggering.
         *
         * <p>This event is {@linkplain ICancellableEvent cancellable}.
         * If this event is cancelled, then transformation will not be triggered and the corresponding
         * events will not be fired.</p>
         *
         * <p>This event is fired on the {@linkplain NeoForge#EVENT_BUS main NeoForge event bus},
         * only on the {@linkplain LogicalSide#SERVER logical server}.</p>
         */
        public static class Trigger extends Transform implements ICancellableEvent {
            @ApiStatus.Internal
            public Trigger(LivingEntity entity, Holder<Miraculous> miraculous, MiraculousData miraculousData, ItemStack stack) {
                super(entity, miraculous, miraculousData, stack);
            }
        }

        /**
         * Fired <b>before</b> the entity begins transformation.
         * This can be used for performing additional effects or suppressing transformation.
         *
         * <p>This event is {@linkplain ICancellableEvent cancellable}.
         * If this event is cancelled, then the entity will not be transformed and the corresponding
         * events will not be fired.</p>
         *
         * <p>This event is fired on the {@linkplain NeoForge#EVENT_BUS main NeoForge event bus},
         * only on the {@linkplain LogicalSide#SERVER logical server}.</p>
         */
        public static class Pre extends Transform implements ICancellableEvent {
            @ApiStatus.Internal
            public Pre(LivingEntity entity, Holder<Miraculous> miraculous, MiraculousData miraculousData, ItemStack stack) {
                super(entity, miraculous, miraculousData, stack);
            }
        }

        /**
         * Fired when the entity begins transformation.
         * This can be used for performing additional effects.
         *
         * <p>This event is not {@linkplain ICancellableEvent cancellable}.</p>
         *
         * <p>This event is fired on the {@linkplain NeoForge#EVENT_BUS main NeoForge event bus},
         * only on the {@linkplain LogicalSide#SERVER logical server}.</p>
         */
        public static class Start extends Transform {
            private Optional<Integer> transformationFrames;

            @ApiStatus.Internal
            public Start(LivingEntity entity, Holder<Miraculous> miraculous, MiraculousData miraculousData, ItemStack stack, Optional<Integer> transformationFrames) {
                super(entity, miraculous, miraculousData, stack);
                this.transformationFrames = transformationFrames;
            }

            /**
             * Returns the number of frames the transformation will take.
             * 
             * @return The number of frames the transformation will take
             */
            public Optional<Integer> getTransformationFrames() {
                return transformationFrames;
            }

            /**
             * Sets the number of frames the transformation will take.
             * 
             * @param transformationFrames The number of frames the transformation will take
             */
            public void setTransformationFrames(Optional<Integer> transformationFrames) {
                this.transformationFrames = transformationFrames;
            }
        }

        /**
         * Fired when the entity finishes transformation.
         * This can be used for performing additional effects.
         *
         * <p>This event is not {@linkplain ICancellableEvent cancellable}.</p>
         *
         * <p>This event is fired on the {@linkplain NeoForge#EVENT_BUS main NeoForge event bus},
         * only on the {@linkplain LogicalSide#SERVER logical server}.</p>
         */
        public static class Finish extends Transform {
            private boolean shouldSetLastUsed = true;

            @ApiStatus.Internal
            public Finish(LivingEntity entity, Holder<Miraculous> miraculous, MiraculousData miraculousData, ItemStack stack) {
                super(entity, miraculous, miraculousData, stack);
            }

            /**
             * Returns whether the last used time should be set.
             * 
             * @return Whether the last used time should be set
             */
            public boolean shouldSetLastUsed() {
                return shouldSetLastUsed;
            }

            /**
             * Sets whether the last used time should be set.
             * 
             * @param shouldSetLastUsed Whether the last used time should be set
             */
            public void setShouldSetLastUsed(boolean shouldSetLastUsed) {
                this.shouldSetLastUsed = shouldSetLastUsed;
            }
        }
    }

    /**
     * Fired when {@link MiraculousData} begins a detransformation.
     * See the subclasses for the phases of detransformation.
     *
     * @see Pre
     * @see Start
     * @see Finish
     */
    public static abstract class Detransform extends MiraculousEvent {
        private final boolean removed;

        @ApiStatus.Internal
        public Detransform(LivingEntity entity, Holder<Miraculous> miraculous, MiraculousData miraculousData, ItemStack stack, boolean removed) {
            super(entity, miraculous, miraculousData, stack);
            this.removed = removed;
        }

        /**
         * Returns whether the miraculous was removed.
         * 
         * @return Whether the miraculous was removed
         */
        public boolean wasRemoved() {
            return removed;
        }

        /**
         * Fired <b>before</b> the entity begins detransformation.
         * This can be used for performing additional effects or suppressing detransformation.
         *
         * <p>This event is {@linkplain ICancellableEvent cancellable}.
         * If this event is cancelled, then the entity will not be detransformed and the corresponding
         * events will not be fired.</p>
         *
         * <p>This event is fired on the {@linkplain NeoForge#EVENT_BUS main NeoForge event bus},
         * only on the {@linkplain LogicalSide#SERVER logical server}.</p>
         */
        public static class Pre extends Detransform implements ICancellableEvent {
            @ApiStatus.Internal
            public Pre(LivingEntity entity, Holder<Miraculous> miraculous, MiraculousData miraculousData, ItemStack stack, boolean removed) {
                super(entity, miraculous, miraculousData, stack, removed);
            }
        }

        /**
         * Fired when the entity begins detransformation.
         * This can be used for performing additional effects.
         *
         * <p>This event is not {@linkplain ICancellableEvent cancellable}.</p>
         *
         * <p>This event is fired on the {@linkplain NeoForge#EVENT_BUS main NeoForge event bus},
         * only on the {@linkplain LogicalSide#SERVER logical server}.</p>
         */
        public static class Start extends Detransform {
            private Optional<Integer> detransformationFrames;

            @ApiStatus.Internal
            public Start(LivingEntity entity, Holder<Miraculous> miraculous, MiraculousData miraculousData, ItemStack stack, Optional<Integer> detransformationFrames) {
                super(entity, miraculous, miraculousData, stack, false);
                this.detransformationFrames = detransformationFrames;
            }

            /**
             * Returns the number of frames the detransformation will take.
             * 
             * @return The number of frames the detransformation will take
             */
            public Optional<Integer> getDetransformationFrames() {
                return detransformationFrames;
            }

            /**
             * Sets the number of frames the detransformation will take.
             * 
             * @param detransformationFrames The number of frames the detransformation will take
             */
            public void setDetransformationFrames(Optional<Integer> detransformationFrames) {
                this.detransformationFrames = detransformationFrames;
            }
        }

        /**
         * Fired when the entity finishes detransformation.
         * This can be used for performing additional effects.
         *
         * <p>This event is not {@linkplain ICancellableEvent cancellable}.</p>
         *
         * <p>This event is fired on the {@linkplain NeoForge#EVENT_BUS main NeoForge event bus},
         * only on the {@linkplain LogicalSide#SERVER logical server}.</p>
         */
        public static class Finish extends Detransform {
            @ApiStatus.Internal
            public Finish(LivingEntity entity, Holder<Miraculous> miraculous, MiraculousData miraculousData, ItemStack stack, boolean removed) {
                super(entity, miraculous, miraculousData, stack, removed);
            }
        }
    }

    /**
     * Fired when a player renounces a miraculous.
     * See the subclasses for listening for before and after renouncing.
     *
     * @see Pre
     * @see Post
     */
    public static abstract class Renounce extends MiraculousEvent {
        @Nullable
        private final MineraculousEntityUtils.KwamiLike kwami;

        @ApiStatus.Internal
        public Renounce(LivingEntity entity, Holder<Miraculous> miraculous, ItemStack stack, @Nullable MineraculousEntityUtils.KwamiLike kwami) {
            super(entity, miraculous, entity.getData(MineraculousAttachmentTypes.MIRACULOUSES).get(miraculous), stack);
            this.kwami = kwami;
        }

        /**
         * Returns the kwami being renounced.
         * 
         * @return The kwami being renounced
         */
        @Nullable
        public MineraculousEntityUtils.KwamiLike getKwami() {
            return kwami;
        }

        /**
         * Fired <b>before</b> the player renounces the miraculous.
         * This can be used for performing additional effects or suppressing renouncing.
         *
         * <p>This event is {@linkplain ICancellableEvent cancellable}.
         * If this event is cancelled, then the miraculous will not be renounced and the corresponding
         * {@link Post} will not be fired.</p>
         *
         * <p>This event is fired on the {@linkplain NeoForge#EVENT_BUS main NeoForge event bus},
         * only on the {@linkplain LogicalSide#SERVER logical server}.</p>
         */
        public static class Pre extends Renounce implements ICancellableEvent {
            private boolean shouldRequireKwami;

            @ApiStatus.Internal
            public Pre(LivingEntity entity, Holder<Miraculous> miraculous, ItemStack stack, @Nullable MineraculousEntityUtils.KwamiLike kwami, boolean shouldRequireKwami) {
                super(entity, miraculous, stack, kwami);
                this.shouldRequireKwami = shouldRequireKwami;
            }

            /**
             * Returns whether the kwami is required to renounce the miraculous.
             * 
             * @return Whether the kwami is required to renounce the miraculous
             */
            public boolean shouldRequireKwami() {
                return shouldRequireKwami;
            }

            /**
             * Sets whether the kwami is required to renounce the miraculous.
             * 
             * @param shouldRequireKwami Whether the kwami is required to renounce the miraculous
             */
            public void setShouldRequireKwami(boolean shouldRequireKwami) {
                this.shouldRequireKwami = shouldRequireKwami;
            }
        }

        /**
         * Fired <b>after</b> the player renounces the miraculous, if the corresponding {@link Pre} is not cancelled.
         * This can be used for performing additional effects.
         *
         * <p>This event is not {@linkplain ICancellableEvent cancellable}.</p>
         *
         * <p>This event is fired on the {@linkplain NeoForge#EVENT_BUS main NeoForge event bus},
         * only on the {@linkplain LogicalSide#SERVER logical server}.</p>
         */
        public static class Post extends Renounce {
            @ApiStatus.Internal
            public Post(LivingEntity entity, Holder<Miraculous> miraculous, ItemStack stack, @Nullable MineraculousEntityUtils.KwamiLike kwami) {
                super(entity, miraculous, stack, kwami);
            }
        }
    }
}
