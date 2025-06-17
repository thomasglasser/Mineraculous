package dev.thomasglasser.mineraculous.api.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.thomasglasser.mineraculous.api.advancements.MineraculousCriteriaTriggers;
import dev.thomasglasser.mineraculous.api.core.registries.MineraculousRegistries;
import dev.thomasglasser.mineraculous.api.world.ability.Ability;
import dev.thomasglasser.mineraculous.api.world.kamikotization.Kamikotization;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
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
public class PerformKamikotizationActiveAbilityTrigger extends SimpleCriterionTrigger<PerformKamikotizationActiveAbilityTrigger.TriggerInstance> {
    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer player, ResourceKey<Kamikotization> kamikotization, String context) {
        this.trigger(player, instance -> instance.matches(kamikotization, context));
    }

    public record TriggerInstance(Optional<ContextAwarePredicate> player, ResourceKey<Kamikotization> kamikotization, Optional<List<String>> contexts) implements SimpleInstance {

        static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player),
                ResourceKey.codec(MineraculousRegistries.KAMIKOTIZATION).fieldOf("kamikotization").forGetter(TriggerInstance::kamikotization),
                Codec.STRING.listOf().optionalFieldOf("contexts").forGetter(TriggerInstance::contexts))
                .apply(instance, TriggerInstance::new));
        public static Criterion<TriggerInstance> performedActiveAbility(ResourceKey<Kamikotization> kamikotization) {
            return performedActiveAbility(kamikotization, Optional.empty());
        }

        public static Criterion<TriggerInstance> performedActiveAbility(ResourceKey<Kamikotization> kamikotization, String... contexts) {
            return performedActiveAbility(kamikotization, Optional.of(ReferenceArrayList.of(contexts)));
        }

        public static Criterion<TriggerInstance> performedActiveAbility(ResourceKey<Kamikotization> kamikotization, Optional<List<String>> contexts) {
            return criterion(Optional.empty(), kamikotization, contexts);
        }

        public static Criterion<TriggerInstance> criterion(Optional<ContextAwarePredicate> player, ResourceKey<Kamikotization> type, Optional<List<String>> contexts) {
            return MineraculousCriteriaTriggers.PERFORMED_KAMIKOTIZATION_ACTIVE_ABILITY.get().createCriterion(new TriggerInstance(player, type, contexts));
        }

        public boolean matches(ResourceKey<Kamikotization> type, String context) {
            return this.kamikotization == type && this.contexts.map(contexts -> contexts.contains(context)).orElse(true);
        }
    }
}
