package dev.thomasglasser.mineraculous.api.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.thomasglasser.mineraculous.api.advancements.MineraculousCriteriaTriggers;
import dev.thomasglasser.mineraculous.api.core.registries.MineraculousRegistries;
import dev.thomasglasser.mineraculous.api.world.miraculous.Miraculous;
import java.util.Optional;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;

/**
 * Advancement trigger for when an entity transforms via {@link Miraculous},
 * with an optional field for {@link Miraculous} key.
 */
public class TransformedMiraculousTrigger extends SimpleCriterionTrigger<TransformedMiraculousTrigger.TriggerInstance> {
    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer player, ResourceKey<Miraculous> miraculous) {
        this.trigger(player, instance -> instance.matches(miraculous));
    }

    public record TriggerInstance(Optional<ContextAwarePredicate> player, Optional<ResourceKey<Miraculous>> miraculous) implements SimpleInstance {
        static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player),
                ResourceKey.codec(MineraculousRegistries.MIRACULOUS).optionalFieldOf("miraculous").forGetter(TriggerInstance::miraculous))
                .apply(instance, TriggerInstance::new));

        public static Criterion<TriggerInstance> transformed() {
            return transformed(Optional.empty(), Optional.empty());
        }

        public static Criterion<TriggerInstance> transformed(ResourceKey<Miraculous> miraculous) {
            return transformed(Optional.empty(), Optional.of(miraculous));
        }

        public static Criterion<TriggerInstance> transformed(Optional<ContextAwarePredicate> player, Optional<ResourceKey<Miraculous>> type) {
            return MineraculousCriteriaTriggers.TRANSFORMED_MIRACULOUS.get().createCriterion(new TriggerInstance(player, type));
        }

        public boolean matches(ResourceKey<Miraculous> miraculous) {
            return this.miraculous.map(key -> key == miraculous).orElse(true);
        }
    }
}
