package dev.thomasglasser.mineraculous.api.advancements.critereon;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.thomasglasser.mineraculous.api.advancements.MineraculousCriteriaTriggers;
import dev.thomasglasser.mineraculous.api.core.registries.MineraculousRegistries;
import dev.thomasglasser.mineraculous.api.world.ability.Ability;
import dev.thomasglasser.mineraculous.api.world.kamikotization.Kamikotization;
import java.util.List;
import java.util.Optional;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;

/**
 * Advancement trigger for when an entity performs their active {@link Kamikotization} {@link Ability},
 * with a field for {@link Kamikotization} key and an optional field for contexts.
 */
public class PerformedKamikotizationActiveAbilityTrigger extends SimpleCriterionTrigger<PerformedKamikotizationActiveAbilityTrigger.TriggerInstance> {
    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer player, ResourceKey<Kamikotization> kamikotization, String context) {
        this.trigger(player, instance -> instance.matches(kamikotization, context));
    }

    public record TriggerInstance(Optional<ContextAwarePredicate> player, Optional<ResourceKey<Kamikotization>> kamikotization, Optional<List<String>> contexts) implements SimpleInstance {

        static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player),
                ResourceKey.codec(MineraculousRegistries.KAMIKOTIZATION).optionalFieldOf("kamikotization").forGetter(TriggerInstance::kamikotization),
                Codec.STRING.listOf().optionalFieldOf("contexts").forGetter(TriggerInstance::contexts))
                .apply(instance, TriggerInstance::new));
        public static Criterion<TriggerInstance> performedActiveAbility() {
            return performedActiveAbility(Optional.empty(), Optional.empty(), Optional.empty());
        }

        public static Criterion<TriggerInstance> performedActiveAbility(ResourceKey<Kamikotization> kamikotization, String... contexts) {
            return performedActiveAbility(Optional.empty(), Optional.of(kamikotization), contexts.length == 0 ? Optional.empty() : Optional.of(ImmutableList.copyOf(contexts)));
        }

        public static Criterion<TriggerInstance> performedActiveAbility(Optional<ContextAwarePredicate> player, Optional<ResourceKey<Kamikotization>> kamikotization, Optional<List<String>> contexts) {
            return MineraculousCriteriaTriggers.PERFORMED_KAMIKOTIZATION_ACTIVE_ABILITY.get().createCriterion(new TriggerInstance(player, kamikotization, contexts));
        }

        public boolean matches(ResourceKey<Kamikotization> kamikotization, String context) {
            return this.kamikotization.map(key -> key == kamikotization).orElse(true) && this.contexts.map(contexts -> contexts.contains(context)).orElse(true);
        }
    }
}
