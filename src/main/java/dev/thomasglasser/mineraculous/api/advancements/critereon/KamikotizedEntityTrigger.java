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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootContext;

/**
 * Advancement trigger for when an entity is kamikotized,
 * with optional fields for target predicates,
 * {@link Kamikotization} key,
 * and whether the kamikotizer is the kamikotized.
 */
public class KamikotizedEntityTrigger extends SimpleCriterionTrigger<KamikotizedEntityTrigger.TriggerInstance> {
    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer player, Entity target, ResourceKey<Kamikotization> kamikotization, boolean self) {
        this.trigger(player, instance -> instance.matches(EntityPredicate.createContext(player, target), kamikotization, self));
    }

    public record TriggerInstance(Optional<ContextAwarePredicate> player, Optional<ContextAwarePredicate> target, Optional<ResourceKey<Kamikotization>> kamikotization, Optional<Boolean> self) implements SimpleInstance {

        static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player),
                EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("target").forGetter(TriggerInstance::target),
                ResourceKey.codec(MineraculousRegistries.KAMIKOTIZATION).optionalFieldOf("kamikotization").forGetter(TriggerInstance::kamikotization),
                Codec.BOOL.optionalFieldOf("self").forGetter(TriggerInstance::self)).apply(instance, TriggerInstance::new));
        public static Criterion<TriggerInstance> kamikotizedEntity() {
            return kamikotizedEntity(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
        }

        public static Criterion<TriggerInstance> kamikotizedEntity(ContextAwarePredicate target) {
            return kamikotizedEntity(Optional.empty(), Optional.of(target), Optional.empty(), Optional.empty());
        }

        public static Criterion<TriggerInstance> kamikotizedEntity(ResourceKey<Kamikotization> kamikotization) {
            return kamikotizedEntity(Optional.empty(), Optional.empty(), Optional.of(kamikotization), Optional.empty());
        }

        public static Criterion<TriggerInstance> kamikotizedEntity(boolean self) {
            return kamikotizedEntity(Optional.empty(), Optional.empty(), Optional.empty(), Optional.of(self));
        }

        public static Criterion<TriggerInstance> kamikotizedEntity(ContextAwarePredicate target, ResourceKey<Kamikotization> kamikotization) {
            return kamikotizedEntity(Optional.empty(), Optional.of(target), Optional.of(kamikotization), Optional.empty());
        }

        public static Criterion<TriggerInstance> kamikotizedEntity(ContextAwarePredicate target, boolean self) {
            return kamikotizedEntity(Optional.empty(), Optional.of(target), Optional.empty(), Optional.of(self));
        }

        public static Criterion<TriggerInstance> kamikotizedEntity(ResourceKey<Kamikotization> kamikotization, boolean self) {
            return kamikotizedEntity(Optional.empty(), Optional.empty(), Optional.of(kamikotization), Optional.of(self));
        }

        public static Criterion<TriggerInstance> kamikotizedEntity(ContextAwarePredicate target, ResourceKey<Kamikotization> kamikotization, boolean self) {
            return kamikotizedEntity(Optional.empty(), Optional.of(target), Optional.of(kamikotization), Optional.of(self));
        }

        public static Criterion<TriggerInstance> kamikotizedEntity(Optional<ContextAwarePredicate> player, Optional<ContextAwarePredicate> target, Optional<ResourceKey<Kamikotization>> kamikotization, Optional<Boolean> self) {
            return MineraculousCriteriaTriggers.KAMIKOTIZED_ENTITY.get().createCriterion(new TriggerInstance(player, target, kamikotization, self));
        }

        public boolean matches(LootContext target, ResourceKey<Kamikotization> kamikotization, boolean self) {
            return this.target.map(predicate -> predicate.matches(target)).orElse(true) && this.kamikotization.map(key -> key == kamikotization).orElse(true) && this.self.map(value -> value == self).orElse(true);
        }
    }
}
