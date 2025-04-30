package dev.thomasglasser.mineraculous.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.thomasglasser.mineraculous.advancements.MineraculousCriteriaTriggers;
import dev.thomasglasser.mineraculous.core.registries.MineraculousRegistries;
import dev.thomasglasser.mineraculous.world.entity.miraculous.Miraculous;
import java.util.Optional;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;

public class TransformMiraculousTrigger extends SimpleCriterionTrigger<TransformMiraculousTrigger.TriggerInstance> {
    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer player, ResourceKey<Miraculous> type) {
        this.trigger(player, instance -> instance.matches(type));
    }

    public record TriggerInstance(Optional<ContextAwarePredicate> player, Optional<ResourceKey<Miraculous>> type)
            implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player),
                ResourceKey.codec(MineraculousRegistries.MIRACULOUS).optionalFieldOf("type").forGetter(TriggerInstance::type))
                .apply(instance, TriggerInstance::new));

        public static Criterion<TriggerInstance> transformed() {
            return criterion(Optional.empty(), Optional.empty());
        }

        public static Criterion<TriggerInstance> transformed(ResourceKey<Miraculous> type) {
            return criterion(Optional.empty(), Optional.of(type));
        }

        public static Criterion<TriggerInstance> criterion(Optional<ContextAwarePredicate> player, Optional<ResourceKey<Miraculous>> type) {
            return MineraculousCriteriaTriggers.TRANSFORMED_MIRACULOUS.get().createCriterion(new TriggerInstance(player, type));
        }

        public boolean matches(ResourceKey<Miraculous> type) {
            return this.type.map(key -> key == type).orElse(true);
        }
    }
}
