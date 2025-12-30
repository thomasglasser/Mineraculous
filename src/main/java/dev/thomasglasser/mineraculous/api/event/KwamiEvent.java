package dev.thomasglasser.mineraculous.api.event;

import dev.thomasglasser.mineraculous.impl.world.entity.Kwami;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.ApiStatus;

/**
 * Fired when Kwami-related events occur.
 * See the subclass for listening for eating.
 *
 * @see Eat
 */
public abstract class KwamiEvent extends Event {
    private final Kwami kwami;

    @ApiStatus.Internal
    public KwamiEvent(Kwami kwami) {
        this.kwami = kwami;
    }

    /**
     * Returns the kwami involved in the event.
     * 
     * @return The kwami involved in the event
     */
    public Kwami getKwami() {
        return kwami;
    }

    /**
     * Fired when a Kwami eats an item.
     * See the subclasses for listening for the start and finish of eating.
     *
     * @see Start
     * @see Finish
     */
    public abstract static class Eat extends KwamiEvent {
        private final ItemStack stack;

        @ApiStatus.Internal
        public Eat(Kwami kwami, ItemStack stack) {
            super(kwami);
            this.stack = stack;
        }

        /**
         * Returns the item being eaten.
         * 
         * @return The item being eaten
         */
        public ItemStack getStack() {
            return stack;
        }

        /**
         * Fired when a Kwami begins eating an item.
         * This can be used for altering eating behavior or suppressing eating.
         *
         * <p>This event is {@linkplain ICancellableEvent cancellable}.
         * If this event is cancelled, then the eating will not begin and the corresponding
         * {@link Finish} will not be fired.</p>
         *
         * <p>This event is fired on the {@linkplain NeoForge#EVENT_BUS main NeoForge event bus},
         * only on the {@linkplain LogicalSide#SERVER logical server}.</p>
         */
        public static class Start extends Eat implements ICancellableEvent {
            private int eatTicks;

            @ApiStatus.Internal
            public Start(Kwami kwami, ItemStack stack, int eatTicks) {
                super(kwami, stack);
                this.eatTicks = eatTicks;
            }

            /**
             * Returns the amount of ticks that the eating will take.
             * 
             * @return The amount of ticks that the eating will take
             */
            public int getEatTicks() {
                return eatTicks;
            }

            /**
             * Sets the amount of ticks that the eating will take.
             * 
             * @param eatTicks The amount of ticks that the eating will take
             */
            public void setEatTicks(int eatTicks) {
                this.eatTicks = eatTicks;
            }
        }

        /**
         * Fired when a Kwami finishes eating an item.
         * See the subclasses for listening for before and after eating.
         *
         * @see Pre
         * @see Post
         */
        public abstract static class Finish extends Eat {
            @ApiStatus.Internal
            public Finish(Kwami kwami, ItemStack stack) {
                super(kwami, stack);
            }

            /**
             * Fired <b>before</b> the Kwami finishes eating.
             * This can be used for altering eating behavior or suppressing eating completion.
             *
             * <p>This event is {@linkplain ICancellableEvent cancellable}.
             * If this event is cancelled, then the eating will not complete and the corresponding
             * {@link Post} will not be fired.</p>
             *
             * <p>This event is fired on the {@linkplain NeoForge#EVENT_BUS main NeoForge event bus},
             * only on the {@linkplain LogicalSide#SERVER logical server}.</p>
             */
            public static class Pre extends Finish implements ICancellableEvent {
                @ApiStatus.Internal
                public Pre(Kwami kwami, ItemStack stack) {
                    super(kwami, stack);
                }
            }

            /**
             * Fired <b>after</b> the Kwami finishes eating, if the corresponding {@link Pre} is not cancelled.
             * This can be used for altering eating behavior.
             *
             * <p>This event is not {@linkplain ICancellableEvent cancellable}.</p>
             *
             * <p>This event is fired on the {@linkplain NeoForge#EVENT_BUS main NeoForge event bus},
             * only on the {@linkplain LogicalSide#SERVER logical server}.</p>
             */
            public static class Post extends Finish {
                private boolean charged;

                @ApiStatus.Internal
                public Post(Kwami kwami, ItemStack stack, boolean charged) {
                    super(kwami, stack);
                    this.charged = charged;
                }

                /**
                 * Returns whether the Kwami was charged by the food.
                 * 
                 * @return Whether the Kwami was charged by the food
                 */
                public boolean isCharged() {
                    return charged;
                }

                /**
                 * Sets whether the Kwami was charged by the food.
                 * 
                 * @param charged Whether the Kwami was charged by the food
                 */
                public void setCharged(boolean charged) {
                    this.charged = charged;
                }
            }
        }
    }
}
