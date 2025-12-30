package dev.thomasglasser.mineraculous.api.event;

import dev.thomasglasser.mineraculous.api.world.ability.Ability;
import dev.thomasglasser.mineraculous.api.world.ability.AbilityData;
import dev.thomasglasser.mineraculous.api.world.ability.context.AbilityContext;
import dev.thomasglasser.mineraculous.api.world.ability.handler.AbilityHandler;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

/**
 * Fired when Ability-related events occur.
 * See the subclasses for listening for ability performance.
 *
 * @see Perform
 */
public abstract class AbilityEvent extends LivingEvent {
    private final Holder<Ability> ability;
    private final AbilityData abilityData;

    @ApiStatus.Internal
    public AbilityEvent(LivingEntity entity, Holder<Ability> ability, AbilityData abilityData) {
        super(entity);
        this.ability = ability;
        this.abilityData = abilityData;
    }

    /**
     * Returns the ability involved in the event.
     * 
     * @return The ability involved in the event
     */
    public Holder<Ability> getAbility() {
        return ability;
    }

    /**
     * Returns the ability data.
     * 
     * @return The ability data
     */
    public AbilityData getAbilityData() {
        return abilityData;
    }

    /**
     * Fired when an ability is performed.
     * See the subclasses for listening for before and after performance.
     *
     * @see Pre
     * @see Post
     */
    public abstract static class Perform extends AbilityEvent {
        private final AbilityHandler handler;
        @Nullable
        private final AbilityContext context;

        @ApiStatus.Internal
        public Perform(LivingEntity entity, Holder<Ability> ability, AbilityData abilityData, AbilityHandler handler, @Nullable AbilityContext context) {
            super(entity, ability, abilityData);
            this.handler = handler;
            this.context = context;
        }

        /**
         * Returns the ability handler.
         * 
         * @return The ability handler
         */
        public AbilityHandler getAbilityHandler() {
            return handler;
        }

        /**
         * Returns the ability context.
         * 
         * @return The ability context
         */
        public @Nullable AbilityContext getAbilityContext() {
            return context;
        }

        /**
         * Fired <b>before</b> the ability is performed.
         * This can be used for altering ability behavior or suppressing performance.
         *
         * <p>This event is not {@linkplain net.neoforged.bus.api.ICancellableEvent cancellable}.</p>
         *
         * <p>This event is fired on the {@linkplain NeoForge#EVENT_BUS main NeoForge event bus},
         * only on the {@linkplain LogicalSide#SERVER logical server}.</p>
         */
        public static class Pre extends Perform {
            @Nullable
            private Ability.State state = null;

            @ApiStatus.Internal
            public Pre(LivingEntity entity, Holder<Ability> ability, AbilityData abilityData, AbilityHandler handler, @Nullable AbilityContext context) {
                super(entity, ability, abilityData, handler, context);
            }

            /**
             * Returns the state of the ability.
             * 
             * @return The state of the ability
             */
            public @Nullable Ability.State getState() {
                return state;
            }

            /**
             * Sets the state of the ability.
             * 
             * @param state The state of the ability
             */
            public void setState(@Nullable Ability.State state) {
                this.state = state;
            }
        }

        /**
         * Fired <b>after</b> the ability is performed.
         * This can be used for altering ability behavior.
         *
         * <p>This event is not {@linkplain net.neoforged.bus.api.ICancellableEvent cancellable}.</p>
         *
         * <p>This event is fired on the {@linkplain NeoForge#EVENT_BUS main NeoForge event bus},
         * only on the {@linkplain LogicalSide#SERVER logical server}.</p>
         */
        public static class Post extends Perform {
            private Ability.State state;

            @ApiStatus.Internal
            public Post(LivingEntity entity, Holder<Ability> ability, AbilityData abilityData, AbilityHandler handler, AbilityContext context, Ability.State state) {
                super(entity, ability, abilityData, handler, context);
                this.state = state;
            }

            /**
             * Returns the state of the ability.
             * 
             * @return The state of the ability
             */
            public Ability.State getState() {
                return state;
            }

            /**
             * Sets the state of the ability.
             * 
             * @param state The state of the ability
             */
            public void setState(Ability.State state) {
                this.state = state;
            }
        }
    }
}
