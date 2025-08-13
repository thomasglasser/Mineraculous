package dev.thomasglasser.mineraculous.api.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.thomasglasser.mineraculous.api.advancements.MineraculousCriteriaTriggers;
import dev.thomasglasser.mineraculous.api.core.registries.MineraculousRegistries;
import dev.thomasglasser.mineraculous.api.world.kamikotization.Kamikotization;
import java.util.Optional;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;

/**
 * Advancement trigger for when an entity transforms via {@link Kamikotization},
 * with optional fields for the {@link Kamikotization} key and whether the kamikotizer is the kamikotized.
 */
public class TransformedKamikotizationTrigger extends SimpleCriterionTrigger<TransformedKamikotizationTrigger.TriggerInstance> {
    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer player, ResourceKey<Kamikotization> kamikotization, boolean self) {
        this.trigger(player, instance -> instance.matches(kamikotization, self));
    }

    public record TriggerInstance(Optional<ContextAwarePredicate> player, Optional<ResourceKey<Kamikotization>> kamikotization, Optional<Boolean> self) implements SimpleInstance {

        static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player),
                ResourceKey.codec(MineraculousRegistries.KAMIKOTIZATION).optionalFieldOf("kamikotization").forGetter(TriggerInstance::kamikotization),
                Codec.BOOL.optionalFieldOf("self").forGetter(TriggerInstance::self))
                .apply(instance, TriggerInstance::new));
        public static Criterion<TriggerInstance> transformed() {
            return transformed(Optional.empty(), Optional.empty(), Optional.empty());
        }

        public static Criterion<TriggerInstance> transformed(ResourceKey<Kamikotization> type) {
            return transformed(Optional.empty(), Optional.of(type), Optional.empty());
        }

        public static Criterion<TriggerInstance> transformed(boolean self) {
            return transformed(Optional.empty(), Optional.empty(), Optional.of(self));
        }

        public static Criterion<TriggerInstance> transformed(ResourceKey<Kamikotization> type, boolean self) {
            return transformed(Optional.empty(), Optional.of(type), Optional.of(self));
        }

        public static Criterion<TriggerInstance> transformed(Optional<ContextAwarePredicate> player, Optional<ResourceKey<Kamikotization>> type, Optional<Boolean> self) {
            return MineraculousCriteriaTriggers.TRANSFORMED_KAMIKOTIZATION.get().createCriterion(new TriggerInstance(player, type, self));
        }

        public boolean matches(ResourceKey<Kamikotization> kamikotization, boolean self) {
            return this.kamikotization.map(key -> key == kamikotization).orElse(true) && this.self.map(value -> value == self).orElse(true);
        }
    }
}
