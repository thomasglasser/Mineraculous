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

public class MiraculousTransformTrigger extends SimpleCriterionTrigger<MiraculousTransformTrigger.TriggerInstance> {
    @Override
    public Codec<TriggerInstance> codec() {
        return MiraculousTransformTrigger.TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer player, ResourceKey<Miraculous> type) {
        this.trigger(player, (instance) -> instance.matches(type));
    }

    public record TriggerInstance(Optional<ContextAwarePredicate> player, ResourceKey<Miraculous> type)
            implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<MiraculousTransformTrigger.TriggerInstance> CODEC = RecordCodecBuilder.create(
                p_337367_ -> p_337367_.group(
                        EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(MiraculousTransformTrigger.TriggerInstance::player),
                        ResourceKey.codec(MineraculousRegistries.MIRACULOUS).fieldOf("type").forGetter(MiraculousTransformTrigger.TriggerInstance::type))
                        .apply(p_337367_, MiraculousTransformTrigger.TriggerInstance::new));

        public static Criterion<MiraculousTransformTrigger.TriggerInstance> transformed(ResourceKey<Miraculous> type) {
            return MineraculousCriteriaTriggers.TRANSFORMED_MIRACULOUS.get().createCriterion(new TriggerInstance(Optional.empty(), type));
        }

        public boolean matches(ResourceKey<Miraculous> type) {
            return this.type == type;
        }
    }
}
