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

public class KamikotizationTransformTrigger extends SimpleCriterionTrigger<KamikotizationTransformTrigger.TriggerInstance> {
    @Override
    public Codec<TriggerInstance> codec() {
        return KamikotizationTransformTrigger.TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer player, ResourceKey<Kamikotization> type, boolean self) {
        this.trigger(player, (instance) -> instance.matches(type, self));
    }

    public record TriggerInstance(Optional<ContextAwarePredicate> player, Optional<ResourceKey<Kamikotization>> type, boolean self)
            implements SimpleInstance {

        public static final Codec<KamikotizationTransformTrigger.TriggerInstance> CODEC = RecordCodecBuilder.create(
                p_337367_ -> p_337367_.group(
                        EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(KamikotizationTransformTrigger.TriggerInstance::player),
                        ResourceKey.codec(MineraculousRegistries.KAMIKOTIZATION).optionalFieldOf("type").forGetter(KamikotizationTransformTrigger.TriggerInstance::type),
                        Codec.BOOL.optionalFieldOf("self", false).forGetter(KamikotizationTransformTrigger.TriggerInstance::self))
                        .apply(p_337367_, KamikotizationTransformTrigger.TriggerInstance::new));
        public static Criterion<KamikotizationTransformTrigger.TriggerInstance> transformed(boolean self) {
            return MineraculousCriteriaTriggers.TRANSFORMED_KAMIKOTIZATION.get().createCriterion(new TriggerInstance(Optional.empty(), Optional.empty(), self));
        }

        public static Criterion<KamikotizationTransformTrigger.TriggerInstance> transformed(ResourceKey<Kamikotization> type, boolean self) {
            return MineraculousCriteriaTriggers.TRANSFORMED_KAMIKOTIZATION.get().createCriterion(new TriggerInstance(Optional.empty(), Optional.of(type), self));
        }

        public boolean matches(ResourceKey<Kamikotization> type, boolean self) {
            return (this.type.isEmpty() || this.type.get() == type) && this.self == self;
        }
    }
}
