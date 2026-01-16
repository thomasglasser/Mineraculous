package dev.thomasglasser.mineraculous.api.event;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import org.jetbrains.annotations.ApiStatus;

/**
 * Fired when a player attempts to steal an item from another player.
 * See the subclasses for listening for the start and finish of stealing.
 *
 * @see Start
 * @see Finish
 */
public abstract class StealEvent extends PlayerEvent {
    private final Player target;

    @ApiStatus.Internal
    public StealEvent(Player player, Player target) {
        super(player);
        this.target = target;
    }

    /**
     * Returns the target of the steal attempt.
     * 
     * @return The target of the steal attempt
     */
    public Player getTarget() {
        return target;
    }

    /**
     * Fired when a player begins to attempt to steal an item from another player.
     * See the three subclasses for listening for before, during, and after stealing.
     *
     * @see Pre
     * @see Tick
     * @see Post
     */
    public abstract static class Start extends StealEvent implements ICancellableEvent {
        private int takeTicks;
        private int maxTakeTicks;

        @ApiStatus.Internal
        public Start(Player player, Player target, int takeTicks, int maxTakeTicks) {
            super(player, target);
            this.takeTicks = takeTicks;
            this.maxTakeTicks = maxTakeTicks;
        }

        /**
         * Returns the amount of take ticks that have passed.
         * 
         * @return The amount of take ticks that have passed
         */
        public int getTakeTicks() {
            return takeTicks;
        }

        /**
         * Returns the maximum amount of ticks that the steal will take.
         * 
         * @return The maximum amount of ticks that the steal will take
         */
        public int getMaxTakeTicks() {
            return maxTakeTicks;
        }

        /**
         * Sets the amount of ticks that have passed.
         * 
         * @param takeTicks The amount of ticks that have passed
         */
        public void setTakeTicks(int takeTicks) {
            this.takeTicks = takeTicks;
        }

        /**
         * Sets the maximum amount of ticks that the steal will take.
         * 
         * @param maxTakeTicks The maximum amount of ticks that the steal will take
         */
        public void setMaxTakeTicks(int maxTakeTicks) {
            this.maxTakeTicks = maxTakeTicks;
        }

        /**
         * Fired <b>before</b> the player begins stealing.
         * This can be used for altering stealing behavior or suppressing stealing.
         *
         * <p>This event is {@linkplain ICancellableEvent cancellable}.
         * If this event is cancelled, then the stealing will not begin and the corresponding
         * {@link Post} will not be fired.</p>
         *
         * <p>This event is fired on the {@linkplain NeoForge#EVENT_BUS main NeoForge event bus},
         * only on the {@linkplain LogicalSide#CLIENT logical client}.</p>
         */
        public static class Pre extends Start {
            @ApiStatus.Internal
            public Pre(Player player, Player target, int takeTicks, int maxTakeTicks) {
                super(player, target, takeTicks, maxTakeTicks);
            }
        }

        /**
         * Fired <b>while</b> the player is stealing, if the corresponding {@link Pre} is not cancelled.
         * This can be used for altering stealing behavior.
         *
         * <p>This event is not {@linkplain ICancellableEvent cancellable}.
         *
         * <p>This event is fired on the {@linkplain NeoForge#EVENT_BUS main NeoForge event bus},
         * only on the {@linkplain LogicalSide#CLIENT logical client}.</p>
         */
        public static class Tick extends Start {
            @ApiStatus.Internal
            public Tick(Player player, Player target, int takeTicks, int maxTakeTicks) {
                super(player, target, takeTicks, maxTakeTicks);
            }
        }

        /**
         * Fired <b>after</b> the player begins stealing, if the corresponding {@link Pre} is not cancelled.
         * This can be used for altering stealing behavior.
         *
         * <p>This event is not {@linkplain ICancellableEvent cancellable}.
         *
         * <p>This event is fired on the {@linkplain NeoForge#EVENT_BUS main NeoForge event bus},
         * only on the {@linkplain LogicalSide#CLIENT logical client}.</p>
         */
        public static class Post extends Start {
            @ApiStatus.Internal
            public Post(Player player, Player target, int takeTicks, int maxTakeTicks) {
                super(player, target, takeTicks, maxTakeTicks);
            }
        }
    }

    /**
     * Fired when a player finishes stealing from another player.
     *
     * <p>This event is {@linkplain ICancellableEvent cancellable}.</p>
     * If this event is cancelled, then the stealing will not complete.
     *
     * <p>This event is fired on the {@linkplain NeoForge#EVENT_BUS main NeoForge event bus},
     * only on the {@linkplain LogicalSide#SERVER logical server}.</p>
     */
    public static class Finish extends StealEvent implements ICancellableEvent {
        private final ItemStack stack;

        @ApiStatus.Internal
        public Finish(Player player, Player target, ItemStack stack) {
            super(player, target);
            this.stack = stack;
        }

        /**
         * Returns the item that was stolen.
         * 
         * @return The item that was stolen
         */
        public ItemStack getStack() {
            return stack;
        }
    }
}
