package dev.thomasglasser.mineraculous.api.event;

import dev.thomasglasser.mineraculous.api.world.item.MineraculousItemUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

/**
 * Fired when an item breaks.
 * See the subclasses for listening for before, during, and after breaking.
 *
 * @see Pre
 * @see DetermineDamage
 * @see Post
 */
public abstract class ItemBreakEvent extends Event {
    private final ItemStack stack;
    private final ServerLevel level;
    private final Vec3 pos;
    @Nullable
    private final LivingEntity breaker;

    @ApiStatus.Internal
    public ItemBreakEvent(ItemStack stack, ServerLevel level, Vec3 pos, @Nullable LivingEntity breaker) {
        this.stack = stack;
        this.level = level;
        this.pos = pos;
        this.breaker = breaker;
    }

    /**
     * Returns the item stack being broken.
     * 
     * @return The item stack being broken
     */
    public ItemStack getStack() {
        return stack;
    }

    /**
     * Returns the level where the item is being broken.
     * 
     * @return The level where the item is being broken
     */
    public ServerLevel getLevel() {
        return level;
    }

    /**
     * Returns the position where the item is being broken.
     * 
     * @return The position where the item is being broken
     */
    public Vec3 getPos() {
        return pos;
    }

    /**
     * Returns the entity breaking the item, if any.
     * 
     * @return The entity breaking the item, if any
     */
    @Nullable
    public LivingEntity getBreaker() {
        return breaker;
    }

    /**
     * Fired <b>before</b> the item breaks.
     * This can be used for altering breaking behavior or suppressing breaking.
     *
     * <p>This event is {@linkplain ICancellableEvent cancellable}.
     * If this event is cancelled, then the item will not break and the corresponding
     * {@link Post} will not be fired.</p>
     *
     * <p>This event is fired on the {@linkplain NeoForge#EVENT_BUS main NeoForge event bus},
     * only on the {@linkplain LogicalSide#SERVER logical server}.</p>
     */
    public static class Pre extends ItemBreakEvent implements ICancellableEvent {
        @ApiStatus.Internal
        public Pre(ItemStack stack, ServerLevel level, Vec3 pos, @Nullable LivingEntity breaker) {
            super(stack, level, pos, breaker);
        }
    }

    /**
     * Fired to determine the damage dealt to the item being broken.
     *
     * <p>This event is not {@linkplain ICancellableEvent cancellable}.</p>
     *
     * <p>This event is fired on the {@linkplain NeoForge#EVENT_BUS main NeoForge event bus},
     * only on the {@linkplain LogicalSide#SERVER logical server}.</p>
     */
    public static class DetermineDamage extends ItemBreakEvent {
        private int damage;

        @ApiStatus.Internal
        public DetermineDamage(ItemStack stack, ServerLevel level, Vec3 pos, @Nullable LivingEntity breaker, int damage) {
            super(stack, level, pos, breaker);
            this.damage = damage;
        }

        /**
         * Returns the damage to be dealt.
         * 
         * @return The damage to be dealt
         */
        public int getDamage() {
            return damage;
        }

        /**
         * Sets the damage to be dealt.
         * 
         * @param damage The damage to be dealt
         */
        public void setDamage(int damage) {
            this.damage = damage;
        }
    }

    /**
     * Fired <b>after</b> the item breaks, if the corresponding {@link Pre} is not cancelled.
     * This can be used for altering breaking behavior.
     *
     * <p>This event is not {@linkplain ICancellableEvent cancellable}.</p>
     *
     * <p>This event is fired on the {@linkplain NeoForge#EVENT_BUS main NeoForge event bus},
     * only on the {@linkplain LogicalSide#SERVER logical server}.</p>
     */
    public static class Post extends ItemBreakEvent {
        private MineraculousItemUtils.BreakResult breakResult;

        @ApiStatus.Internal
        public Post(ItemStack stack, ServerLevel level, Vec3 pos, @Nullable LivingEntity breaker, MineraculousItemUtils.BreakResult breakResult) {
            super(stack, level, pos, breaker);
            this.breakResult = breakResult;
        }

        /**
         * Returns the result of the break.
         * 
         * @return The result of the break
         */
        public MineraculousItemUtils.BreakResult getBreakResult() {
            return breakResult;
        }

        /**
         * Sets the result of the break.
         * 
         * @param breakResult The result of the break
         */
        public void setBreakResult(MineraculousItemUtils.BreakResult breakResult) {
            this.breakResult = breakResult;
        }
    }
}
