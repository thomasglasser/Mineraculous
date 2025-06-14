package dev.thomasglasser.mineraculous.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.thomasglasser.mineraculous.advancements.MineraculousCriteriaTriggers;
import dev.thomasglasser.mineraculous.core.registries.MineraculousRegistries;
import dev.thomasglasser.mineraculous.world.entity.kamikotization.Kamikotization;
import java.util.Optional;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;

public class TransformKamikotizationTrigger extends SimpleCriterionTrigger<TransformKamikotizationTrigger.TriggerInstance> {
    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer player, ResourceKey<Kamikotization> type, boolean self) {
        this.trigger(player, instance -> instance.matches(type, self));
    }

    public record TriggerInstance(Optional<ContextAwarePredicate> player, Optional<ResourceKey<Kamikotization>> type, Optional<Boolean> self) implements SimpleInstance {

        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player),
                ResourceKey.codec(MineraculousRegistries.KAMIKOTIZATION).optionalFieldOf("type").forGetter(TriggerInstance::type),
                Codec.BOOL.optionalFieldOf("self").forGetter(TriggerInstance::self))
                .apply(instance, TriggerInstance::new));
        public static Criterion<TriggerInstance> transformed() {
            return criterion(Optional.empty(), Optional.empty(), Optional.empty());
        }

        public static Criterion<TriggerInstance> transformed(ResourceKey<Kamikotization> type) {
            return criterion(Optional.empty(), Optional.of(type), Optional.empty());
        }

        public static Criterion<TriggerInstance> transformed(boolean self) {
            return criterion(Optional.empty(), Optional.empty(), Optional.of(self));
        }

        public static Criterion<TriggerInstance> transformed(ResourceKey<Kamikotization> type, boolean self) {
            return criterion(Optional.empty(), Optional.of(type), Optional.of(self));
        }

        public static Criterion<TriggerInstance> criterion(Optional<ContextAwarePredicate> player, Optional<ResourceKey<Kamikotization>> type, Optional<Boolean> self) {
            return MineraculousCriteriaTriggers.TRANSFORMED_KAMIKOTIZATION.get().createCriterion(new TriggerInstance(player, type, self));
        }

        public boolean matches(ResourceKey<Kamikotization> type, boolean self) {
            return this.type.map(key -> key == type).orElse(true) && this.self.map(value -> value == self).orElse(true);
        }
    }
}
