package dev.thomasglasser.mineraculous.api.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.thomasglasser.mineraculous.api.advancements.MineraculousCriteriaTriggers;
import dev.thomasglasser.mineraculous.api.core.registries.MineraculousRegistries;
import dev.thomasglasser.mineraculous.api.world.ability.Ability;
import dev.thomasglasser.mineraculous.api.world.miraculous.Miraculous;
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
 * Advancement trigger for when an entity performs their active {@link Miraculous} {@link Ability},
 * with a field for {@link Miraculous} key and an optional field for contexts.
 */
public class PerformMiraculousActiveAbilityTrigger extends SimpleCriterionTrigger<PerformMiraculousActiveAbilityTrigger.TriggerInstance> {
    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer player, ResourceKey<Miraculous> miraculous, String context) {
        this.trigger(player, instance -> instance.matches(miraculous, context));
    }

    public record TriggerInstance(Optional<ContextAwarePredicate> player, ResourceKey<Miraculous> miraculous, Optional<List<String>> contexts) implements SimpleInstance {

        static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player),
                ResourceKey.codec(MineraculousRegistries.MIRACULOUS).fieldOf("miraculous").forGetter(TriggerInstance::miraculous),
                Codec.STRING.listOf().optionalFieldOf("contexts").forGetter(TriggerInstance::contexts))
                .apply(instance, TriggerInstance::new));
        public static Criterion<TriggerInstance> performedActiveAbility(ResourceKey<Miraculous> miraculous) {
            return performedActiveAbility(miraculous, Optional.empty());
        }

        public static Criterion<TriggerInstance> performedActiveAbility(ResourceKey<Miraculous> miraculous, String... contexts) {
            return performedActiveAbility(miraculous, Optional.of(ReferenceArrayList.of(contexts)));
        }

        public static Criterion<TriggerInstance> performedActiveAbility(ResourceKey<Miraculous> miraculous, Optional<List<String>> contexts) {
            return criterion(Optional.empty(), miraculous, contexts);
        }

        public static Criterion<TriggerInstance> criterion(Optional<ContextAwarePredicate> player, ResourceKey<Miraculous> miraculous, Optional<List<String>> contexts) {
            return MineraculousCriteriaTriggers.PERFORMED_MIRACULOUS_ACTIVE_ABILITY.get().createCriterion(new TriggerInstance(player, miraculous, contexts));
        }

        public boolean matches(ResourceKey<Miraculous> miraculous, String context) {
            return this.miraculous == miraculous && this.contexts.map(contexts -> contexts.contains(context)).orElse(true);
        }
    }
}
