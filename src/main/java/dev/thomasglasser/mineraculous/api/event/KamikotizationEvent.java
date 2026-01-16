package dev.thomasglasser.mineraculous.api.event;

import dev.thomasglasser.mineraculous.api.world.kamikotization.KamikotizationData;
import java.util.Optional;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

/**
 * Fired when Kamikotization-related events occur.
 * See the subclasses for listening for de/transformation.
 *
 * @see Transform
 * @see Detransform
 */
public abstract class KamikotizationEvent extends LivingEvent {
    private final Optional<KamikotizationData> kamikotizationData;
    @Nullable
    private final ItemStack stack;

    @ApiStatus.Internal
    public KamikotizationEvent(LivingEntity entity, Optional<KamikotizationData> kamikotizationData, @Nullable ItemStack stack) {
        super(entity);
        this.kamikotizationData = kamikotizationData;
        this.stack = stack;
    }

    /**
     * Returns the kamikotization data.
     * 
     * @return The kamikotization data
     */
    public Optional<KamikotizationData> getKamikotizationData() {
        return kamikotizationData;
    }

    /**
     * Returns the item stack involved in the event.
     * 
     * @return The item stack involved in the event
     */
    public @Nullable ItemStack getStack() {
        return stack;
    }

    /**
     * Fired when {@link KamikotizationData} begins a transformation.
     * See the subclasses for the phases of transformation.
     *
     * @see Pre
     * @see Start
     * @see Finish
     */
    public static abstract class Transform extends KamikotizationEvent {
        @ApiStatus.Internal
        public Transform(LivingEntity entity, KamikotizationData kamikotizationData, ItemStack stack) {
            super(entity, Optional.of(kamikotizationData), stack);
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
            public Pre(LivingEntity entity, KamikotizationData kamikotizationData, ItemStack stack) {
                super(entity, kamikotizationData, stack);
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
            public Start(LivingEntity entity, KamikotizationData kamikotizationData, ItemStack stack, Optional<Integer> transformationFrames) {
                super(entity, kamikotizationData, stack);
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
            @ApiStatus.Internal
            public Finish(LivingEntity entity, KamikotizationData kamikotizationData, ItemStack stack) {
                super(entity, kamikotizationData, stack);
            }
        }
    }

    /**
     * Fired when {@link KamikotizationData} begins a detransformation.
     * See the subclasses for the phases of detransformation.
     *
     * @see Pre
     * @see Start
     * @see Finish
     */
    public static abstract class Detransform extends KamikotizationEvent {
        @ApiStatus.Internal
        public Detransform(LivingEntity entity, Optional<KamikotizationData> kamikotizationData, ItemStack stack) {
            super(entity, kamikotizationData, stack);
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
            public Pre(LivingEntity entity, KamikotizationData kamikotizationData, ItemStack stack) {
                super(entity, Optional.of(kamikotizationData), stack);
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
            public Start(LivingEntity entity, KamikotizationData kamikotizationData, ItemStack stack, Optional<Integer> detransformationFrames) {
                super(entity, Optional.of(kamikotizationData), stack);
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
            public Finish(LivingEntity entity, ItemStack stack) {
                super(entity, Optional.empty(), stack);
            }
        }
    }
}
