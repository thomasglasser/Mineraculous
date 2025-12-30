package dev.thomasglasser.mineraculous.api.event;

import dev.thomasglasser.mineraculous.api.world.ability.Ability;
import dev.thomasglasser.mineraculous.api.world.ability.AbilityData;
import dev.thomasglasser.mineraculous.api.world.ability.context.AbilityContext;
import dev.thomasglasser.mineraculous.api.world.ability.handler.AbilityHandler;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import org.jetbrains.annotations.Nullable;

public abstract class AbilityEvent extends LivingEvent {
    private final Holder<Ability> ability;
    private final AbilityData abilityData;

    public AbilityEvent(LivingEntity entity, Holder<Ability> ability, AbilityData abilityData) {
        super(entity);
        this.ability = ability;
        this.abilityData = abilityData;
    }

    public Holder<Ability> getAbility() {
        return ability;
    }

    public AbilityData getAbilityData() {
        return abilityData;
    }

    public abstract static class Perform extends AbilityEvent {
        private final AbilityHandler handler;
        @Nullable
        private final AbilityContext context;

        public Perform(LivingEntity entity, Holder<Ability> ability, AbilityData abilityData, AbilityHandler handler, @Nullable AbilityContext context) {
            super(entity, ability, abilityData);
            this.handler = handler;
            this.context = context;
        }

        public AbilityHandler getAbilityHandler() {
            return handler;
        }

        public @Nullable AbilityContext getAbilityContext() {
            return context;
        }

        public static class Pre extends Perform {
            @Nullable
            private Ability.State state = null;

            public Pre(LivingEntity entity, Holder<Ability> ability, AbilityData abilityData, AbilityHandler handler, @Nullable AbilityContext context) {
                super(entity, ability, abilityData, handler, context);
            }

            public @Nullable Ability.State getState() {
                return state;
            }

            public void setState(@Nullable Ability.State state) {
                this.state = state;
            }
        }

        public static class Post extends Perform {
            private Ability.State state;

            public Post(LivingEntity entity, Holder<Ability> ability, AbilityData abilityData, AbilityHandler handler, AbilityContext context, Ability.State state) {
                super(entity, ability, abilityData, handler, context);
                this.state = state;
            }

            public Ability.State getState() {
                return state;
            }

            public void setState(Ability.State state) {
                this.state = state;
            }
        }
    }
}
